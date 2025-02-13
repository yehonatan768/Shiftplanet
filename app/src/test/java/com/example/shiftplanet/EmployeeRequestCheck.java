package com.example.shiftplanet;
import static com.example.shiftplanet.EmployeeRequestPage.requestFieldsCheck;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

public class EmployeeRequestCheck {
    @Test
    public void testValidInput() {
        assertTrue(requestFieldsCheck("Vacation", "2025-02-15", "2025-02-20"));
    }

    @Test
    public void testEmptyReason() {
        assertFalse(requestFieldsCheck("", "2025-02-15", "2025-02-20"));
    }

    @Test
    public void testEmptyStartDate() {
        assertFalse(requestFieldsCheck("Vacation", "", "2025-02-20"));
    }

    @Test
    public void testEmptyEndDate() {
        assertFalse(requestFieldsCheck("Vacation", "2025-02-15", ""));
    }

    @Test
    public void testAllEmpty() {
        assertFalse(requestFieldsCheck("", "", ""));
    }

}







