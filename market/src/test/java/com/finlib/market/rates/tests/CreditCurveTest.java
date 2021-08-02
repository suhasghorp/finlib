package com.finlib.market.rates.tests;

import com.finlib.finutils.DateUtils;
import com.finlib.finutils.DayCountType;
import com.finlib.finutils.FrequencyType;
import com.finlib.market.credit.CDS;
import com.finlib.market.credit.CreditCurve;
import com.finlib.market.rates.*;
import com.finlib.shared.DiscountCurve;
import com.finlib.shared.InterpolationType;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreditCurveTest {

    @Test
    public void curveTest(){
        LocalDate curveDate = LocalDate.of(2018,12,20);

        List<IborSwap> swaps = new ArrayList<>();
        List<IborDeposit> depos = new ArrayList<>();
        List<IborFRA> fras = new ArrayList<>();

        DayCountType fixedDCC = DayCountType.ACT_365F;
        FrequencyType fixedFreq = FrequencyType.SEMI_ANNUAL;
        double fixedCoupon = 0.05;

        for (int i = 1; i < 11; i++){
            LocalDate matDate = DateUtils.addMonths(curveDate, 12*i);
            IborSwap swap = IborSwap.of(curveDate,matDate, SwapType.PAY,fixedCoupon,fixedFreq,fixedDCC);
            swaps.add(swap);
        }
        IborCurve liborCurve = new IborCurve(curveDate, Optional.empty(), depos, fras, swaps, InterpolationType.FLAT_FORWARD_RATES, true);
        List<CDS> cdsContracts = new ArrayList<>();
        for (int i = 1; i < 11; i++){
            LocalDate matDate = DateUtils.addMonths(curveDate, 12*i);
            CDS cds = CDS.of(curveDate,matDate,0.005 + 0.001 * (i - 1));
            cdsContracts.add(cds);
        }

        CreditCurve creditCurve = CreditCurve.of(curveDate,liborCurve,cdsContracts);
        DoubleArrayList times = creditCurve.getTimes();
        DoubleArrayList survProbs = creditCurve.getSurvProbs();
        for (int i = 0; i < times.size();i++){
            System.out.println(times.getDouble(i) + " - " + survProbs.getDouble(i));
        }



    }
}
