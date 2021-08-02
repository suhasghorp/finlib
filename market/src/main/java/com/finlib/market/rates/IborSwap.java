package com.finlib.market.rates;

import com.finlib.finutils.*;
import com.finlib.shared.DiscountCurve;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IborSwap {
    private final LocalDate effectiveDate;
    private final LocalDate terminationDate;
    private final LocalDate maturityDate;
    private final double fixedCoupon,notional,floatSpread;
    private final FrequencyType fixedFreqType;
    private final DayCountType fixedDayCountType;
    private final FrequencyType floatFreqType;
    private final DayCountType floatDayCountType;
    private final SwapType fixedLegType;
    private final CalendarType calendarType;
    private final FixedLeg fixedLeg;
    private final FloatLeg floatLeg;
    private final DayAdjustType dayAdjustType;
    private final DateGenRuleType dateGenRuleType;
    private final List<LocalDate> adjustedFixedDates = new ArrayList<>();
    private final List<LocalDate> adjustedFloatDates = new ArrayList<>();
    private final List<Double> fixedYearFracs = new ArrayList<>();
    private final List<Double> fixedFlows = new ArrayList<>();
    private final List<Double> fixedDfs = new ArrayList<>();
    private final List<Double> fixedFlowPVs = new ArrayList<>();
    private final List<Double> floatYearFracs = new ArrayList<>();
    private final List<Double> floatFlows = new ArrayList<>();
    private final List<Double> floatDfs = new ArrayList<>();
    private final List<Double> floatFlowPVs = new ArrayList<>();
    private final Calendar calendar;
    private final SwapType floatLegType;
    private final int paymentLag;
    private final double principal;

    protected IborSwap(LocalDate effectiveDate,
                      LocalDate terminationDate,
                      SwapType fixedLegType,
                      double fixedCoupon,
                      FrequencyType fixedFreqType,
                      DayCountType fixedDayCountType,
                      double notional,
                      double floatSpread,
                      FrequencyType floatFreqType,
                      DayCountType floatDayCountType,
                      CalendarType calendarType,
                      DayAdjustType dayAdjustType,
                      DateGenRuleType dateGenRuleType) {
        this.effectiveDate = effectiveDate;
        this.terminationDate = terminationDate;
        this.fixedCoupon = fixedCoupon;
        this.fixedFreqType = fixedFreqType;
        this.fixedDayCountType = fixedDayCountType;
        this.notional = notional;
        this.floatSpread = floatSpread;
        this.floatFreqType = floatFreqType;
        this.floatDayCountType = floatDayCountType;
        this.fixedLegType = fixedLegType;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
        this.dateGenRuleType = dateGenRuleType;
        calendar = new Calendar(calendarType);
        maturityDate = calendar.adjust(terminationDate,dayAdjustType);
        floatLegType = fixedLegType == SwapType.PAY ? SwapType.RECEIVE : SwapType.PAY;
        paymentLag = 0;
        principal = 0.0;
        //generateFixedLegPaymentDates();
        //generateFloatLegPaymentDates();
        fixedLeg = new FixedLeg.Builder(effectiveDate,terminationDate,fixedLegType,fixedCoupon,
                fixedFreqType,
                fixedDayCountType)
                .withNotional(notional)
                .withPrincipal(principal)
                .withPaymentLag(paymentLag)
                .withCalendar(calendarType)
                .withDayAdjust(dayAdjustType)
                .withDateGenRule(dateGenRuleType).build();
        floatLeg = new FloatLeg.Builder(effectiveDate,
                terminationDate,
                floatLegType,
                floatSpread,
                floatFreqType,
                floatDayCountType)
                .withNotional(notional)
                .withPrincipal(principal)
                .withPaymentLag(paymentLag)
                .withCalendar(calendarType)
                .withDayAdjust(dayAdjustType)
                .withDateGenRule(dateGenRuleType).build();
    }

    public static IborSwap of(LocalDate effectiveDate,
                    LocalDate terminationDate,
                    SwapType fixedLegType,
                    double fixedCoupon,
                    FrequencyType fixedFreqType,
                    DayCountType fixedDayCountType,
                    double notional,
                    double floatSpread,
                    FrequencyType floatFreqType,
                    DayCountType floatDayCountType,
                    CalendarType calendarType,
                    DayAdjustType dayAdjustType,
                    DateGenRuleType dateGenRuleType){
        return new IborSwap( effectiveDate, terminationDate,fixedLegType, fixedCoupon,fixedFreqType,
                fixedDayCountType, notional, floatSpread, floatFreqType,
                floatDayCountType, calendarType, dayAdjustType,dateGenRuleType);
    }

    public static IborSwap of(LocalDate effectiveDate,
                       String tenor,
                       SwapType fixedLegType,
                       double fixedCoupon,
                       FrequencyType fixedFreqType,
                       DayCountType fixedDayCountType,
                       double notional,
                       double floatSpread,
                       FrequencyType floatFreqType,
                       DayCountType floatDayCountType,
                       CalendarType calendarType,
                       DayAdjustType dayAdjustType,
                       DateGenRuleType dateGenRuleType) {
        return new IborSwap( effectiveDate, DateUtils.addTenor(effectiveDate,tenor),fixedLegType, fixedCoupon,fixedFreqType,
                fixedDayCountType, notional, floatSpread, floatFreqType,
                floatDayCountType, calendarType, dayAdjustType,dateGenRuleType);
    }

    public static IborSwap of(LocalDate effectiveDate,
                       LocalDate terminationDate,
                       SwapType fixedLegType,
                       double fixedCoupon,
                       FrequencyType fixedFreqType,
                       DayCountType fixedDayCountType) {
        return new IborSwap( effectiveDate, terminationDate,fixedLegType,fixedCoupon,fixedFreqType,
                fixedDayCountType, 1_000_000.0, 0.0, FrequencyType.QUARTERLY,
                DayCountType.THIRTY_E_360, CalendarType.WEEKEND, DayAdjustType.FOLLOWING, DateGenRuleType.BACKWARD);
    }

    public static IborSwap of(LocalDate effectiveDate,
                       String tenor,
                       SwapType fixedLegType,
                       double fixedCoupon,
                       FrequencyType fixedFreqType,
                       DayCountType fixedDayCountType) {
        return new IborSwap( effectiveDate, DateUtils.addTenor(effectiveDate,tenor),fixedLegType,fixedCoupon,fixedFreqType,
                fixedDayCountType, 1_000_000.0, 0.0, FrequencyType.QUARTERLY,
                DayCountType.THIRTY_E_360, CalendarType.WEEKEND, DayAdjustType.FOLLOWING, DateGenRuleType.BACKWARD);
    }

    public static IborSwap ofOIS(LocalDate effectiveDate,
                              String tenor,
                              SwapType fixedLegType,
                              double fixedCoupon,
                              FrequencyType fixedFreqType,
                              DayCountType fixedDayCountType) {
        return new IborSwap( effectiveDate, DateUtils.addTenor(effectiveDate,tenor),fixedLegType,fixedCoupon,fixedFreqType,
                fixedDayCountType, 1_000_000.0, 0.0, FrequencyType.ANNUAL,
                DayCountType.THIRTY_E_360, CalendarType.WEEKEND, DayAdjustType.FOLLOWING, DateGenRuleType.BACKWARD);
    }

    public IborSwap bump(double bump){
        return new IborSwap(this.effectiveDate,
                this.terminationDate,
                this.fixedLegType,
                this.fixedCoupon + bump,
                this.fixedFreqType,
                this.fixedDayCountType,
                this.notional,
                this.floatSpread,
                this.floatFreqType,
                this.floatDayCountType,
                this.calendarType,
                this.dayAdjustType,
                this.dateGenRuleType);
    }


    public double value(LocalDate valuationDate, DiscountCurve indexCurve, Optional<DiscountCurve> discountCurve, Optional<Double> firstFixing){
        DiscountCurve discCurve = discountCurve.orElse(indexCurve);
        double fixedLegValue = fixedLeg.value(valuationDate,discCurve);
        double floatLegValue = floatLeg.value(valuationDate,indexCurve,discCurve,firstFixing);
        double value = fixedLegValue + floatLegValue;
        return value;
    }


    public double pv01(LocalDate valuationDate, DiscountCurve discountCurve) {
        double pv = Math.abs(fixedLeg.value(valuationDate, discountCurve));
        double pv01 = pv / fixedLeg.getCoupon() / fixedLeg.getNotional();
        return pv01;
    }


    public LocalDate getMaturityDate() {
        return maturityDate;
    }


    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }


    public FixedLeg getFixedLeg() {
        return fixedLeg;
    }


    public FloatLeg getFloatLeg() {
        return floatLeg;
    }

    /*Calculate the fixed leg coupon that makes the swap worth zero.
                If the valuation date is before the swap payments start then this
                is the forward swap rate as it starts in the future. The swap rate
                is then a forward swap rate and so we use a forward discount
                factor. If the swap fixed leg has begun then we have a spot
                starting swap.*/

    public double swapRate(LocalDate valueDate, DiscountCurve indexCurve, Optional<DiscountCurve> discountCurve, Optional<Double> firstFixing){
        DiscountCurve discCurve = discountCurve.orElse(indexCurve);
        double pv_ONE = pv01(valueDate, discCurve);
        if (Math.abs(pv_ONE) < 1e-10)
            throw new FinlibException("PV01 is zero. Cannot compute swap rate.");
        double df0;
        if (valueDate.isBefore(effectiveDate)) {
            df0 = discCurve.df(effectiveDate);
        }else {
            df0 = discCurve.df(valueDate);
        }
        double floatLegPV = 0.0;
        if (discountCurve.isEmpty()) {
            double dfT = discCurve.df(maturityDate);
            floatLegPV = (df0 - dfT);
        } else {
            floatLegPV = floatLeg.value(valueDate,indexCurve,discountCurve.get(),firstFixing)/fixedLeg.getNotional();
        }
        double cpn = floatLegPV / pv_ONE;
        return cpn;
    }


    public double cashSettledPV01(LocalDate valuationDate, double flatSwapRate, FrequencyType frequencyType){
        int freq = frequencyType.getFrequency();
        List<LocalDate> paymentDates = fixedLeg.getPaymentDates();
        if (freq == 0)
            throw new FinlibException("Frequency cannot be zero");
        int startIndex = 0;
        while (paymentDates.get(startIndex).isBefore(valuationDate)) {
            startIndex += 1;
        }
        if (valuationDate.isBefore(effectiveDate) || valuationDate.isEqual(effectiveDate)) {
            startIndex = 1;
        }

        double flatPV01 = 0.0;
        double df = 1.0;
        double alpha = 1.0 / freq;

        for (int i = startIndex; i < paymentDates.size(); i++) {
            df = df / (1.0 + alpha * flatSwapRate);
            flatPV01 += df * alpha;
        }

        return flatPV01;
    }
}
