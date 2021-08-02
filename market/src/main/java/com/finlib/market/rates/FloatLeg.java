package com.finlib.market.rates;

import com.finlib.finutils.*;
import com.finlib.shared.DiscountCurve;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FloatLeg {
    private final LocalDate effectiveDate;
    private final LocalDate endDate;
    private final SwapType legType;
    private final double spread;
    private final FrequencyType freqType;
    private final DayCountType dayCountType;
    private final double notional;
    private final double principal;
    private final int paymentLag;
    private final CalendarType calendarType;
    private final DayAdjustType dayAdjustType;
    private final DateGenRuleType dateGenRuleType;
    private final LocalDate maturityDate;
    private final List<LocalDate> startAccrueDates = new ArrayList<>();
    private final List<LocalDate> endAccrueDates = new ArrayList<>();
    private final List<LocalDate> paymentDates = new ArrayList<>();
    private final DoubleArrayList rates = new DoubleArrayList();

    private final DoubleArrayList yearFracs = new DoubleArrayList();
    private final IntArrayList accruedDays = new IntArrayList();
    private final DoubleArrayList paymentDFs = new DoubleArrayList();
    private final DoubleArrayList payments = new DoubleArrayList();
    private final DoubleArrayList paymentPVs = new DoubleArrayList();
    private final DoubleArrayList cumulativePVs = new DoubleArrayList();
    private final Calendar calendar;
    private final DayCount dayCount;

    public static class Builder {
        private final LocalDate effectiveDate;
        private LocalDate endDate;
        private final SwapType legType;
        private final double spread;
        private final FrequencyType freqType;
        private final DayCountType dayCountType;
        private double notional = 1_000_000;
        private double principal = 0.0;
        private int paymentLag = 0;
        private CalendarType calendarType = CalendarType.WEEKEND;
        private DayAdjustType dayAdjustType = DayAdjustType.FOLLOWING;
        private DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;

        public Builder(LocalDate effectiveDate,
                       LocalDate endDate,
                       SwapType legType,
                       double spread,
                       FrequencyType freqType,
                       DayCountType dayCountType){
            this.effectiveDate = effectiveDate;
            this.endDate = endDate;
            this.legType = legType;
            this.spread = spread;
            this.freqType = freqType;
            this.dayCountType = dayCountType;
        }

        public Builder(LocalDate effectiveDate,
                       String tenor,
                       SwapType legType,
                       double spread,
                       FrequencyType freqType,
                       DayCountType dayCountType) {
            this.effectiveDate = effectiveDate;
            this.endDate = DateUtils.addTenor(this.effectiveDate, tenor);
            this.legType = legType;
            this.spread = spread;
            this.freqType = freqType;
            this.dayCountType = dayCountType;
        }

        public Builder withNotional(double notional){
            this.notional = notional;
            return this;
        }
        public Builder withPrincipal(double principal){
            this.principal = principal;
            return this;
        }
        public Builder withPaymentLag(int paymentLag){
            this.paymentLag = paymentLag;
            return this;
        }
        public Builder withCalendar(CalendarType calendarType){
            this.calendarType = calendarType;
            return this;
        }
        public Builder withDayAdjust(DayAdjustType dayAdjustType){
            this.dayAdjustType = dayAdjustType;
            return this;
        }
        public Builder withDateGenRule(DateGenRuleType dateGenRuleType){
            this.dateGenRuleType = dateGenRuleType;
            return this;
        }

        public FloatLeg build(){
            return new FloatLeg(this);
        }
    }

    private FloatLeg(Builder builder){
        this.effectiveDate = builder.effectiveDate;
        this.endDate = builder.endDate;
        this.legType = builder.legType;
        this.spread = builder.spread;
        this.freqType = builder.freqType;
        this.dayCountType = builder.dayCountType;
        this.notional = builder.notional;
        this.principal = builder.principal;
        this.paymentLag = builder.paymentLag;
        this.calendarType = builder.calendarType;
        this.dayAdjustType = builder.dayAdjustType;
        this.dateGenRuleType = builder.dateGenRuleType;
        this.calendar = new Calendar(this.calendarType);
        this.dayCount = new DayCount(this.dayCountType);
        this.maturityDate = this.calendar.adjust(endDate, this.dayAdjustType);
        generatePaymentDates();
    }

    private void generatePaymentDates() {
        Schedule schedule = new Schedule.Builder(effectiveDate,endDate).withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(dayAdjustType)
                .withDateGenRule(dateGenRuleType).build();
        List<LocalDate> scheduleDates = schedule.getAdjustedDates();
        LocalDate prevDate = scheduleDates.get(0);
        for (int i = 1; i < scheduleDates.size();i++){
            startAccrueDates.add(prevDate);
            endAccrueDates.add(scheduleDates.get(i));
            if (paymentLag == 0){
                paymentDates.add(scheduleDates.get(i));
            } else {
                paymentDates.add(calendar.addBusinessDays(scheduleDates.get(i), paymentLag));
            }
            double yearFrac = dayCount.yearFrac(prevDate, scheduleDates.get(i), Optional.empty());
            yearFracs.add(yearFrac);

            accruedDays.add((int) ChronoUnit.DAYS.between(prevDate, scheduleDates.get(i)));
            prevDate = scheduleDates.get(i);
        }
    }

    public double value(LocalDate valuationDate, DiscountCurve indexCurve, DiscountCurve discountCurve, Optional<Double> firstFixing){
        rates.clear();
        payments.clear();
        paymentDFs.clear();
        paymentPVs.clear();
        cumulativePVs.clear();
        MathContext mc = MathContext.DECIMAL128;
        double dfValDt = discountCurve.df(valuationDate);
        int numPayments = paymentDates.size();
        double legPV = 0.0, fwdRate = 0.0;
        boolean firstPayment = false;
        for (int i = 0; i < numPayments;i++){
            LocalDate paymentDt = paymentDates.get(i);
            if (paymentDt.isAfter(valuationDate)) {
                LocalDate startAccrueDt = startAccrueDates.get(i);
                LocalDate endAccrueDt = endAccrueDates.get(i);
                double alpha = yearFracs.getDouble(i);

                if (firstPayment == false && firstFixing.isPresent()) {
                    fwdRate = firstFixing.get();
                    firstPayment = true;
                } else {
                    double dfStart = indexCurve.df(startAccrueDt);
                    double dfEnd = indexCurve.df(endAccrueDt);
                    //fwdRate = (dfStart / dfEnd - 1.0) / alpha;
                    try {
                        fwdRate = BigDecimal.valueOf(dfStart).divide(BigDecimal.valueOf(dfEnd), mc).subtract(BigDecimal.valueOf(1.0)).
                                divide(BigDecimal.valueOf(alpha), mc).doubleValue();
                    } catch (NumberFormatException nfe){
                        System.out.println("here");
                    }
                }
                double pmntAmount = BigDecimal.valueOf(fwdRate).add(BigDecimal.valueOf(spread)).multiply(BigDecimal.valueOf(alpha)).
                        multiply(BigDecimal.valueOf(notional)).doubleValue();
                double dfPmnt = BigDecimal.valueOf(discountCurve.df(paymentDt)).divide(BigDecimal.valueOf(dfValDt),mc).doubleValue();
                double pmntPV = BigDecimal.valueOf(pmntAmount).multiply(BigDecimal.valueOf(dfPmnt)).doubleValue();
                legPV = BigDecimal.valueOf(legPV).add(BigDecimal.valueOf(pmntPV)).doubleValue();

                rates.add(fwdRate);
                payments.add(pmntAmount);
                paymentDFs.add(dfPmnt);
                paymentPVs.add(pmntPV);
                cumulativePVs.add(legPV);
            } else {
                rates.add(0.0);
                payments.add(0.0);
                paymentDFs.add(0.0);
                paymentPVs.add(0.0);
                cumulativePVs.add(legPV);
            }

            /*if (paymentDates.get(paymentDates.size() - 1).isAfter(valuationDate)){
                double paymentPV = principal * paymentDFs.getDouble(paymentDFs.size()-1) * notional;
                paymentPVs.set(paymentPVs.size()-1, paymentPVs.getDouble(paymentPVs.size()-1) + paymentPV);
                legPV += paymentPV;
                cumulativePVs.set(cumulativePVs.size()-1, legPV);
            }*/
        }
        if (paymentDates.get(paymentDates.size() - 1).isAfter(valuationDate)){
            double paymentPV = principal * paymentDFs.getDouble(paymentDFs.size()-1) * notional;
            paymentPVs.set(paymentPVs.size()-1, paymentPVs.getDouble(paymentPVs.size()-1) + paymentPV);
            legPV += paymentPV;
            cumulativePVs.set(cumulativePVs.size()-1, legPV);
        }

        if (legType == SwapType.PAY)
            legPV = legPV * -1.0;

        return legPV;
    }
}
