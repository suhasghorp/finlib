package com.finlib.products.rates;

import com.finlib.finutils.*;
import com.finlib.market.curves.DiscountCurve;

import java.time.LocalDate;
import java.util.Optional;

public class IborDeposit {
    public LocalDate settlementDate,maturityDate;
    public double depositRate;
    public DayCountType dayCountType;
    public double notional;
    public CalendarType calendarType;
    public DayAdjustType dayAdjustType;

    private IborDeposit(LocalDate settlementDate,
                         LocalDate maturityDate,
                         double depositRate,
                         DayCountType dayCountType,
                         double notional,
                         CalendarType calendarType,
                         DayAdjustType dayAdjustType) {
        if (settlementDate.isAfter(maturityDate))
            throw new FinlibException("Settlement date is after maturity date");
        this.settlementDate = settlementDate;
        this.maturityDate = maturityDate;
        this.depositRate = depositRate;
        this.dayCountType = dayCountType;
        this.notional = notional;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
    }



    public static IborDeposit of (LocalDate settlementDate,
                                   String tenor,
                                   double depositRate,
                                   DayCountType dayCountType){
        return of(settlementDate, tenor, depositRate, dayCountType, 100.0, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);

    }
    public static IborDeposit of(LocalDate settlementDate,
                                  String tenor,
                                  double depositRate,
                                  DayCountType dayCountType,
                                  double notional,
                                  CalendarType calendarType,
                                  DayAdjustType dayAdjustType) {
        LocalDate maturityDate = DateUtils.addTenor(settlementDate,tenor);
        Calendar calendar = new Calendar(calendarType);
        maturityDate = calendar.adjust(maturityDate, dayAdjustType);
        return new IborDeposit(settlementDate, maturityDate, depositRate, dayCountType, notional, calendarType, dayAdjustType);
    }

    /*Returns the maturity date discount factor that would allow the
        Libor curve to reprice the contractual market deposit rate. Note that
        this is a forward discount factor that starts on settlement date.*/
    public double maturityDf() throws Exception {
        double accFactor = new DayCount(dayCountType).yearFrac(settlementDate, maturityDate, Optional.empty());
        return 1.0 / (1.0 + accFactor * depositRate);
    }

    /*Determine the value of the Deposit given a Libor curve*/
    public double value(LocalDate valueDate, DiscountCurve curve) {
        if (valueDate.isAfter(maturityDate))
            throw new FinlibException("Start date after maturity date");

        double accFactor = new DayCount(dayCountType).yearFrac(settlementDate, maturityDate, Optional.empty());
        double df = curve.df(maturityDate);
        double value =  (1.0 + accFactor * depositRate) * df * notional;
        double df_settlement = curve.df(settlementDate);
        return value / df_settlement;
    }

    public void print(){
        System.out.println("SETTLEMENT DATE:" + settlementDate);
        System.out.println("MATURITY DATE:" + maturityDate);
        System.out.println("DAY COUNT TYPE:"+ dayCountType);
        System.out.println("DEPOSIT RATE:"+ depositRate);
    }
}
