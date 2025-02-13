package com.example.shiftplanet;

import static com.example.shiftplanet.Registration.validPassword;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PasswordTest {

    @Test
    public void testValid () {
        assertTrue(validPassword("Pass1234"));
    }

    @Test
    public void testPasswordTooShort() {
        assertFalse(validPassword("Pass1"));
    }

    @Test
    public void testPasswordNoUppercase() {
        assertFalse(validPassword("pass123"));
    }

    @Test
    public void testPasswordNoNumbers() {
        assertFalse(validPassword("Password"));

    }
    @Test
    public void testPasswordNoLetters() {
        assertFalse(validPassword("123456"));
    }

}


