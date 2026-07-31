package com.credarc.credarc.service;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void hashPassword_returnsNonNullHash() {
        String hash = passwordService.hashPassword("password");
        assertNotNull(hash);
    }

    @Test
    void hashPassword_returnsDifferentHash_thanRawPassword() {
        String hash = passwordService.hashPassword("password");
        assertNotEquals("password", hash);
    }

    @Test
    void hashPassword_producesDifferentHashes_forSamePassword() {
        String hash1 = passwordService.hashPassword("password");
        String hash2 = passwordService.hashPassword("password");
        assertNotEquals(hash1, hash2); // proves salting is actually happening
    }

    @Test
    void hashPassword_thenMatchPassword_returnsTrue_forCorrectPassword() {
        String hash = passwordService.hashPassword("password");
        assertTrue(passwordService.matchPassword("password", hash));
    }

    @Test
    void matchPassword_returnsFalse_forWrongPassword() {
        String hash = passwordService.hashPassword("password");
        assertFalse(passwordService.matchPassword("wrongPassword", hash));
    }

}