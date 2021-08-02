package com.finlib.market.credit;

import com.finlib.finutils.FinlibException;
import com.finlib.market.rates.IborCurve;
import com.finlib.market.rates.IborSwap;
import com.finlib.shared.DiscountCurve;
import com.finlib.shared.InterpolationType;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.math4.analysis.UnivariateFunction;
import org.apache.commons.math4.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math4.analysis.solvers.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public final class CreditCurve implements Serializable {
    private final LocalDate valuationDate;
    private final IborCurve liborCurve;
    private final List<CDS> cdsContracts;
    private final double recoveryRate;
    private final InterpolationType interpType;
    private DoubleArrayList times, survProbs;

    private CreditCurve(LocalDate valuationDate,
                        IborCurve liborCurve,
                        List<CDS> cdsContracts,
                        double recoveryRate,
                        InterpolationType interpType) {

        this.valuationDate = valuationDate;
        this.liborCurve = liborCurve;
        this.cdsContracts = cdsContracts;
        this.recoveryRate = recoveryRate;
        this.interpType = interpType;
        assert !cdsContracts.isEmpty() : "CDS contracts are needed for CreditCurve";
        assert valuationDate.equals(liborCurve.getValuationDate()) : "Valuation date of credit curve and Libor curve do not match";
        assert validate() : "CDS Contracts are not in increasing order";
        buildCurve();
    }

    public static CreditCurve of(LocalDate valuationDate,
                                 IborCurve liborCurve,
                                 List<CDS> cdsContracts){
        return new CreditCurve(valuationDate, liborCurve,cdsContracts,0.4,InterpolationType.FLAT_FORWARD_RATES);
    }

    public IborCurve getLiborCurve(){return liborCurve;}
    public DoubleArrayList getTimes(){return times;}
    public DoubleArrayList getSurvProbs(){return survProbs;}
    public List<CDS> getCdsContracts(){return cdsContracts;}

    private boolean validate() {
        //Ensure that contracts are in increasing maturity
        LocalDate matDate = cdsContracts.get(0).getMaturityDate();
        for (CDS cds : cdsContracts){
            if (cds.getMaturityDate().isBefore(matDate))
                return false;
            matDate = cds.getMaturityDate();
        }
        return true;
    }

    private static class CDSFunction implements UnivariateFunction {
        LocalDate valuationDate;
        private CreditCurve creditCurve;
        private CDS cds;
        CDSFunction(LocalDate valuationDate, CreditCurve creditCurve, CDS cds){
            this.valuationDate = valuationDate;
            this.creditCurve = creditCurve;
            this.cds = cds;
        }
        @Override
        public double value(double q) {
            int numPoints = creditCurve.times.size();
            creditCurve.survProbs.set(numPoints-1,q);
            double v_cds = cds.value(valuationDate,creditCurve,true,Optional.of(0.4), Optional.of(25))[1];
            return v_cds;
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


    public void buildCurve(){
        times = new DoubleArrayList(cdsContracts.size());
        survProbs = new DoubleArrayList(cdsContracts.size());
        times.add(0.0);
        survProbs.add(1.0);
        for (int i = 0; i < cdsContracts.size(); i++){
            LocalDate matDate = cdsContracts.get(i).getMaturityDate();
            double tmat = ChronoUnit.DAYS.between(valuationDate,matDate)/365.0;
            double q = survProbs.getDouble(i);
            times.add(tmat);
            survProbs.add(q);

            /*double it = q;
            boolean boundsFound = false;
            while (it > 0) {
                it -= 0.001;
                if (sign(new CreditCurve.CDSFunction(valuationDate,this,cdsContracts.get(i)).value(it)) != sign(new CreditCurve.CDSFunction(valuationDate,this,cdsContracts.get(i)).value(it - 0.001))) {
                    boundsFound = true;
                    break;
                }
            }
            if (!boundsFound)
                throw new FinlibException("Could not find bounds for minimization of CDS..check your inputs");*/

            BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
            q = solver.solve(100, new CreditCurve.CDSFunction(valuationDate,this,cdsContracts.get(i)), 0.001, q,  AllowedSolution.ABOVE_SIDE);



        }
    }









}
