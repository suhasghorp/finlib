package com.finlib.market.curves;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.List;
import java.util.stream.Collectors;

public class Interpolator {
    private InterpolationType interpType;
    private DoubleArrayList times ;
    private DoubleArrayList dfs ;

    private Interpolator(InterpolationType interpType,DoubleArrayList times,DoubleArrayList dfs){
        this.interpType = interpType;
        this.times = times;
        this.dfs = dfs;
    }

    public static Interpolator of(InterpolationType interpType,DoubleArrayList times,DoubleArrayList dfs){
        return new Interpolator(interpType,times,dfs);
    }

    public Interpolator(InterpolationType method){
        interpType = method;
    }

    /*public static List<Double> pinterpolate(List<Double> t, List<Double> times, List<Double> dfs, InterpolationTypes method){
        return t.stream().map(x -> interpolate(x, times, dfs, method)).collect(Collectors.toList());
    }*/

    public double interpolate(double t) {
        double small = 1e-10;
        int numPoints = times.size();

        if (t == times.getDouble(0))
            return dfs.getDouble(0);

        int i = 0;
        while (times.getDouble(i) < t && i < numPoints - 1)
            i = i + 1;

        if (t > times.getDouble(i))
            i = numPoints;

        double yvalue = 0.0;

        //linear interpolation of y(x)

        if (interpType == InterpolationType.LINEAR_ZERO_RATES) {

            if (i == 1) {
                double r1 = -Math.log(dfs.getDouble(i)) / times.getDouble(i);
                double r2 = -Math.log(dfs.getDouble(i)) / times.getDouble(i);
                double dt = times.getDouble(i) - times.getDouble(i-1);
                double rvalue = ((times.getDouble(i) - t) * r1 + (t - times.getDouble(i-1) * r2)) / dt;
                yvalue = Math.exp(-rvalue * t);
            } else if (i < numPoints) {
                double r1 = -Math.log(dfs.getDouble(i-1)) / times.getDouble(i-1);
                double r2 = -Math.log(dfs.getDouble(i)) / times.getDouble(i);
                double dt = times.getDouble(i) - times.getDouble(i-1);
                double rvalue = ((times.getDouble(i) - t) * r1 + (t - times.getDouble(i-1)) * r2) / dt;
                yvalue = Math.exp(-rvalue * t);
            } else {
                double r1 = -Math.log(dfs.getDouble(i-1)) / times.getDouble(i-1);
                double r2 = -Math.log(dfs.getDouble(i-1)) / times.getDouble(i-1);
                double dt = times.getDouble(i-1) - times.getDouble(i-2);
                double rvalue = ((times.getDouble(i-1) - t) * r1 + (t - times.getDouble(i-2)) * r2) / dt;
                yvalue = Math.exp(-rvalue * t);
            }
        } else if (interpType == InterpolationType.FLAT_FORWARD_RATES) {
            //linear interpolation of log(y(x)) which means the linear interpolation of
            //continuously compounded zero rates in the case of discount curves
            //This is also FLAT FORWARDS
            if (i == 1) {
                double rt1 = -Math.log(dfs.getDouble(i - 1));
                double rt2 = -Math.log(dfs.getDouble(i));
                double dt = times.getDouble(i) - times.getDouble(i - 1);
                double rtvalue = ((times.getDouble(i) - t) * rt1 + (t - times.getDouble(i - 1)) * rt2) / dt;
                yvalue = Math.exp(-rtvalue);
            } else if (i < numPoints) {
                double rt1 = -Math.log(dfs.getDouble(i - 1));
                double rt2 = -Math.log(dfs.getDouble(i));
                double dt = times.getDouble(i) - times.getDouble(i - 1);
                double rtvalue = ((times.getDouble(i) - t) * rt1 + (t - times.getDouble(i - 1)) * rt2) / dt;
                yvalue = Math.exp(-rtvalue);
            } else {
                double rt1 = -Math.log(dfs.getDouble(i - 2));
                double rt2 = -Math.log(dfs.getDouble(i - 1));
                double dt = times.getDouble(i - 1) - times.getDouble(i - 2);
                double rtvalue = ((times.getDouble(i - 1) - t) * rt1 + (t - times.getDouble(i - 2)) * rt2) / dt;
                yvalue = Math.exp(-rtvalue);
            }
        } else if (interpType == InterpolationType.LINEAR_FORWARD_RATES) {
            if (i == 1) {
                double y2 = -Math.log(Math.abs(dfs.getDouble(i)) + small);
                yvalue = t * y2 / (times.getDouble(i) + small);
                yvalue = Math.exp(-yvalue);
            } else if (i == 0) {
                double fwd1 = -Math.log(dfs.getDouble(dfs.size() - 1) / dfs.getDouble(dfs.size() - 2)) / (times.getDouble(times.size() - 1) - times.getDouble(times.size() - 2));
                double fwd2 = -Math.log(dfs.getDouble(i) / dfs.getDouble(dfs.size() - 1)) / (times.getDouble(i) - times.getDouble(times.size() - 1));
                double dt = times.getDouble(i) - times.getDouble(times.size() - 1);
                double fwd = ((times.getDouble(i) - t) * fwd1 + (t - times.getDouble(times.size() - 1)) * fwd2) / dt;
                yvalue = dfs.getDouble(dfs.size() - 1) * Math.exp(-fwd * (t - times.getDouble(times.size() - 1)));

            } else if (i < numPoints) {
                double fwd1 = -Math.log(dfs.getDouble(i - 1) / dfs.getDouble(i - 2)) / (times.getDouble(i - 1) - times.getDouble(i - 2));
                double fwd2 = -Math.log(dfs.getDouble(i) / dfs.getDouble(i - 1)) / (times.getDouble(i) - times.getDouble(i - 1));
                double dt = times.getDouble(i) - times.getDouble(i - 1);
                double fwd = ((times.getDouble(i) - t) * fwd1 + (t - times.getDouble(i - 1)) * fwd2) / dt;
                yvalue = dfs.getDouble(i - 1) * Math.exp(-fwd * (t - times.getDouble(i - 1)));
            } else {
                double fwd = -Math.log(dfs.getDouble(i - 1) / dfs.getDouble(i - 2)) / (times.getDouble(i - 1) - times.getDouble(i - 2));
                yvalue = dfs.getDouble(i - 1) * Math.exp(-fwd * (t - times.getDouble(i - 1)));
            }
        }
        return yvalue;
    }

}
