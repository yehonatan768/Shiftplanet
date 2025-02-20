package com.example.shiftplanet;

import static com.example.shiftplanet.Registration.validPasswordCheck;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PasswordTest {

    @Test
    public void testValid () {
        assertTrue(validPasswordCheck("Pass1234"));
    }

    @Test
    public void testPasswordTooShort() {
        assertFalse(validPasswordCheck("Pass1"));
    }

    @Test
    public void testPasswordNoUppercase() {
        assertFalse(validPasswordCheck("pass123"));
    }

    @Test
    public void testPasswordNoNumbers() {
        assertFalse(validPasswordCheck("Password"));

    }
    @Test
    public void testPasswordNoLetters() {
        assertFalse(validPasswordCheck("123456"));
    }

}


