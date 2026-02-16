package com.credarc.credarc.exception;

public class InsufficientBalanceException extends RuntimeException{

    public InsufficientBalanceException(){
        super("Not Enough Funds To Withdraw !!");
    }
}
