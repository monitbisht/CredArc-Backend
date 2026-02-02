package com.credarc.credarc.dto;

public class AccountCreationResponse {
    private String confirmationMessage;

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String message) {
        this.confirmationMessage = message;
    }
}