package com.finlib.market.credit;

import com.finlib.finutils.*;
import com.finlib.market.rates.IborCurve;
import com.finlib.market.rates.IborDeposit;
import com.finlib.shared.InterpolationType;
import com.finlib.shared.Interpolator;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.math4.util.FastMath;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CDS {
    private final LocalDate stepInDate;
    private final LocalDate maturityDate;
    private double coupon;
    private final double notional;
    private final boolean longProtection;
    private final FrequencyType freqType;
    private final DayCountType dayCountType;
    private final CalendarType calendarType;
    private final DayAdjustType dayAdjustType;
    private final DateGenRuleType dateGenRuleType;
    private List<LocalDate> adjDates = null;
    private DoubleArrayList accrualFactors = new DoubleArrayList();
    private DoubleArrayList flows = new DoubleArrayList();


    private CDS(LocalDate stepInDate,
               LocalDate maturityDate,
               double coupon,
               double notional,
               boolean longProtection,
               FrequencyType freqType,
               DayCountType dayCountType,
               CalendarType calendarType,
               DayAdjustType dayAdjustType,
               DateGenRuleType dateGenRuleType
               ) {
        this.stepInDate = stepInDate;
        this.maturityDate = maturityDate;
        this.coupon = coupon;
        this.notional = notional;
        this.longProtection = longProtection;
        this.freqType = freqType;
        this.dayCountType = dayCountType;
        this.calendarType = calendarType;
        this.dayAdjustType = dayAdjustType;
        this.dateGenRuleType = dateGenRuleType;

        genAdjustedCDSDates();
        genPremiumLegCashFlows();

    }

    public static CDS of(LocalDate stepInDate,
                         LocalDate maturityDate,
                         double coupon,
                         double notional,
                         boolean longProtection,
                         FrequencyType freqType,
                         DayCountType dayCountType,
                         CalendarType calendarType,
                         DayAdjustType dayAdjustType,
                         DateGenRuleType dateGenRuleType) {

        return new CDS(stepInDate, maturityDate,coupon, notional, longProtection, freqType,
                dayCountType,calendarType, dayAdjustType, dateGenRuleType);
    }

    public static CDS of(LocalDate stepInDate,
               String tenor,
               double coupon,
               double notional,
               boolean longProtection,
               FrequencyType freqType,
               DayCountType dayCountType,
               CalendarType calendarType,
               DayAdjustType dayAdjustType,
               DateGenRuleType dateGenRuleType) {

        LocalDate maturityDate = DateUtils.addTenor(stepInDate, tenor);
        maturityDate = DateUtils.nextCDSDate(maturityDate, Optional.ofNullable(null));
        return new CDS(stepInDate, maturityDate,coupon, notional, longProtection, freqType,
        dayCountType,calendarType, dayAdjustType, dateGenRuleType);
    }

    public static CDS of(LocalDate stepInDate,
                         LocalDate maturityDate,
                         double coupon) {

        return new CDS(stepInDate, maturityDate,coupon, 1_000_000, true, FrequencyType.QUARTERLY,
                DayCountType.ACT_360,CalendarType.WEEKEND, DayAdjustType.FOLLOWING, DateGenRuleType.BACKWARD);
    }

    private void genAdjustedCDSDates(){
        int frequency = freqType.getFrequency();
        Calendar calendar = new Calendar(calendarType);
        LocalDate startDate = stepInDate;
        LocalDate endDate = maturityDate;

        List<LocalDate> unadjDates = new ArrayList<>();
        int numMonths = 12/frequency;

        if (dateGenRuleType == DateGenRuleType.BACKWARD) {
            LocalDate nextDate = endDate;
            while (nextDate.isAfter(startDate)) {
                unadjDates.add(nextDate);
                nextDate = DateUtils.addMonths(nextDate, -numMonths);
            }
            unadjDates.add(nextDate);
            adjDates = unadjDates.stream().map(x -> calendar.adjust(x, dayAdjustType)).sorted().collect(Collectors.toList());
            int lastIndex = adjDates.size()-1;
            adjDates.set(lastIndex, adjDates.get(lastIndex).plusDays(1));
            //adjDates.add(nextDate.plusDays(1));
        } else if (dateGenRuleType == DateGenRuleType.FORWARD) {
            LocalDate nextDate = startDate;
            unadjDates.add(nextDate);
            while (nextDate.isBefore(endDate)) {
                unadjDates.add(nextDate);
                nextDate = DateUtils.addMonths(nextDate, numMonths);
            }
            unadjDates.add(nextDate);
            adjDates = unadjDates.stream().map(x -> calendar.adjust(x, dayAdjustType)).sorted().collect(Collectors.toList());
            int lastIndex = adjDates.size()-1;
            adjDates.set(lastIndex, adjDates.get(lastIndex).plusDays(1));
        }
    }

    private void genPremiumLegCashFlows(){
        DayCount dayCount = new DayCount(dayCountType);
        accrualFactors.add(0.0);
        flows.add(0.0);
        for (int it = 1; it < adjDates.size(); it++){
            LocalDate t0 = adjDates.get(it - 1);
            LocalDate t1 = adjDates.get(it);
            double accFactor = dayCount.yearFrac(t0, t1);
            accrualFactors.add(accFactor);
            flows.add(accFactor * coupon * notional);
        }
    }

    public LocalDate getMaturityDate(){return maturityDate;}

    private double[] riskyPV01(LocalDate valuationDate, CreditCurve creditCurve){

        int couponAccruedIndicator = 1;
        boolean useFlatHazardRateIntegral = true;

        /*The risky_pv01 is the present value of a risky one dollar paid on
        the premium leg of a CDS contract.*/
        IborCurve liborCurve = creditCurve.getLiborCurve();
        DoubleArrayList paymentTimes = adjDates.stream().map(x -> ChronoUnit.DAYS.between(valuationDate,x)/365.0).
                collect(Collectors.toCollection(DoubleArrayList::new));
        /*this is the part of the coupon accrued from the previous coupon date
        to now*/
        LocalDate pcd = adjDates.get(0);
        LocalDate eff = stepInDate;
        DayCount dayCount = new DayCount(dayCountType);
        double accrual_factorPCDToNow = dayCount.yearFrac(eff, pcd);

        DoubleArrayList yearFracs = accrualFactors;
        double teff = ChronoUnit.DAYS.between(valuationDate,eff) / 365.0;

        Interpolator qinterp = Interpolator.of(InterpolationType.FLAT_FORWARD_RATES,creditCurve.getTimes(), creditCurve.getSurvProbs());
        Interpolator zinterp = Interpolator.of(InterpolationType.FLAT_FORWARD_RATES,liborCurve.getTimes(),liborCurve.getDiscFactors());

        /*The first coupon is a special case which needs to be handled carefully
        taking into account what coupon has already accrued and what has not*/

        double qeff = qinterp.interpolate(teff);
        double q1 = qinterp.interpolate(paymentTimes.getDouble(1));
        double z1 = zinterp.interpolate(paymentTimes.getDouble(1));

        /*reference credit survives to the premium payment date*/
        double fullRPV01 = q1 * z1 * yearFracs.getDouble(1);

        /*coupon accrued from previous coupon to today paid in full at default
        before coupon payment*/

        fullRPV01 = fullRPV01 + z1 * (qeff - q1) * accrual_factorPCDToNow * couponAccruedIndicator;

        /*future accrued from now to coupon payment date assuming default roughly
        midway*/

        fullRPV01 += 0.5 * z1 *
                 (qeff - q1) * (yearFracs.getDouble(1) - accrual_factorPCDToNow) * couponAccruedIndicator;

        for (int it = 2; it < paymentTimes.size(); it++){
            double t2 = paymentTimes.getDouble(it);
            double q2 = qinterp.interpolate(t2);
            double z2 = zinterp.interpolate(t2);
            double accrualFactor = yearFracs.getDouble(it);
            /*full coupon is paid at the end of the current period if survives to
            payment date*/
            fullRPV01 += q2 * z2 * accrualFactor;
            double dfullRPV01 = 0.0;
            if (couponAccruedIndicator == 1){
                if (useFlatHazardRateIntegral){
                    double tau = accrualFactor;
                    double h12 = -FastMath.log(q2 / q1) / tau;
                    double r12 = -FastMath.log(z2 / z1) / tau;
                    double alpha = h12 + r12;
                    double expTerm = 1.0 - FastMath.exp(-alpha * tau) - alpha * tau * FastMath.exp(-alpha * tau);
                    dfullRPV01 = q1 * z1 * h12 * expTerm / FastMath.abs(alpha * alpha + 1e-20);
                } else {
                    dfullRPV01 = 0.50 * (q1 - q2) * z2 * accrualFactor;
                }
                fullRPV01 = fullRPV01 + dfullRPV01;
            }
            q1 = q2;
        }
        double cleanRPV01 = fullRPV01 - accrual_factorPCDToNow;
        return new double[]{fullRPV01, cleanRPV01};
    }

    public double protectionLegPV(LocalDate valuationDate, CreditCurve creditCurve, boolean useHazardRateIntegral,
                                  Optional<Double> recoveryRate, Optional<Integer> numStepsPerYear){
        int numSteps = numStepsPerYear.orElse(25);
        double recRate = recoveryRate.orElse(0.4);
        double teff = ChronoUnit.DAYS.between(valuationDate,stepInDate)/365.0;
        double tmat = ChronoUnit.DAYS.between(valuationDate,maturityDate)/365.0;
        double dt = (tmat - teff) / numSteps;
        double t = teff;
        IborCurve liborCurve = creditCurve.getLiborCurve();
        Interpolator qinterp = Interpolator.of(InterpolationType.FLAT_FORWARD_RATES,creditCurve.getTimes(), creditCurve.getSurvProbs());
        Interpolator zinterp = Interpolator.of(InterpolationType.FLAT_FORWARD_RATES,liborCurve.getTimes(),liborCurve.getDiscFactors());
        double z1 = zinterp.interpolate(t);
        double q1 = qinterp.interpolate(t);
        double protectionPV = 0.0;
        if (useHazardRateIntegral){
            for (int i = 0; i < numSteps; i++){
                t = t + dt;
                double z2 = zinterp.interpolate(t);
                double q2 = qinterp.interpolate(t);
                double h12 = -FastMath.log(q2 / q1) / dt;
                double r12 = -FastMath.log(z2 / z1) / dt;
                double expTerm = FastMath.exp(-(r12 + h12) * dt);
                double dprot_pv = h12 * (1.0 - expTerm) * q1 * z1 / (Math.abs(h12 + r12) + 1e-8);
                protectionPV += dprot_pv;
                q1 = q2;
                z1 = z2;
            }
        } else {
            for (int i = 0; i < numSteps; i++) {
                t = t + dt;
                double z2 = zinterp.interpolate(t);
                double q2 = qinterp.interpolate(t);
                double dq = q1 - q2;
                double dprot_pv = 0.5 * (z1 + z2) * dq;
                protectionPV += dprot_pv;
                q1 = q2;
                z1 = z2;
            }
        }
        protectionPV = protectionPV * (1.0 - recRate);
        return protectionPV * notional;
    }

    public double[] value(LocalDate valuationDate, CreditCurve creditCurve,boolean useHazardRateIntegral,
                           Optional<Double> recoveryRate, Optional<Integer> numStepsPerYear){

        /*Valuation of a CDS contract on a specific valuation date given
        an issuer curve and a contract recovery rate.*/

        double[] rpv01 = riskyPV01(valuationDate,creditCurve);
        double fullRPV01 = rpv01[0];
        double cleanRPV01 = rpv01[1];

        double protectionPV = protectionLegPV(valuationDate,creditCurve,true,Optional.of(0.4), Optional.of(25));

        double fwdDf = 1.0;
        int longProt = longProtection ? 1 : -1;

        double fullPV = fwdDf * longProt * (protectionPV - coupon * fullRPV01 * notional);
        double cleanPV = fwdDf * longProt * (protectionPV - coupon * cleanRPV01 * notional);

        return new double[]{fullPV,cleanPV};
    }

    public double creditDV01(LocalDate valuationDate, CreditCurve creditCurve,boolean useHazardRateIntegral,
                             Optional<Double> recoveryRate, Optional<Integer> numStepsPerYear){
        /*Calculation of the change in the value of the CDS contract for a
        one basis point change in the level of the CDS curve.*/
        //full PV
        double v0 = value(valuationDate, creditCurve,useHazardRateIntegral,recoveryRate, numStepsPerYear)[0];
        double bump = 0.0001; //1 bp
        CreditCurve bumpedCurve = SerializationUtils.clone(creditCurve);
        for (CDS cds : bumpedCurve.getCdsContracts()){
            cds.coupon += bump;
        }
        bumpedCurve.buildCurve();
        double v1 = value(valuationDate, bumpedCurve,useHazardRateIntegral,recoveryRate, numStepsPerYear)[0];
        return v1 - v0;
    }

    public double interestDV01(LocalDate valuationDate, CreditCurve creditCurve,boolean useHazardRateIntegral,
                             Optional<Double> recoveryRate, Optional<Integer> numStepsPerYear){
    /*Calculation of the interest DV01 based on a simple bump of
        the discount factors and reconstruction of the CDS curve.*/
        double v0 = value(valuationDate, creditCurve,useHazardRateIntegral,recoveryRate, numStepsPerYear)[0];
        double bump = 0.0001; //1 bp
        CreditCurve newCreditCurve = SerializationUtils.clone(creditCurve);
        IborCurve bumpedCurve = newCreditCurve.getLiborCurve();
        List<IborDeposit> depos = bumpedCurve.getDepos();
        for (IborDeposit depo : depos){
            depo.depo
        }

    }




}
