package com.smart.farmerrecords;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RecordValidator (11 tests).
 */
public class RecordValidatorTest {

    @Test
    public void testBlankFarmerRejected() {
        RecordValidator.Result r = RecordValidator.validate("", "12.5");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.FARMER, r.field);
    }

    @Test
    public void testSpacesOnlyFarmerRejected() {
        RecordValidator.Result r = RecordValidator.validate("   ", "10");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.FARMER, r.field);
    }

    @Test
    public void testBlankQuantityRejected() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }

    @Test
    public void testValidIntegerQuantityAccepted() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "25");
        assertTrue(r.ok);
        assertNull(r.field);
        assertEquals("Farmer John", r.farmer);
        assertEquals(25.0, r.quantityKg, 0.001);
    }

    @Test
    public void testValidDecimalQuantityAccepted() {
        RecordValidator.Result r = RecordValidator.validate("Farmer Mary", "12.75");
        assertTrue(r.ok);
        assertNull(r.field);
        assertEquals("Farmer Mary", r.farmer);
        assertEquals(12.75, r.quantityKg, 0.001);
    }

    @Test
    public void testZeroQuantityAccepted() {
        RecordValidator.Result r = RecordValidator.validate("Farmer Peter", "0");
        assertTrue(r.ok);
        assertNull(r.field);
        assertEquals(0.0, r.quantityKg, 0.001);
    }

    @Test
    public void testLettersInQuantityRejected() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "12kg");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }

    @Test
    public void testCommasInQuantityRejected() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "12,5");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }

    @Test
    public void testExponentsInQuantityRejected() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "1e3");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }

    @Test
    public void testNegativeQuantityRejected() {
        RecordValidator.Result r = RecordValidator.validate("Farmer John", "-5");
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }

    @Test
    public void testInfiniteQuantityRejected() {
        String huge = "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999";
        RecordValidator.Result r = RecordValidator.validate("Farmer John", huge);
        assertFalse(r.ok);
        assertEquals(RecordValidator.Field.QUANTITY, r.field);
    }
}
