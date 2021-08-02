package com.finlib.finutils;

import java.util.ArrayList;
import java.util.List;

public final class TestUtils {
    public static double[] linspace(double min, double max, int points) {
        double[] d = new double[points];
        for (int i = 0; i < points; i++){
            d[i] = min + i * (max - min) / (points - 1);
        }
        return d;
    }
    public static List<Double> linspaceList(double min, double max, int points) {
        List<Double> d = new ArrayList<>(points);
        for (int i = 0; i < points; i++){
            d.add(min + i * (max - min) / (points - 1));
        }
        return d;
    }
}
