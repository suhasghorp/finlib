package com.finlib.finutils;

import java.util.List;

public final class MathUtils {
    private MathUtils(){}
    public static boolean checkMonotonicity(List<Double> x){
        for (var i = 1; i < x.size(); i++){
            if (x.get(i) <= x.get(i-1))
                return false;
        }
        return true;
    }
}

