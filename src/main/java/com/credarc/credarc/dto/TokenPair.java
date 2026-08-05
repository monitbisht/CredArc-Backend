package com.credarc.credarc.dto;

public class TokenPair {
    private String refreshToken;
    private String accessToken;

    /** Getters **/

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /** Setters **/

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
