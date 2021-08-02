package com.finlib.market.rates;

import com.finlib.finutils.*;
import com.finlib.shared.DiscountCurve;

import java.time.LocalDate;
import java.util.Optional;

public final class IborDeposit {
    private final LocalDate startDate,maturityDate;
    private final double depositRate;
    private final DayCountType dayCountType;
    private final double notional;
    private final CalendarType calendarType;
    private final DayAdjustType dayAdjustType;

    private IborDeposit(LocalDate startDate,
                         LocalDate maturityDate,
                         double depositRate,
                         DayCountType dayCountType,
                         double notional,
                         CalendarType calendarType,
                         DayAdjustType dayAdjustType) {
        if (startDate.isAfter(maturityDate))
            throw new FinlibException("Settlement date is after maturity date");
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.depositRate = depositRate;
        this.dayCountType = dayCountType;
        this.notional = notional;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
    }

    public static IborDeposit of (LocalDate startDate,
                                   String tenor,
                                   double depositRate,
                                   DayCountType dayCountType){
        return of(startDate, tenor, depositRate, dayCountType, 100.0, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);

    }

    public static IborDeposit of (LocalDate startDate,
                                  LocalDate maturityDate,
                                  double depositRate,
                                  DayCountType dayCountType){
        return of(startDate, maturityDate, depositRate, dayCountType, 100.0, CalendarType.WEEKEND, DayAdjustType.MODIFIED_FOLLOWING);

    }
    public static IborDeposit of(LocalDate startDate,
                                 LocalDate maturityDate,
                                 double depositRate,
                                 DayCountType dayCountType,
                                 double notional,
                                 CalendarType calendarType,
                                 DayAdjustType dayAdjustType) {
        return new IborDeposit(startDate, maturityDate, depositRate, dayCountType, notional, calendarType, dayAdjustType);
    }

    public static IborDeposit of(LocalDate startDate,
                                  String tenor,
                                  double depositRate,
                                  DayCountType dayCountType,
                                  double notional,
                                  CalendarType calendarType,
                                  DayAdjustType dayAdjustType) {
        LocalDate maturityDate = DateUtils.addTenor(startDate,tenor);
        Calendar calendar = new Calendar(calendarType);
        maturityDate = calendar.adjust(maturityDate, dayAdjustType);
        return new IborDeposit(startDate, maturityDate, depositRate, dayCountType, notional, calendarType, dayAdjustType);
    }

    public static IborDeposit of(IborDeposit depo) {
        return new IborDeposit(depo.getStartDate(), depo.getMaturityDate(), depo.getDepositRate(),
                depo.getDayCountType(), depo.getNotional(), depo.getCalendarType(), depo.getDayAdjustType());
    }

    public IborDeposit bump(double bump){
        return new IborDeposit(this.getStartDate(), this.getMaturityDate(), this.getDepositRate() + bump,
                this.getDayCountType(), this.getNotional(), this.getCalendarType(), this.getDayAdjustType());
    }

    /*Returns the maturity date discount factor that would allow the
        Libor curve to reprice the contractual market deposit rate. Note that
        this is a forward discount factor that starts on settlement date.*/
    public double maturityDf() {
        double accFactor = new DayCount(dayCountType).yearFrac(startDate, maturityDate, Optional.empty());
        return 1.0 / (1.0 + accFactor * depositRate);
    }

    /*Determine the value of the Deposit given a Libor curve*/
    public double value(LocalDate valueDate, DiscountCurve curve) {
        if (valueDate.isAfter(maturityDate))
            throw new FinlibException("Start date after maturity date");

        double accFactor = new DayCount(dayCountType).yearFrac(startDate, maturityDate, Optional.empty());
        double df = curve.df(maturityDate);
        double value =  (1.0 + accFactor * depositRate) * df * notional;
        double df_settlement = curve.df(startDate);
        return value / df_settlement;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public double getDepositRate() {
        return depositRate;
    }

    public DayCountType getDayCountType() {
        return dayCountType;
    }

    public double getNotional() {
        return notional;
    }

    public CalendarType getCalendarType() {
        return calendarType;
    }

    public DayAdjustType getDayAdjustType() {
        return dayAdjustType;
    }

    public void print(){
        System.out.println("SETTLEMENT DATE:" + startDate);
        System.out.println("MATURITY DATE:" + maturityDate);
        System.out.println("DAY COUNT TYPE:"+ dayCountType);
        System.out.println("DEPOSIT RATE:"+ depositRate);
    }
}
