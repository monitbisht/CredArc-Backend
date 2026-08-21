package com.credarc.credarc.exception;

public class RateLimitExceededException extends RuntimeException{

    public RateLimitExceededException(String message){
        super(message);
    }
}
