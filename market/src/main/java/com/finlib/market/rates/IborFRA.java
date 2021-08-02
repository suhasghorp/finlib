package com.finlib.market.rates;

import com.finlib.finutils.*;
import com.finlib.shared.DiscountCurve;

import java.time.LocalDate;
import java.util.Optional;

public final class IborFRA {

    private final LocalDate startDate,maturityDate;
    private final double fraRate;
    private final DayCountType dayCountType;
    private final double notional;
    private final boolean payFixedRate;
    private final CalendarType calendarType;
    private final DayAdjustType dayAdjustType;

    private IborFRA(LocalDate startDate,
                    String tenor,
                    double fraRate,
                    DayCountType dayCountType,
                    double notional,
                    boolean payFixedRate,
                    CalendarType calendarType,
                    DayAdjustType dayAdjustType)  {

        Calendar cal = new Calendar(calendarType);
        this.startDate = startDate;
        this.maturityDate = cal.adjust(DateUtils.addTenor(startDate,tenor),dayAdjustType);
        this.fraRate = fraRate;
        this.dayCountType = dayCountType;
        this.payFixedRate = payFixedRate;
        this.notional = notional;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
    }

    private IborFRA(LocalDate startDate,
                     LocalDate maturityDate,
                     double fraRate,
                     DayCountType dayCountType,
                     double notional,
                     boolean payFixedRate,
                     CalendarType calendarType,
                     DayAdjustType dayAdjustType)  {
        if (startDate.isAfter(maturityDate))
            throw new FinlibException("Settlement date is after maturity date");
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.fraRate = fraRate;
        this.dayCountType = dayCountType;
        this.payFixedRate = payFixedRate;
        this.notional = notional;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
    }

    public static IborFRA of(LocalDate startDate,
                              LocalDate maturityDate,
                              double fraRate,
                              DayCountType dayCountType,
                              double notional,
                              boolean payFixedRate) {
        return new IborFRA(startDate, maturityDate, fraRate, dayCountType, notional, payFixedRate, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);
    }
    public static IborFRA of(LocalDate startDate,
                              LocalDate maturityDate,
                              double fraRate,
                              DayCountType dayCountType) {
        return new IborFRA(startDate, maturityDate, fraRate, dayCountType, 100.0, true, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);
    }
    public static IborFRA of(LocalDate startDate,
                             String tenor,
                             double fraRate,
                             DayCountType dayCountType) {
        return new IborFRA(startDate, DateUtils.addTenor(startDate,tenor), fraRate, dayCountType, 100.0, true, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);
    }

    public IborFRA bump(double bump){
        return new IborFRA( this.startDate,this.maturityDate,this.fraRate + bump, this.dayCountType, this.notional,
                this.payFixedRate, this.calendarType,this.dayAdjustType);
    }

    /*Determine the maturity date discount factor needed to refit
        the FRA given the libor curve anbd the contract FRA rate.*/

    public double maturityDf(DiscountCurve curve) {
        double df1 = curve.df(startDate);
        double accFactor = new DayCount(dayCountType).yearFrac(startDate, maturityDate, Optional.empty());
        return df1 / (1.0 + accFactor * fraRate);
    }
    public double value(LocalDate valueDate, DiscountCurve indexCurve, Optional<DiscountCurve> discountCurve) {
        if (discountCurve.isEmpty())
            discountCurve = Optional.of(indexCurve);

        double accFactor0 = new DayCount(dayCountType).yearFrac(startDate, maturityDate, Optional.empty());
        double df1 = indexCurve.df(startDate);
        double df2 = indexCurve.df(maturityDate);
        double liborFwd = (df1 / df2 - 1.0) / accFactor0;
        //Get the discount factor from a discount curve
        double dfDiscount2 = discountCurve.get().df(maturityDate);
        double v = accFactor0 * (liborFwd - fraRate) * dfDiscount2;
        // Forward value the FRA to the value date
        double dfTovalueDate = discountCurve.get().df(valueDate);
        v = v * notional / dfTovalueDate;
        if (payFixedRate)
            v = v * -1.0;
        return v;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public double getNotional() {
        return notional;
    }

    public void print(){
        System.out.println("SETTLEMENT DATE:"+startDate);
        System.out.println("MATURITY DATE  :"+maturityDate);
        System.out.println("FRA RATE       :"+fraRate);
        System.out.println("PAY FIXED LEG  :"+payFixedRate);
        System.out.println("DAY COUNT TYPE :"+dayCountType);
    }
}
