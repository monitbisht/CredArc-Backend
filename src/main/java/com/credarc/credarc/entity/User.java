package com.credarc.credarc.entity;

import java.time.Instant;
import java.util.UUID;

public class User {

    private UUID userId;

    private String name;

    private String email;

    private String mobile;

    private Instant createdAt;

    /** Getters **/

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }


    /** Setters **/

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
