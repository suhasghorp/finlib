package com.finlib.products.rates;

import com.finlib.finutils.*;
import com.finlib.market.curves.DiscountCurve;

import java.time.LocalDate;
import java.util.Optional;

public class IborFRA {

    private LocalDate settlementDate,maturityDate;
    private double fraRate;
    private DayCountType dayCountType;
    private double notional;
    private boolean payFixedRate;

    private IborFRA(LocalDate settlementDate,
                     LocalDate maturityDate,
                     double fraRate,
                     DayCountType dayCountType,
                     double notional,
                     boolean payFixedRate,
                     CalendarType calendarType,
                     DayAdjustType dayAdjustType)  {
        if (settlementDate.isAfter(maturityDate))
            throw new FinlibException("Settlement date is after maturity date");
        Calendar cal = new Calendar(calendarType);
        this.settlementDate = cal.adjust(settlementDate,dayAdjustType);
        this.maturityDate = cal.adjust(maturityDate,dayAdjustType);
        this.fraRate = fraRate;
        this.dayCountType = dayCountType;
        this.payFixedRate = payFixedRate;
        this.notional = notional;
    }

    public static IborFRA of(LocalDate settlementDate,
                              LocalDate maturityDate,
                              double fraRate,
                              DayCountType dayCountType,
                              double notional,
                              boolean payFixedRate,
                              CalendarType calendarType,
                              DayAdjustType dayAdjustType) {
        return new IborFRA(settlementDate, maturityDate, fraRate, dayCountType, notional, payFixedRate, calendarType, dayAdjustType);
    }
    public static IborFRA of(LocalDate settlementDate,
                              LocalDate maturityDate,
                              double fraRate,
                              DayCountType dayCountType) {
        return of(settlementDate, maturityDate, fraRate, dayCountType, 100.0, true, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);
    }

    /*Determine the maturity date discount factor needed to refit
        the FRA given the libor curve anbd the contract FRA rate.*/

    public double maturityDf(DiscountCurve curve) {
        double df1 = curve.df(settlementDate);
        double accFactor = new DayCount(dayCountType).yearFrac(settlementDate, maturityDate, Optional.empty());
        return df1 / (1.0 + accFactor * fraRate);
    }
    public double value(LocalDate valueDate, DiscountCurve discountCurve, Optional<DiscountCurve> indexCurve) {
        if (indexCurve.isEmpty())
            indexCurve = Optional.of(discountCurve);

        double accFactor0 = new DayCount(dayCountType).yearFrac(settlementDate, maturityDate, Optional.empty());
        double df1 = indexCurve.get().df(settlementDate);
        double df2 = indexCurve.get().df(maturityDate);
        double liborFwd = (df1 / df2 - 1.0) / accFactor0;
        //Get the discount factor from a discount curve
        double dfDiscount2 = discountCurve.df(maturityDate);
        double v = accFactor0 * (liborFwd - fraRate) * dfDiscount2;
        // Forward value the FRA to the value date
        double dfTovalueDate = discountCurve.df(valueDate);
        v = v * notional / dfTovalueDate;
        if (payFixedRate)
            v = v * -1.0;
        return v;
    }

    public double maturityDF(DiscountCurve indexCurve){
        DayCount dc = new DayCount(dayCountType);
        double df1 = indexCurve.df(settlementDate);
        double accFactor = dc.yearFrac(settlementDate, maturityDate);
        double df2 = df1 / (1.0 + accFactor * fraRate);
        return df2;
    }
    public void print(){
        System.out.println("SETTLEMENT DATE:"+settlementDate);
        System.out.println("MATURITY DATE  :"+maturityDate);
        System.out.println("FRA RATE       :"+fraRate);
        System.out.println("PAY FIXED LEG  :"+payFixedRate);
        System.out.println("DAY COUNT TYPE :"+dayCountType);
    }
}
