package com.smart.farmerrecords;

import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Lab_01_hm1: farmer records, validation and the Activity lifecycle.
 *
 * Connection: input (EditText/Spinner) -> RecordValidator -> FarmerRecord (model)
 *             -> RecordsViewModel.records -> RecordAdapter -> ListView (what the user sees)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle";
    private static final String STATE_FARMER = "draft_farmer";
    private static final String STATE_QTY = "draft_quantity";
    private static final String STATE_CROP = "draft_crop";

    private static final SimpleDateFormat TRACE_TIME = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat ROW_TIME = new SimpleDateFormat("HH:mm", Locale.US);

    private RecordsViewModel vm;
    private RecordAdapter adapter;

    private EditText etFarmer;
    private EditText etQty;
    private Spinner spCrop;
    private TextView tvCount;
    private TextView tvTrace;
    private ScrollView svTrace;
    private ListView lvRecords;

    // ------------------------------------------------------------------ LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etFarmer = findViewById(R.id.etFarmer);
        etQty = findViewById(R.id.etQuantity);
        spCrop = findViewById(R.id.spCrop);
        tvCount = findViewById(R.id.tvCount);
        tvTrace = findViewById(R.id.tvTrace);
        svTrace = findViewById(R.id.svTrace);
        lvRecords = findViewById(R.id.lvRecords);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnClearTrace = findViewById(R.id.btnClearTrace);

        ArrayAdapter<CharSequence> cropAdapter = ArrayAdapter.createFromResource(
                this, R.array.crops, android.R.layout.simple_spinner_item);
        cropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCrop.setAdapter(cropAdapter);

        vm = new ViewModelProvider(this).get(RecordsViewModel.class); // same instance after rotation
        adapter = new RecordAdapter(this, vm.records);
        lvRecords.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> onAddClicked());
        btnClearTrace.setOnClickListener(v -> {
            vm.trace.clear();
            renderTrace();
        });

        refreshCount();
        trace("onCreate  savedInstanceState=" + (savedInstanceState == null ? "null (fresh start)" : "present (recreated)"));
    }

    @Override protected void onStart()   { super.onStart();   trace("onStart"); }
    @Override protected void onRestart() { super.onRestart(); trace("onRestart"); }
    @Override protected void onResume()  { super.onResume();  trace("onResume"); }
    @Override protected void onPause()   { super.onPause();   trace("onPause"); }
    @Override protected void onStop()    { super.onStop();    trace("onStop"); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trace("onDestroy  isChangingConfigurations=" + isChangingConfigurations()
                + " isFinishing=" + isFinishing());
    }

    /** SAVED STATE: small draft values, kept by the system across rotation AND process death. */
    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(STATE_FARMER, etFarmer.getText().toString());
        out.putString(STATE_QTY, etQty.getText().toString());
        out.putInt(STATE_CROP, spCrop.getSelectedItemPosition());
        trace("onSaveInstanceState  (draft saved)");
    }

    @Override
    protected void onRestoreInstanceState(Bundle saved) {
        super.onRestoreInstanceState(saved);
        etFarmer.setText(saved.getString(STATE_FARMER, ""));
        etQty.setText(saved.getString(STATE_QTY, ""));
        spCrop.setSelection(saved.getInt(STATE_CROP, 0));
        trace("onRestoreInstanceState  (draft restored)");
    }

    // ------------------------------------------------------------------ ADD RECORD
    private void onAddClicked() {
        etFarmer.setError(null);
        etQty.setError(null);

        RecordValidator.Result r = RecordValidator.validate(
                etFarmer.getText().toString(), etQty.getText().toString());

        if (!r.ok) {
            EditText bad = (r.field == RecordValidator.Field.FARMER) ? etFarmer : etQty;
            bad.setError(r.message);
            bad.requestFocus();
            Log.d("Validation", "REJECTED: " + r.message);
            return;
        }

        String crop = String.valueOf(spCrop.getSelectedItem());
        FarmerRecord rec = new FarmerRecord(vm.nextId++, r.farmer, crop, r.quantityKg,
                ROW_TIME.format(new Date()));
        vm.records.add(rec);
        adapter.notifyDataSetChanged();
        lvRecords.smoothScrollToPosition(vm.records.size() - 1);
        refreshCount();
        Log.d("Validation", "ACCEPTED: #" + rec.id + " " + rec.farmer + " " + rec.quantityKg + " kg");

        etFarmer.setText("");
        etQty.setText("");
        etFarmer.requestFocus();
    }

    private void refreshCount() {
        tvCount.setText("Records: " + vm.records.size());
    }

    // ------------------------------------------------------------------ TRACE
    /** Writes to Logcat (tag "Lifecycle") and to the on-screen trace. */
    private void trace(String event) {
        String line = TRACE_TIME.format(new Date())
                + "  act=" + Integer.toHexString(System.identityHashCode(this))
                + "  pid=" + Process.myPid()
                + "  " + event;
        Log.d(TAG, line);
        vm.trace.add(line);
        renderTrace();
    }

    private void renderTrace() {
        tvTrace.setText(TextUtils.join("\n", vm.trace));
        svTrace.post(() -> svTrace.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
