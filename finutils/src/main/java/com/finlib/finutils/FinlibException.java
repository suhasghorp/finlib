package com.finlib.finutils;

public class FinlibException extends RuntimeException {
    public FinlibException(){
        super();
    }
    public FinlibException(String msg){
        super(msg);
    }
}
