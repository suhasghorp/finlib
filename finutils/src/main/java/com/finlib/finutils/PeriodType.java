package com.finlib.finutils;

public enum PeriodType {
    DAY (1),
    WEEKS (2),
    MONTHS (3),
    YEARS (4);

    private final int num;
    PeriodType(int i){
        this.num = i;
    }

}