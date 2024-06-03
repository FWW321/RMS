package com.fww.error;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.fww.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(NumberFormatException.class)
    public Map<String, String> handleNumberFormatException(NumberFormatException e){
        return Map.of("msg", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Map<String, String> handleException(Exception e)
    {
        return Map.of("msg", e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public Map<String, String> handleNullPointerException( Exception e){
        return Map.of("msg", e.getMessage());
    }
}
