package com.finlib.market.rates;

import com.finlib.finutils.DateUtils;
import com.finlib.finutils.DayCountType;

import java.time.LocalDate;

public final class IborFuture {
    private final LocalDate todayDate;
    private final int futureNumber;
    private final String futureTenor;
    private final DayCountType accrualType;
    private final double contractSize;
    private LocalDate deliveryDate;
    private LocalDate endOfInterestPeriod;
    private final LocalDate lastTradingDate;

    private IborFuture(LocalDate todayDate,
            int futureNumber,
            String futureTenor,
            DayCountType accrualType,
            double contractSize){

        this.futureNumber = futureNumber;
        this.futureTenor = futureTenor;
        this.todayDate = todayDate;
        this.deliveryDate = DateUtils.nextIMMDate(todayDate);
        for (int i = 0; i < futureNumber - 1; i++) {
            this.deliveryDate = DateUtils.nextIMMDate(this.deliveryDate);
        }
        this.endOfInterestPeriod = DateUtils.nextIMMDate(this.deliveryDate);
        this.lastTradingDate = deliveryDate.plusDays(-2);
        this.accrualType = accrualType;
        this.contractSize = contractSize;
    }

    public static IborFuture of(LocalDate todayDate,int futureNumber){
        return new IborFuture(todayDate,futureNumber,"3M",DayCountType.ACT_360,1_000_000);
    }
    public static double futuresRate(double futuresPrice) {
        return (100.0 - futuresPrice) / 100.0;
    }

    public static double futureToFRARate(double price, double convexity) {
        double fraRate = 0.0;
        double futRate = (100 - price) / 100.0;
        if (convexity < 0)
            fraRate = futRate + convexity / 100.0;
        else
            fraRate = futRate - convexity / 100.0;
        return fraRate;
    }

    public IborFRA toFRA(double futuresPrice, double convexity){
        double fraRate = IborFuture.futureToFRARate(futuresPrice, convexity);
        return IborFRA.of(deliveryDate,endOfInterestPeriod,fraRate,accrualType,contractSize,false);
    }
}
