package com.credarc.credarc.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(nullable = false , updatable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(updatable = false , nullable = false)
    private Instant createdAt ;

    @Column(nullable = false)
    private BigDecimal balance =  BigDecimal.ZERO;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    /* BigDecimal.ZERO points to one static object shared by everyone
    instead of creating new object on the heap every time an Account is created. */


    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }

    /** Getters **/

    public UUID getAccountId() { return accountId; }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public BigDecimal getBalance() {
        return balance;
    }




    /** Setters **/

    public void setStatus(AccountStatus status) { this.status = status; }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
