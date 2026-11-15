package com.smart.androidsensorsinventory;

import android.hardware.Sensor;
import android.os.Build;

import java.util.Locale;

/**
 * Helpers that turn raw Sensor METADATA into readable text with units.
 * Nothing here reads live sensor values (that belongs to Lab 6).
 */
public final class SensorFormat {

    public static final String NOT_REPORTED = "not reported";

    private SensorFormat() { }

    /** Unit of the readings for a sensor type, or null when this app does not state one. */
    public static String unitForType(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "m/s\u00b2";
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "rad/s";
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "\u00b5T";
            case Sensor.TYPE_LIGHT:
                return "lux";
            case Sensor.TYPE_PROXIMITY:
                return "cm";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "\u00b0C";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            case Sensor.TYPE_STEP_COUNTER:
                return "steps";
            case Sensor.TYPE_HEART_RATE:
                return "bpm";
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "unitless";
            default:
                return null;
        }
    }

    /** Groups sensor types so the inventory can be filtered. */
    public static String categoryOf(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
            case Sensor.TYPE_SIGNIFICANT_MOTION:
            case Sensor.TYPE_STEP_COUNTER:
            case Sensor.TYPE_STEP_DETECTOR:
                return "Motion";
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
            case Sensor.TYPE_PROXIMITY:
                return "Position";
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Environment";
            default:
                return "Other";
        }
    }

    /** "android.sensor.accelerometer" becomes "accelerometer". */
    public static String shortType(Sensor s) {
        String t = s.getStringType();
        if (t == null || t.isEmpty()) {
            return "type " + s.getType();
        }
        return t.startsWith("android.sensor.") ? t.substring("android.sensor.".length()) : t;
    }

    /** Never returns null: missing text is reported honestly. */
    public static String safe(String v) {
        return (v == null || v.trim().isEmpty()) ? NOT_REPORTED : v.trim();
    }

    /** Compact number: no long float tails, scientific notation for tiny or huge values. */
    public static String num(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            return NOT_REPORTED;
        }
        if (v == 0f) {
            return "0";
        }
        float a = Math.abs(v);
        if (a < 0.001f || a >= 1000000f) {
            return String.format(Locale.US, "%.3e", v);
        }
        int decimals = (a >= 1f) ? 4 : (a >= 0.01f ? 6 : 8);
        String s = String.format(Locale.US, "%." + decimals + "f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    /** Value with unit; a zero is flagged because drivers often use 0 for "unknown". */
    private static String valueWithUnit(float v, String suffix) {
        if (v == 0f) {
            return "0" + suffix + "  (zero may mean 'not reported')";
        }
        return num(v) + suffix;
    }

    public static String reportingModeName(int mode) {
        switch (mode) {
            case Sensor.REPORTING_MODE_CONTINUOUS:
                return "continuous (events at a steady rate)";
            case Sensor.REPORTING_MODE_ON_CHANGE:
                return "on change (event only when the value changes)";
            case Sensor.REPORTING_MODE_ONE_SHOT:
                return "one-shot (one event, then it stops)";
            case Sensor.REPORTING_MODE_SPECIAL_TRIGGER:
                return "special trigger";
            default:
                return "unknown (" + mode + ")";
        }
    }

    private static String minDelayText(int us) {
        if (us > 0) {
            return us + " \u00b5s  (fastest rate about " + num((float) (1000000.0 / us)) + " Hz)";
        }
        if (us == 0) {
            return "0  (no fixed rate: reports on change, or not reported)";
        }
        return us + "  (negative: one-shot / trigger sensor, no streaming rate)";
    }

    private static String maxDelayText(int us) {
        if (us > 0) {
            return us + " \u00b5s  (slowest rate about " + num((float) (1000000.0 / us)) + " Hz)";
        }
        return us + "  (not reported / not applicable)";
    }

    private static void line(StringBuilder b, String label, String value) {
        b.append(label).append(": ").append(value).append("\n");
    }

    /** Full metadata text for one sensor, with units and honest "not reported" values. */
    public static String describe(Sensor s) {
        int type = s.getType();
        String unit = unitForType(type);
        String suffix = (unit == null) ? "" : " " + unit;

        StringBuilder b = new StringBuilder();
        line(b, "Name", safe(s.getName()));
        line(b, "Type", type + "  (" + shortType(s) + ")");
        line(b, "Category", categoryOf(type));
        line(b, "Vendor", safe(s.getVendor()));
        line(b, "Version", String.valueOf(s.getVersion()));
        line(b, "Unit of readings", unit == null
                ? "not stated by this app for this type (see Android docs)" : unit);
        line(b, "Maximum range", valueWithUnit(s.getMaximumRange(), suffix));
        line(b, "Resolution", valueWithUnit(s.getResolution(), suffix)
                + "  (smallest change it can report; not the same as accuracy)");

        float power = s.getPower();
        line(b, "Power", num(power) + " mA"
                + (power == 0f ? "  (0 usually means 'not reported', not 'free')"
                : "  (drawn while the sensor is active)"));

        line(b, "Min delay", minDelayText(s.getMinDelay()));
        line(b, "Max delay", maxDelayText(s.getMaxDelay()));
        line(b, "Reporting mode", reportingModeName(s.getReportingMode()));
        line(b, "FIFO (max / reserved)", s.getFifoMaxEventCount() + " / "
                + s.getFifoReservedEventCount() + " events");
        line(b, "Wake-up sensor", s.isWakeUpSensor() ? "yes" : "no");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            line(b, "Dynamic sensor", s.isDynamicSensor() ? "yes" : "no");
        }
        b.append("\nThese are driver-reported capability values, not measured readings.");
        return b.toString();
    }

    /** Rough check only: emulators and real phones can both fool it. */
    public static boolean looksLikeEmulator() {
        String fp = String.valueOf(Build.FINGERPRINT).toLowerCase(Locale.US);
        String hw = String.valueOf(Build.HARDWARE).toLowerCase(Locale.US);
        String model = String.valueOf(Build.MODEL).toLowerCase(Locale.US);
        String product = String.valueOf(Build.PRODUCT).toLowerCase(Locale.US);
        return fp.startsWith("generic") || fp.startsWith("unknown")
                || hw.contains("goldfish") || hw.contains("ranchu")
                || model.contains("emulator") || model.contains("sdk_gphone")
                || model.contains("android sdk built for")
                || product.contains("sdk") || product.contains("emulator");
    }

    /** Device, OS and real/simulated label. Reads no serial number, IMEI or account data. */
    public static String deviceSummary() {
        return "Device: " + Build.MANUFACTURER + " " + Build.MODEL
                + "\nAndroid " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + "\nEnvironment: "
                + (looksLikeEmulator()
                ? "SIMULATED (emulator, detected by heuristic)"
                : "Physical device (by heuristic)");
    }
}