package com.finlib.market.rates;

import com.finlib.finutils.FinlibException;
import com.finlib.shared.DiscountCurve;
import com.finlib.shared.InterpolationType;
import com.finlib.shared.Interpolator;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.math4.analysis.UnivariateFunction;
import org.apache.commons.math4.analysis.solvers.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class IborCurve extends DiscountCurve implements Serializable {

    private final List<IborDeposit> depos;
    private final List<IborFRA> fras;
    private final List<IborSwap> swaps;
    private final InterpolationType interpolationType;
    private final boolean checkRefit;
    private Optional<DiscountCurve> discountCurve;


    public IborCurve(LocalDate valuationDate, Optional<DiscountCurve> discountCurve, List<IborDeposit> depos, List<IborFRA> fras, List<IborSwap> swaps,
                     InterpolationType interpolationType, boolean checkRefit){
        this.valuationDate = valuationDate;
        this.discountCurve = discountCurve;
        this.depos = depos;
        this.fras = fras;
        this.swaps = swaps;
        this.interpolationType = interpolationType;
        this.checkRefit = checkRefit;
        this.interpolator = Interpolator.of(interpolationType);
        validateInputs();
        buildCurve();

    }
    /*public IborSingleCurve(LocalDate valuationDate, List<IborDeposit> depos, List<IborFRA> fras, List<IborSwap> swaps,
                           InterpolationType interpolationType, boolean checkRefit){
        this.valuationDate = valuationDate;
        this.discountCurve = Optional.ofNullable(null);
        this.depos = depos;
        this.fras = fras;
        this.swaps = swaps;
        this.interpolationType = interpolationType;
        this.checkRefit = checkRefit;
        this.interpolator = Interpolator.of(interpolationType);
        validateInputs();
        buildCurve();
        new IborSingleCurve(valuationDate, Optional.ofNullable(null), depos,fras,swaps,interpolationType,checkRefit);

    }*/

    public DoubleArrayList getTimes(){return times;}
    public DoubleArrayList getDiscFactors(){return dfs;}

    public IborCurve getBumpedCurve(double bump){
        List<IborDeposit> bumpedDepos = new ArrayList<>();
        List<IborFRA> bumpedFRAs = new ArrayList<>();
        List<IborSwap> bumpedSwaps = new ArrayList<>();
        for (IborDeposit depo : depos){
            bumpedDepos.add(depo.bump(bump));
        }
        for (IborFRA fra : fras){
            bumpedFRAs.add(fra.bump(bump));
        }
        for (IborSwap swap : swaps){
            bumpedSwaps.add(swap.bump(bump));
        }
        return new IborCurve(valuationDate, discountCurve, bumpedDepos, bumpedFRAs, bumpedSwaps,
                interpolationType, checkRefit);
    }

    private void checkRefit(){
        for (IborDeposit depo : depos){
            double v = depo.value(valuationDate, this) / depo.getNotional();
            if (Math.abs(v - 1.0) > 1e-10)
                throw new FinlibException("Deposit not repriced with maturity - " + depo.getMaturityDate());
        }
        for (IborFRA fra : fras){
            double v = fra.value(valuationDate, this, discountCurve) / fra.getNotional();
            if (Math.abs(v) > 1e-5)
                throw new FinlibException("FRA not repriced with maturity - " + fra.getMaturityDate());
        }
        for (IborSwap swap : swaps){
            double v = swap.value(swap.getEffectiveDate(), this, discountCurve, Optional.empty());
            v = v / swap.getFixedLeg().getNotional();
            if (Math.abs(v) > 1e-5)
                throw new FinlibException("Swap not repriced with maturity - " + swap.getMaturityDate());
        }
    }

    private void validateInputs(){
        LocalDate lastDepositMaturityDate = LocalDate.of(1900,1,1);
        LocalDate firstFRAMaturityDate = LocalDate.of(1900,1,1);
        LocalDate lastFRAMaturityDate = LocalDate.of(1900,1,1);
        LocalDate firstSwapMaturityDate = LocalDate.of(1900,1,1);

        if (depos.size() > 0)
            lastDepositMaturityDate = depos.get(depos.size()-1).getMaturityDate();

        if (fras.size() > 0) {
            firstFRAMaturityDate = fras.get(0).getMaturityDate();
            lastFRAMaturityDate = fras.get(fras.size()-1).getMaturityDate();
        }

        if (swaps.size() > 0)
            firstSwapMaturityDate = swaps.get(0).getMaturityDate();

        if (depos.size() > 0 && fras.size() > 0) {
            if (firstFRAMaturityDate.isBefore(lastDepositMaturityDate) || firstFRAMaturityDate.equals(lastDepositMaturityDate)) {
                throw new FinlibException("First FRA must end after last Deposit");
            }
        }

        if (fras.size() > 0 && swaps.size() > 0) {
            if (firstSwapMaturityDate.isBefore(lastFRAMaturityDate) || firstSwapMaturityDate.equals(lastFRAMaturityDate))
                throw new FinlibException("First Swap must mature after last FRA ends");
        }

        // If both depos and swaps start after T, we need a rate to get them to
        // the first deposit. So we create a synthetic deposit rate contract.

        if (swaps.get(0).getEffectiveDate().isAfter(valuationDate)) {

            if (depos.size() == 0)
                throw new FinlibException("Need a deposit rate to pin down short end.");

            if (depos.get(0).getStartDate().isAfter(valuationDate)) {
                IborDeposit firstDepo = depos.get(0);
                if (firstDepo.getStartDate().isAfter(valuationDate)) {
                    IborDeposit syntheticDeposit = IborDeposit.of(valuationDate,firstDepo.getStartDate(),
                    firstDepo.getDepositRate(), firstDepo.getDayCountType(), firstDepo.getNotional(),
                            firstDepo.getCalendarType(),firstDepo.getDayAdjustType());
                    depos.set(0, syntheticDeposit);
                }
            }
        }
    }

    private static class FRAFunction implements UnivariateFunction {
        private Optional<DiscountCurve> discountCurve;
        private DiscountCurve indexCurve;
        private IborFRA fra;
        FRAFunction(DiscountCurve indexCurve, Optional<DiscountCurve> discountCurve, IborFRA fra){
            this.indexCurve = indexCurve;
            this.discountCurve = discountCurve;
            this.fra = fra;
        }
        @Override
        public double value(double v) {
            int numPoints = indexCurve.times.size();
            indexCurve.dfs.set(numPoints-1,v);
            // For curves that need a fit function, we fit it now
            indexCurve.getInterpolator().fit(indexCurve.times, indexCurve.dfs);
            double v_fra = fra.value(indexCurve.getValuationDate(), indexCurve, discountCurve);
            v_fra /= fra.getNotional();
            return v_fra;
        }
    }

    private static class SwapFunction implements UnivariateFunction {
        private DiscountCurve indexCurve;
        private Optional<DiscountCurve> discountCurve;
        private IborSwap swap;
        SwapFunction(DiscountCurve indexCurve, Optional<DiscountCurve> discountCurve, IborSwap swap){
            this.indexCurve = indexCurve;
            this.discountCurve = discountCurve;
            this.swap = swap;
        }
        @Override
        public double value(double v) {
            int numPoints = indexCurve.times.size();
            indexCurve.dfs.set(numPoints-1,v);
            indexCurve.getInterpolator().fit(indexCurve.times, indexCurve.dfs);
            double v_swap = swap.value(indexCurve.getValuationDate(), indexCurve, discountCurve, Optional.ofNullable(null));
            double notional = swap.getFixedLeg().getNotional();
            v_swap /= notional;
            return v_swap;
        }
    }

    private static int sign(double x) {
        if (x < 0.0)
            return -1;
        else if (x > 0.0)
            return 1;
        else
            return 0;
    }

    private void buildCurve() {
            times.add(0.0);
            dfs.add(1.0);
            interpolator.fit(new DoubleArrayList(times), new DoubleArrayList(dfs));
            double oldTMat = 0.0;
            double dfMat = 1.0;
            for (IborDeposit depo : depos) {
                double dfSettle = df(depo.getStartDate());
                dfMat = depo.maturityDf() * dfSettle;
                double tmat = ChronoUnit.DAYS.between(valuationDate, depo.getMaturityDate()) / 365.0;
                oldTMat = tmat;
                times.add(tmat);
                dfs.add(dfMat);
                interpolator.fit(times, dfs);
            }
            for (IborFRA fra : fras) {
                double tset = ChronoUnit.DAYS.between(valuationDate, fra.getStartDate()) / 365.0;
                double tmat = ChronoUnit.DAYS.between(valuationDate, fra.getMaturityDate()) / 365.0;

                if (tset < oldTMat && tmat > oldTMat) {
                    dfMat = fra.maturityDf(this);
                    times.add(tmat);
                    dfs.add(dfMat);
                } else {
                    times.add(tmat);
                    dfs.add(dfMat);

                    BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
                    dfMat = solver.solve(100, new FRAFunction(this,discountCurve, fra), 0.001, 1.2, AllowedSolution.ABOVE_SIDE);
                }
                interpolator.fit(times, dfs);
            }

            for (IborSwap swap : swaps){
                //I use the lastPaymentDate in case a date has been adjusted fwd
                //over a holiday as the maturity date is usually not adjusted CHECK
                LocalDate maturityDate = swap.getFixedLeg().getLastPaymentDate();
                double tmat = ChronoUnit.DAYS.between(valuationDate, maturityDate) / 365.0;
                times.add(tmat);
                dfs.add(dfMat);

                BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
                dfMat = solver.solve(10000, new SwapFunction(this,discountCurve,swap), 0.001, 1.2, AllowedSolution.ABOVE_SIDE);
                //BrentSolver solver = new BrentSolver();
                //dfMat = solver.solve(100, new SwapFunction(this,discountCurve,swap), 0.001, 1.2, dfMat);
            }
            interpolator.fit(times, dfs);
            checkRefit();
        }



    }


