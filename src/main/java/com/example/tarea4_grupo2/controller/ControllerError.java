package com.example.tarea4_grupo2.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerError {

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(){
        return "login/error500";
    }
}
