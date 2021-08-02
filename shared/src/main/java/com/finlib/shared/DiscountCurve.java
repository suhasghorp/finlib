package com.finlib.shared;

import com.finlib.finutils.*;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


public class DiscountCurve {

    public DoubleArrayList times = new DoubleArrayList();
    public DoubleArrayList dfs = new DoubleArrayList();
    private InterpolationType interpType;
    private final FrequencyType freqType = FrequencyType.CONTINUOUS;
    protected LocalDate valuationDate;
    private DayCountType dayCountType;
    protected Interpolator interpolator;

    public DiscountCurve(){}

    public DiscountCurve(LocalDate valuationDate, DoubleArrayList dfTimes, DoubleArrayList dfValues, InterpolationType interType){
        int numPoints = dfTimes.size();
        if (numPoints < 1)
            throw new RuntimeException("Times not provided");
        if (numPoints != dfValues.size())
            throw new RuntimeException("Times and discount factors provided have a mismatch");
        int startIndex = 0;
        if (numPoints > 0) {
            if (dfTimes.getDouble(0) == 0.0) {
                dfs.add(dfValues.getDouble(0));
                startIndex = 1;
            }
        }
        times.add(0.0);
        dfs.add(1.0);
        for (int i = startIndex; i < numPoints; i++) {
            times.add(dfTimes.getDouble(i));
            dfs.add(dfValues.getDouble(i));
        }
        if (testMonotonicity() == false)
            throw new RuntimeException("Times are not sorted in increasing order");
        this.valuationDate = valuationDate;
        this.dfs = dfValues;
        this.interpType = interType;
        this.dayCountType = null;
        this.interpolator = Interpolator.of(interpType,times,dfs);
    }

    public DiscountCurve(LocalDate valuationDate, List<LocalDate> dfDates, DoubleArrayList dfValues, InterpolationType interType) {
        int numPoints = dfDates.size();
        if (numPoints < 1)
            throw new RuntimeException("Dates not provided");
        if (numPoints != dfValues.size())
            throw new RuntimeException("Dates and discount factors provided have a mismatch");
        int startIndex = 0;
        if (numPoints > 0) {
            if (dfDates.get(0) == valuationDate) {
                dfs.add(dfValues.getDouble(0));
                startIndex = 1;
            }
        }
        times.add(0.0);
        dfs.add(1.0);
        for (int i = startIndex; i < numPoints; i++) {
            double t = ChronoUnit.DAYS.between(valuationDate, dfDates.get(i)) / 365.0;
            times.add(t);
            dfs.add(dfValues.getDouble(i));
        }
        if (testMonotonicity() == false)
            throw new RuntimeException("Times are not sorted in increasing order");
        this.valuationDate = valuationDate;
        this.dfs = dfValues;
        this.interpType = interType;
        this.dayCountType = null;
        this.interpolator = Interpolator.of(interpType,times,dfs);

    }

    public double df(LocalDate dt){
        double yearFrac = new DayCount(dayCountType).yearFrac(valuationDate,dt, Optional.empty());
        return interpolator.interpolate(yearFrac);
    }
    public double df(double dt){
        return interpolator.interpolate(dt);
    }

    private double zeroToDF(double rate, double yearFrac, FrequencyType freqType){
        double dt = Math.max(yearFrac,1e-10);
        int f = freqType.getFrequency();
        double df = 0.0;
        if (freqType == FrequencyType.CONTINUOUS)
            df = Math.exp(-rate * dt);
        else if (freqType == FrequencyType.SIMPLE)
            df = 1.0 / (1.0 + rate * dt);
        else if (freqType == FrequencyType.ANNUAL ||
                freqType == FrequencyType.SEMI_ANNUAL ||
                freqType == FrequencyType.QUARTERLY ||
                freqType == FrequencyType.MONTHLY)
            df = 1.0 / Math.pow(1.0 + rate/f, f * dt);
        return df;
    }

