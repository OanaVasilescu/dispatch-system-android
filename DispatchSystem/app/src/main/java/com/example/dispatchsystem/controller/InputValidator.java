package com.example.dispatchsystem.controller;


import android.widget.EditText;

public class InputValidator {
    public boolean doStringsMatch(String firstString, String secondString) {
        if (firstString.equals(secondString))
            return true;
        return false;
    }

    public void setFieldError(EditText field, String errorMessage) {
        field.setError(errorMessage);
        field.requestFocus();
        return; ///?
    }
}