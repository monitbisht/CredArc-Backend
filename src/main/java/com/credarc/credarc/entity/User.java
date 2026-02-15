package com.credarc.credarc.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(updatable = false,nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String mobile;

    @Column(updatable = false , nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }

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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
