package com.devflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashTests {

    @Test
    void generateDemoPasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "DevFlow123!";
        String aliceHash = encoder.encode(password);
        String bobHash = encoder.encode(password);

        assertTrue(encoder.matches(password, aliceHash));
        assertTrue(encoder.matches(password, bobHash));
        assertFalse(encoder.matches("wrong-password", aliceHash));

        System.out.println("Alice hash: " + aliceHash);
        System.out.println("Bob hash: " + bobHash);


    }

}
