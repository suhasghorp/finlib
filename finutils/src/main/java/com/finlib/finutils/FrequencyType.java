package com.finlib.finutils;

public enum FrequencyType {
    ANNUAL (1),
    SEMI_ANNUAL (2),
    QUARTERLY (4),
    MONTHLY (12),
    SIMPLE (0),
    CONTINUOUS(-1);

    private final int num;
    FrequencyType(int i){
        this.num = i;
    }
    public int getFrequency(){
        return this.num;
    }
}