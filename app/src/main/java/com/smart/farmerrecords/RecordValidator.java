package com.smart.farmerrecords;

import java.util.regex.Pattern;

/**
 * Pure-Java validation rules (no Android classes), so they can be unit-tested on the JVM.
 *
 * Rules, defined BEFORE coding the screen:
 *  - Farmer reference/name: required (blank or spaces only is rejected).
 *  - Quantity (kg): required; must be a plain decimal number (digits with an optional
 *    decimal part, optional leading minus). Letters, commas, exponents, "NaN" and
 *    "Infinity" are rejected as non-numeric.
 *  - Quantity must be finite (a 400-digit number parses to Infinity and is rejected).
 *  - Quantity must not be negative.
 *  - ZERO IS ALLOWED: a weekly harvest of 0 kg is a real business event ("nothing was
 *    harvested this week"), unlike a delivery, which must move a positive weight.
 */
public final class RecordValidator {

    public enum Field { FARMER, QUANTITY }

    public static final class Result {
        public final boolean ok;
        public final Field field;       // which input is wrong (null when ok)
        public final String message;    // message shown to the user (null when ok)
        public final String farmer;     // cleaned value (when ok)
        public final double quantityKg; // parsed value (when ok)

        private Result(boolean ok, Field field, String message, String farmer, double q) {
            this.ok = ok;
            this.field = field;
            this.message = message;
            this.farmer = farmer;
            this.quantityKg = q;
        }

        static Result accepted(String farmer, double q) {
            return new Result(true, null, null, farmer, q);
        }

        static Result rejected(Field field, String message) {
            return new Result(false, field, message, null, Double.NaN);
        }
    }

    // Plain decimal only: 12, 12.5, .5, -3  (NOT 1e3, 5d, NaN, Infinity, 1,5, 12kg)
    private static final Pattern PLAIN_DECIMAL = Pattern.compile("-?(\\d+(\\.\\d+)?|\\.\\d+)");

    private RecordValidator() { }

    public static Result validate(String farmerRaw, String quantityRaw) {
        String farmer = farmerRaw == null ? "" : farmerRaw.trim();
        if (farmer.isEmpty()) {
            return Result.rejected(Field.FARMER, "Farmer reference or name is required");
        }

        String q = quantityRaw == null ? "" : quantityRaw.trim();
        if (q.isEmpty()) {
            return Result.rejected(Field.QUANTITY, "Quantity is required");
        }
        if (!PLAIN_DECIMAL.matcher(q).matches()) {
            return Result.rejected(Field.QUANTITY,
                    "Enter a plain number such as 12.5 (no letters, commas or exponents)");
        }

        double value = Double.parseDouble(q);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Result.rejected(Field.QUANTITY, "Quantity is too large to be valid");
        }
        if (value < 0) {
            return Result.rejected(Field.QUANTITY, "Quantity cannot be negative");
        }
        if (value == 0) {
            value = 0.0; // turns "-0" (negative zero) into plain 0.0
        }
        return Result.accepted(farmer, value);
    }
}
