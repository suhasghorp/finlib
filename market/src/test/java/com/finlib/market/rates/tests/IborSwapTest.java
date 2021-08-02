package com.finlib.market.rates.tests;

import com.finlib.finutils.*;
import com.finlib.market.rates.*;
import com.finlib.shared.InterpolationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IborSwapTest {

    private IborCurve buildIborSingleCurve(LocalDate valuationDate) {

        LocalDate settlementDate = valuationDate.plusDays(2);
        DayCountType dcType = DayCountType.ACT_360;

        List<IborDeposit> depos = new ArrayList<>();
        List<IborFRA> fras = new ArrayList<>();
        List<IborSwap> swaps = new ArrayList<>();

        LocalDate maturityDate = settlementDate.plusMonths(1);
        IborDeposit depo1 = IborDeposit.of(valuationDate, maturityDate, -0.00251, dcType);
        depos.add(depo1);

        //Series of 1M futures
        LocalDate startDate = DateUtils.nextIMMDate(settlementDate);
        LocalDate endDate = startDate.plusMonths(1);
        IborFRA fra = IborFRA.of(startDate, endDate, -0.0023, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00234, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00225, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00226, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00219, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00213, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00186, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00189, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00175, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00143, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00126, dcType);
        fras.add(fra);

        startDate = startDate.plusMonths(1);
        endDate = startDate.plusMonths(1);
        fra = IborFRA.of(startDate, endDate, -0.00126, dcType);
        fras.add(fra);


        FrequencyType fixedFreq = FrequencyType.ANNUAL;
        dcType = DayCountType.THIRTY_E_360;
        SwapType fixedLegType = SwapType.PAY;

        maturityDate = settlementDate.plusMonths(24);
        double swapRate = -0.001506;
        IborSwap swap1 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap1);


        maturityDate = settlementDate.plusMonths(36);
        swapRate = -0.000185;
        IborSwap swap2 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap2);


        maturityDate = settlementDate.plusMonths(48);
        swapRate = 0.001358;
        IborSwap swap3 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap3);

        maturityDate = settlementDate.plusMonths(60);
        swapRate = 0.0027652;
        IborSwap swap4 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap4);


        maturityDate = settlementDate.plusMonths(72);
        swapRate = 0.0041539;
        IborSwap swap5 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap5);


        maturityDate = settlementDate.plusMonths(84);
        swapRate = 0.0054604;
        IborSwap swap6 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap6);

        maturityDate = settlementDate.plusMonths(96);
        swapRate = 0.006674;
        IborSwap swap7 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap7);

        maturityDate = settlementDate.plusMonths(108);
        swapRate = 0.007826;
        IborSwap swap8 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap8);

        maturityDate = settlementDate.plusMonths(120);
        swapRate = 0.008821;
        IborSwap swap9 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap9);

        maturityDate = settlementDate.plusMonths(132);
        swapRate = 0.0097379;
        IborSwap swap10 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap10);

        maturityDate = settlementDate.plusMonths(144);
        swapRate = 0.0105406;
        IborSwap swap11 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap11);

        maturityDate = settlementDate.plusMonths(180);
        swapRate = 0.0123927;
        IborSwap swap12 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap12);

        maturityDate = settlementDate.plusMonths(240);
        swapRate = 0.0139882;
        IborSwap swap13 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap13);

        maturityDate = settlementDate.plusMonths(300);
        swapRate = 0.0144972;
        IborSwap swap14 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap14);

        maturityDate = settlementDate.plusMonths(360);
        swapRate = 0.0146081;
        IborSwap swap15 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap15);

        maturityDate = settlementDate.plusMonths(420);
        swapRate = 0.01461897;
        IborSwap swap16 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap16);


        maturityDate = settlementDate.plusMonths(480);
        swapRate = 0.014567455;
        IborSwap swap17 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap17);


        maturityDate = settlementDate.plusMonths(540);
        swapRate = 0.0140826;
        IborSwap swap18 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap18);


        maturityDate = settlementDate.plusMonths(600);
        swapRate = 0.01436822;
        IborSwap swap19 = IborSwap.of(settlementDate, maturityDate, fixedLegType,
                swapRate, fixedFreq, dcType);
        swaps.add(swap19);


        IborCurve liborCurve = new IborCurve(valuationDate, Optional.ofNullable(null), depos, fras, swaps, InterpolationType.FLAT_FORWARD_RATES, true);
        for (IborDeposit depo : depos) {
            double v = depo.value(settlementDate, liborCurve);
            //testCases.print("DEPO VALUE:", depo._maturityDate, v)
        }

        for (IborFRA fraa : fras) {
            double v = fraa.value(settlementDate, liborCurve, Optional.ofNullable(null));
            //testCases.print("FRA VALUE:", fra._maturityDate, v)
        }

        for (IborSwap swap : swaps) {
            double v = swap.value(settlementDate, liborCurve, Optional.ofNullable(null), Optional.ofNullable(null));
            //testCases.print("SWAP VALUE:", swap._maturityDate, v)
        }

        return liborCurve;
    }

    @Test
    public void LiborSwapTest(){
        // I have tried to reproduce the example from the blog by Ioannis Rigopoulos
        //https://blog.deriscope.com/index.php/en/excel-interest-rate-swap-price-dual-bootstrapping-curve

        LocalDate startDate = LocalDate.of(2017,12,27);
        LocalDate endDate = LocalDate.of(2067,12,27);

        double fixedCoupon = 0.015;
        FrequencyType fixedFreqType = FrequencyType.ANNUAL;
        DayCountType fixedDayCountType = DayCountType.THIRTY_E_360;

        double floatSpread = 0.0;
        FrequencyType floatFreqType = FrequencyType.SEMI_ANNUAL;
        DayCountType floatDayCountType = DayCountType.ACT_360;
        double firstFixing = -0.00268;

        CalendarType swapCalendarType = CalendarType.WEEKEND;
        DayAdjustType busDayAdjustType = DayAdjustType.FOLLOWING;
        DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        SwapType fixedLegType = SwapType.RECEIVE;

        double notional = 10.0 * 1_000_000;

        IborSwap swap = IborSwap.of(startDate,
                endDate,
                fixedLegType,
                fixedCoupon,
                fixedFreqType,
                fixedDayCountType,
                notional,
                floatSpread,
                floatFreqType,
                floatDayCountType,
                swapCalendarType,
                busDayAdjustType,
                dateGenRuleType);

        //Now perform a valuation after the swap has seasoned but with the
        //same curve being used for discounting and working out the implied
        //future Libor rates.

        LocalDate valuationDate = LocalDate.of(2018,11,30);
        LocalDate settlementDate = valuationDate.plusDays(2);
        IborCurve liborCurve = buildIborSingleCurve(valuationDate);
        double v = swap.value(settlementDate, liborCurve, Optional.of(liborCurve), Optional.of(firstFixing));

        double v_bbg = 388147.0;
        Assertions.assertEquals(v_bbg,v, 5000.0);

    }
}
