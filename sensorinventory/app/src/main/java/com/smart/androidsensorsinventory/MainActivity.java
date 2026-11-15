package com.smart.androidsensorsinventory;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Lab 5: sensor inventory and capability analysis (metadata only, no live readings).
 * This is the launcher screen of a standalone app.
 * Shows device details, a total count, a filterable list, a details dialog per sensor,
 * and safe handling of unavailable sensors and an empty inventory.
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_FILTER = "filter_position";
    private static final String STATE_SIM_EMPTY = "simulate_empty";

    private static final String[] FILTERS = {"All", "Motion", "Position", "Environment", "Other"};

    private static final String[] PROBE_NAMES = {
            "Accelerometer", "Gyroscope", "Magnetic field", "Light", "Proximity",
            "Pressure (barometer)", "Ambient temperature", "Relative humidity",
            "Step counter", "Heart rate"};
    private static final int[] PROBE_TYPES = {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY, Sensor.TYPE_PRESSURE,
            Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_HEART_RATE};

    private SensorManager sensorManager;
    private final List<Sensor> allSensors = new ArrayList<>();
    private final List<Sensor> shownSensors = new ArrayList<>();
    private SensorRowAdapter adapter;

    private TextView tvTotal;
    private TextView tvShowing;
    private TextView tvEmpty;
    private TextView tvProbeResult;
    private Spinner spFilter;
    private Spinner spProbe;
    private CheckBox cbSimulateEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The sensor service. It can be null on unusual devices, so it is always null-checked.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        ListView lv = findViewById(R.id.lvSensors);
        View header = LayoutInflater.from(this).inflate(R.layout.header_sensor_inventory, lv, false);
        lv.addHeaderView(header, null, false); // header scrolls with the list (good in landscape)

        TextView tvDevice = header.findViewById(R.id.tvDevice);
        tvTotal = header.findViewById(R.id.tvTotal);
        tvShowing = header.findViewById(R.id.tvShowing);
        tvEmpty = header.findViewById(R.id.tvEmpty);
        tvProbeResult = header.findViewById(R.id.tvProbeResult);
        spFilter = header.findViewById(R.id.spFilter);
        spProbe = header.findViewById(R.id.spProbe);
        cbSimulateEmpty = header.findViewById(R.id.cbSimulateEmpty);

        tvDevice.setText(SensorFormat.deviceSummary());

        adapter = new SensorRowAdapter(shownSensors);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof Sensor) {
                showDetails((Sensor) item);
            }
        });

        spFilter.setAdapter(spinnerAdapter(FILTERS));
        spProbe.setAdapter(spinnerAdapter(PROBE_NAMES));

        boolean simulateEmpty = false;
        int filterPosition = 0;
        if (savedInstanceState != null) { // restore after rotation
            simulateEmpty = savedInstanceState.getBoolean(STATE_SIM_EMPTY, false);
            filterPosition = savedInstanceState.getInt(STATE_FILTER, 0);
        }
        cbSimulateEmpty.setChecked(simulateEmpty);
        spFilter.setSelection(filterPosition, false);

        spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        cbSimulateEmpty.setOnCheckedChangeListener((button, checked) -> {
            loadInventory();
            applyFilter();
        });
        header.findViewById(R.id.btnProbe).setOnClickListener(v -> checkSelectedSensor());

        loadInventory();
        applyFilter();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt(STATE_FILTER, spFilter.getSelectedItemPosition());
        out.putBoolean(STATE_SIM_EMPTY, cbSimulateEmpty.isChecked());
    }

    private ArrayAdapter<String> spinnerAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    /** Asks the sensor service for every sensor. Null and empty results are both handled. */
    private void loadInventory() {
        allSensors.clear();
        boolean simulate = cbSimulateEmpty.isChecked();
        if (!simulate && sensorManager != null) {
            List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
            if (list != null) {
                allSensors.addAll(list);
            }
        }
        String note = simulate ? "  (TEST MODE: empty inventory simulated)"
                : (sensorManager == null ? "  (sensor service unavailable)" : "");
        tvTotal.setText("Total sensors reported: " + allSensors.size() + note);
    }

    /** Fills the visible list from the full inventory using the chosen category. */
    private void applyFilter() {
        int pos = spFilter.getSelectedItemPosition();
        if (pos < 0 || pos >= FILTERS.length) {
            pos = 0;
        }
        String filter = FILTERS[pos];

        shownSensors.clear();
        for (Sensor s : allSensors) {
            if ("All".equals(filter) || filter.equals(SensorFormat.categoryOf(s.getType()))) {
                shownSensors.add(s);
            }
        }
        adapter.notifyDataSetChanged();
        tvShowing.setText("Showing " + shownSensors.size() + " (" + filter + ")");

        if (allSensors.isEmpty()) {
            tvEmpty.setText("No sensors reported. The sensor service returned an empty list, "
                    + "so sensor-based features should be hidden or offer a manual alternative.");
            tvEmpty.setVisibility(View.VISIBLE);
        } else if (shownSensors.isEmpty()) {
            tvEmpty.setText("No sensors in the \"" + filter + "\" category on this device.");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /** Checks one sensor type. getDefaultSensor() returns null when it is not present. */
    private void checkSelectedSensor() {
        int pos = spProbe.getSelectedItemPosition();
        if (pos < 0 || pos >= PROBE_TYPES.length) {
            return;
        }
        String name = PROBE_NAMES[pos];

        if (sensorManager == null) {
            tvProbeResult.setText("Cannot check " + name
                    + ": the sensor service is not available on this device.");
            return;
        }
        if (cbSimulateEmpty.isChecked()) {
            tvProbeResult.setText("TEST MODE: the inventory is simulated as empty, so "
                    + name + " is treated as NOT available.");
            return;
        }

        Sensor s = sensorManager.getDefaultSensor(PROBE_TYPES[pos]);
        if (s == null) {
            tvProbeResult.setText("NOT AVAILABLE: " + name
                    + " (getDefaultSensor returned null). A feature that needs it must be "
                    + "hidden or offer a manual alternative; the app must not crash.");
        } else {
            tvProbeResult.setText("AVAILABLE: " + SensorFormat.safe(s.getName())
                    + " (vendor: " + SensorFormat.safe(s.getVendor())
                    + "). Being listed does not guarantee accurate or useful readings.");
        }
    }

    private void showDetails(Sensor s) {
        new AlertDialog.Builder(this)
                .setTitle("Sensor details")
                .setMessage(SensorFormat.describe(s))
                .setPositiveButton("OK", null)
                .show();
    }

    /** One row per sensor: long names wrap onto several lines instead of being cut off. */
    private class SensorRowAdapter extends ArrayAdapter<Sensor> {

        SensorRowAdapter(List<Sensor> data) {
            super(MainActivity.this, 0, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_sensor_inventory, parent, false);
            }
            Sensor s = getItem(position);
            TextView name = convertView.findViewById(R.id.tvName);
            TextView meta = convertView.findViewById(R.id.tvMeta);
            TextView range = convertView.findViewById(R.id.tvRange);
            if (s != null) {
                String unit = SensorFormat.unitForType(s.getType());
                String suffix = (unit == null) ? "" : " " + unit;
                name.setText(SensorFormat.safe(s.getName()));
                meta.setText(SensorFormat.shortType(s) + "  |  "
                        + SensorFormat.categoryOf(s.getType()) + "  |  "
                        + SensorFormat.safe(s.getVendor()));
                range.setText("Max range: " + SensorFormat.num(s.getMaximumRange()) + suffix
                        + "    Resolution: " + SensorFormat.num(s.getResolution()) + suffix);
            }
            return convertView;
        }
    }
}