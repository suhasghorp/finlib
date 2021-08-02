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

public class IborCurveTest {

    @Test
    //This is an example of a replication of a BBG example from
    //https://github.com/vilen22/curve-building/blob/master/Bloomberg%20Curve%20Building%20Replication.xlsx

    public void BloombergPricingTest(){

        LocalDate valuationDate = LocalDate.of(2018,6,6);
        int spotDays = 0;
        LocalDate settlementDate = DateUtils.addWeekDays(valuationDate, spotDays);
        DayCountType depoDCCType = DayCountType.ACT_360;

        List<IborDeposit> depos = new ArrayList<>();
        LocalDate maturityDate = DateUtils.addMonths(settlementDate,3);
        depos.add(IborDeposit.of(settlementDate, maturityDate, 0.0231381, depoDCCType));

        List<IborFRA> fras = new ArrayList<>();
        fras.add(IborFuture.of(valuationDate, 1).toFRA(97.6675, -0.00005));
        fras.add(IborFuture.of(valuationDate, 2).toFRA(97.5200, -0.00060));
        fras.add(IborFuture.of(valuationDate, 3).toFRA(97.3550, -0.00146));
        fras.add(IborFuture.of(valuationDate, 4).toFRA(97.2450, -0.00263));
        fras.add(IborFuture.of(valuationDate, 5).toFRA(97.1450, -0.00411));
        fras.add(IborFuture.of(valuationDate, 6).toFRA(97.0750, -0.00589));

        spotDays = 2;
        settlementDate = DateUtils.addWeekDays(valuationDate,spotDays);

        List<IborSwap> swaps = new ArrayList<>();
        SwapType fixedLegType = SwapType.PAY;
        DayCountType accrual = DayCountType.THIRTY_E_360;
        FrequencyType freq = FrequencyType.SEMI_ANNUAL;

        double swapRate = 0.02776305;

        IborSwap swap = IborSwap.of(settlementDate, "2Y", fixedLegType, (2.77417+2.77844)/200, freq, accrual);swaps.add(swap);
        swap = IborSwap.of(settlementDate, "3Y", fixedLegType, (2.86098+2.86582)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "4Y", fixedLegType, (2.90240+2.90620)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "5Y", fixedLegType, (2.92944+2.92906)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "6Y", fixedLegType, (2.94001+2.94499)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "7Y", fixedLegType, (2.95352+2.95998)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "8Y", fixedLegType, (2.96830+2.97400)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "9Y", fixedLegType, (2.98403+2.98817)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "10Y", fixedLegType, (2.99716+3.00394)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "11Y", fixedLegType, (3.01344+3.01596)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "12Y", fixedLegType, (3.02276+3.02684)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "15Y", fixedLegType, (3.04092+3.04508)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "20Y", fixedLegType, (3.04417+3.05183)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "25Y", fixedLegType, (3.03219+3.03621)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "30Y", fixedLegType, (3.01030+3.01370)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "40Y", fixedLegType, (2.96946+2.97354)/200, freq, accrual); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "50Y", fixedLegType, (2.91552+2.93748)/200, freq, accrual); swaps.add(swap);

        IborCurve liborCurve = new IborCurve(valuationDate, Optional.ofNullable(null), depos, fras, swaps, InterpolationType.FLAT_FORWARD_RATES, true);

        double v = swaps.get(0).value(valuationDate, liborCurve, Optional.of(liborCurve), Optional.empty());
        double fixedv = -swaps.get(0).getFixedLeg().value(valuationDate, liborCurve);
        double floatv = swaps.get(0).getFloatLeg().value(valuationDate, liborCurve, liborCurve, Optional.empty());
        Assertions.assertEquals(0.0, Math.abs(v), 2.0);