    private double dfToZero(double df, LocalDate maturityDate, FrequencyType freqType, DayCountType dayCountType ){
        double yearFrac = new DayCount(dayCountType).yearFrac(valuationDate,maturityDate, Optional.empty());
        if (freqType == FrequencyType.CONTINUOUS)
            return -Math.log(df)/yearFrac;
        else if (freqType == FrequencyType.SIMPLE)
            return (1.0/df - 1.0)/yearFrac;
        else return (Math.pow(df, -1.0/(yearFrac * freqType.getFrequency()))-1.0) * freqType.getFrequency();
    }

    public double zeroRate(LocalDate maturityDate, FrequencyType freqType, DayCountType dayCountType){
        double discFact = df(maturityDate);
        return dfToZero(discFact,maturityDate,freqType,dayCountType);
    }
    private double ccRate(LocalDate maturityDate, DayCountType dayCountType){
        return zeroRate(maturityDate,FrequencyType.CONTINUOUS,dayCountType);
    }
    private double swapRate(LocalDate effectiveDate, LocalDate maturityDate, FrequencyType freqType, DayCountType dayCountType){
        if (effectiveDate.isBefore(valuationDate))
            throw new RuntimeException("Swap starts before the curve valuation date.");
        if (freqType == FrequencyType.SIMPLE)
            throw new RuntimeException("Cannot calculate par rate with simple yield freq.");
        if (freqType == FrequencyType.CONTINUOUS)
            throw new RuntimeException("Cannot calculate par rate with continuous freq.");
        if (maturityDate.isBefore(effectiveDate))
            throw new RuntimeException("Maturity date is before the swap start date.");
        Schedule schedule = new Schedule.Builder(effectiveDate,maturityDate).withFrequency(freqType).build();
        List<LocalDate> flowDates = new ArrayList<>();
        flowDates.add(effectiveDate);
        flowDates.addAll(schedule.getAdjustedDates());
        DayCount dc = new DayCount(dayCountType);
        LocalDate prevDt = flowDates.get(0);
        double pv01 = 0.0, discFact = 0.0;
        for(LocalDate nextDt : flowDates.subList(1,flowDates.size())){
            discFact = df(nextDt);
            double alpha = dc.yearFrac(prevDt, nextDt);
            pv01 += alpha * discFact;
            prevDt = nextDt;
        }
        if (Math.abs(pv01) < 1e-10)
            return 0.0;

        double dfStart = df(effectiveDate);
        return (dfStart - discFact) / pv01;
    }

    public double fwd(LocalDate dt){
        double df1 = df(dt);
        double df2 = df(dt.plusDays(1));
        double yearFrac = 1.0/365.0;
        return Math.log(df1/df2)/(1.0*yearFrac);
    }

    private double fwd(double dt){
        double bump = 1e-6;
        double times = Math.max(dt, bump);
        double df1 = df(times - dt);
        double df2 = df(times + dt);
        return Math.log(df1/df2)/(2.0*dt);
    }

    private DiscountCurve bump(double bumpSize){
        DoubleArrayList dfsCopy = dfs.clone();
        DoubleArrayList timesCopy = times.clone();
        for(int i = 0; i < dfsCopy.size();i++){
            dfsCopy.set(i, dfsCopy.getDouble(i) * Math.exp(-bumpSize * timesCopy.getDouble(i)));
        }
        return new DiscountCurve(valuationDate,dfsCopy,timesCopy,interpType);
    }

    private double fwdRate(LocalDate startDate, LocalDate maturityDate,DayCountType dayCountType){
        double yearFrac = new DayCount(dayCountType).yearFrac(startDate,maturityDate);
        double df1 = df(startDate);
        double df2 = df(maturityDate);
        return (df1 / df2 - 1.0) / yearFrac;
    }
    private double fwdRate(LocalDate startDate, String tenor,DayCountType dayCountType){
        LocalDate maturityDate = DateUtils.addTenor(startDate,tenor);
        return fwdRate(startDate,maturityDate,dayCountType);
    }

    public Interpolator getInterpolator(){
        return interpolator;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    private boolean testMonotonicity(){
        return IntStream.range(1, times.size()).reduce(0, (acc, e) -> acc + (times.getDouble(e - 1) <= times.getDouble(e) ? 0 : 1)) == 0;
    }



}