        v = swaps.get(0).value(settlementDate, liborCurve, Optional.of(liborCurve), Optional.empty());
        fixedv = -swaps.get(0).getFixedLeg().value(settlementDate, liborCurve);
        floatv = swaps.get(0).getFloatLeg().value(settlementDate, liborCurve, liborCurve, Optional.empty());
        Assertions.assertEquals(0.0, Math.abs(v), 2.0);
    }

    @Test
    //Example from
    //https://blog.deriscope.com/index.php/en/excel-interest-rate-swap-price-dual-bootstrapping-curve
    public void DeriscopePricingTest(){
        double vBloomberg = 388147.0;
        LocalDate valuationDate = LocalDate.of(2018, 11, 30);
        LocalDate startDate = LocalDate.of(2017,12,27);
        LocalDate maturityDate = LocalDate.of(2067,12,27);
        double notional = 10 * 1_000_000;
        SwapType fixedLegType = SwapType.RECEIVE;

        double fixedRate = 0.0150;
        DayCountType fixedDCCType = DayCountType.THIRTY_360_BOND;
        FrequencyType fixedFreqType = FrequencyType.ANNUAL;
        double floatSpread = 0.0;
        DayCountType floatDCCType = DayCountType.ACT_360;
        FrequencyType floatFreqType = FrequencyType.SEMI_ANNUAL;

        IborSwap offMarketSwap = IborSwap.of(startDate, maturityDate, fixedLegType,
                fixedRate, fixedFreqType, fixedDCCType,
                notional, floatSpread, floatFreqType, floatDCCType, CalendarType.WEEKEND,DayAdjustType.FOLLOWING,DateGenRuleType.BACKWARD);

        InterpolationType interpType = InterpolationType.LINEAR_ZERO_RATES;

        //depos
        DayCountType depoDCCType = DayCountType.ACT_360;
        List<IborDeposit> depos = new ArrayList<>();
        int spotDays = 0;

        LocalDate settlementDate = DateUtils.addWeekDays(valuationDate,spotDays);
        depos.add(IborDeposit.of(settlementDate, "6M", -0.2510/100.0, depoDCCType));

        //FRAs
        DayCountType fraDCCType = DayCountType.ACT_360;
        List<IborFRA> fras = new ArrayList<>();
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"1M"), "6M", -0.2450/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"2M"), "6M", -0.2435/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"3M"), "6M", -0.2400/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"4M"), "6M", -0.2360/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"5M"), "6M", -0.2285/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"6M"), "6M", -0.2230/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"7M"), "6M", -0.2110/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"8M"), "6M", -0.1990/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"9M"), "6M", -0.1850/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"10M"), "6M", -0.1680/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"11M"), "6M", -0.1510/100.0, fraDCCType));
        fras.add(IborFRA.of(DateUtils.addTenor(settlementDate,"12M"), "6M", -0.1360/100.0, fraDCCType));

        //swaps
        List<IborSwap> swaps = new ArrayList<>();
        fixedLegType = SwapType.PAY;
        DayCountType accrual = DayCountType.THIRTY_360_BOND;
        FrequencyType freq = FrequencyType.ANNUAL;

        IborSwap swap = IborSwap.of(settlementDate, "2Y", fixedLegType, -0.1525/100.0, fixedFreqType, fixedDCCType);swaps.add(swap);
        swap = IborSwap.of(settlementDate, "3Y", fixedLegType, -0.0185/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "4Y", fixedLegType, 0.1315/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "5Y", fixedLegType, 0.2745/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "6Y", fixedLegType, 0.4135/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "7Y", fixedLegType, 0.5439/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "8Y", fixedLegType, 0.6652/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "9Y", fixedLegType, 0.7784/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "10Y", fixedLegType, 0.8799/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "11Y", fixedLegType, 0.9715/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "12Y", fixedLegType, 1.0517/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "15Y", fixedLegType, 1.2369/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "20Y", fixedLegType, 1.3965/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "25Y", fixedLegType, 1.4472/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "30Y", fixedLegType, 1.4585/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "35Y", fixedLegType, 1.4595/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "40Y", fixedLegType, 1.4535/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "45Y", fixedLegType, 1.4410/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);
        swap = IborSwap.of(settlementDate, "50Y", fixedLegType, 1.4335/100.0, fixedFreqType, fixedDCCType); swaps.add(swap);

        IborCurve liborCurve = new IborCurve(valuationDate, Optional.ofNullable(null), depos, fras, swaps, interpType, true);

        double v = offMarketSwap.value(valuationDate, liborCurve, Optional.of(liborCurve), Optional.of(-0.268/100.0));
        //double fixedv = -swaps.get(0).getFixedLeg().value(valuationDate, liborCurve);
        //double floatv = swaps.get(0).getFloatLeg().value(valuationDate, liborCurve, Optional.of(liborCurve), Optional.empty());
        Assertions.assertEquals(381271.3571003331, Math.abs(v), 10.0);

        List<IborDeposit> oisdepos = new ArrayList<>();
        oisdepos.add(IborDeposit.of(settlementDate, "1D", -0.3490/100.0, depoDCCType));

        List<IborFRA> oisfra = new ArrayList<>(); //empty

        fixedDCCType = DayCountType.ACT_365F;
        List<IborSwap> ois = new ArrayList<>();

        IborSwap oisswap = IborSwap.ofOIS(settlementDate, "2W", fixedLegType,-0.3600/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "1M", fixedLegType,-0.3560/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "2M", fixedLegType,-0.3570/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "3M", fixedLegType,-0.3580/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "4M", fixedLegType,-0.3575/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "5M", fixedLegType,-0.3578/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "6M", fixedLegType,-0.3580/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "7M", fixedLegType,-0.3600/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "8M", fixedLegType,-0.3575/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "9M", fixedLegType,-0.3569/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "10M", fixedLegType,-0.3553/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);        ;
        oisswap = IborSwap.ofOIS(settlementDate, "11M", fixedLegType,-0.3534/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "12M", fixedLegType,-0.3496/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "18M", fixedLegType,-0.3173/100.0, fixedFreqType, fixedDCCType);ois.add(oisswap);

        oisswap = IborSwap.ofOIS(settlementDate, "2Y", fixedLegType, -0.2671/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "30M", fixedLegType, -0.2070/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "3Y", fixedLegType, -0.1410/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "4Y", fixedLegType, -0.0060/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "5Y", fixedLegType, 0.1285/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "6Y", fixedLegType, 0.2590/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "7Y", fixedLegType, 0.3830/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "8Y", fixedLegType, 0.5020/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "9Y", fixedLegType, 0.6140/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "10Y", fixedLegType, 0.7160/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "11Y", fixedLegType, 0.8070/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "12Y", fixedLegType, 0.8890/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "15Y", fixedLegType, 1.0790/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "20Y", fixedLegType, 1.2460/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "25Y", fixedLegType, 1.3055/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "30Y", fixedLegType, 1.3270/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "35Y", fixedLegType, 1.3315/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "40Y", fixedLegType, 1.3300/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);
        oisswap = IborSwap.ofOIS(settlementDate, "50Y", fixedLegType, 1.3270/100.0, fixedFreqType, fixedDCCType); ois.add(oisswap);

        IborCurve oisCurve = new IborCurve(valuationDate, Optional.ofNullable(null), oisdepos, oisfra, ois, interpType, true);

        IborCurve dualCurve = new IborCurve(valuationDate, Optional.of(oisCurve), depos, fras, swaps, interpType, true);
        //387290.7706 swap rate = -0.0139905820518
        double v2 = offMarketSwap.value(valuationDate, dualCurve,Optional.of(oisCurve),  Optional.of(-0.268/100.0));

        Assertions.assertEquals(387290.7706, Math.abs(v2), 11.0);

        double swapRate = offMarketSwap.swapRate(valuationDate, dualCurve, Optional.of(oisCurve), Optional.of(-0.268/100.0));
        Assertions.assertEquals(-0.0139905820518, swapRate, 1e-4);

    }
}
