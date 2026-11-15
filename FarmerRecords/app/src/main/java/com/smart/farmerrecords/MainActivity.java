package com.smart.farmerrecords;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Lab_01_hm1: farmer records, validation and the Activity lifecycle.
 * Lab 3: Options menu, navigation and Toast feedback.
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

    private Snackbar activeSnackbar;

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

        btnAdd.setOnClickListener(v -> addRecordFromForm());
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

    // ------------------------------------------------------------------ OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("MenuTrace", "selected=" + entryName(id));

        if (id == R.id.action_add_record) {
            // Option to submit input from the form
            Log.d("MenuTrace", "  handler=addRecordFromForm");
            addRecordFromForm();
            return true;
        } else if (id == R.id.action_view_records) {
            // Option to navigate to the secondary RecordsActivity screen
            Log.d("MenuTrace", "  handler=openRecordsScreen");
            openRecordsScreen();
            return true;
        } else if (id == R.id.action_clear_form) {
            // Option to clear the current draft input fields with confirmation
            Log.d("MenuTrace", "  handler=clearFormWithWarning");
            clearFormWithWarning();
            return true;
        } else if (id == R.id.action_about) {
            // Option to show information dialog about the app
            Log.d("MenuTrace", "  handler=showAboutDialog");
            showAboutDialog();
            return true;
        } else {
            // Default handler for unhandled options (including action_export placeholder)
            Log.w("MenuTrace", "  UNHANDLED item " + entryName(id));
            toast(R.string.msg_not_available);
            return super.onOptionsItemSelected(item);
        }
    }

    private String entryName(int id) {
        try {
            return getResources().getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            return "unknown(" + id + ")";
        }
    }

    // ------------------------------------------------------------------ ACTIONS
    /** Refactored add record logic shared by the Add Button and Options Menu item. */
    private void addRecordFromForm() {
        if (activeSnackbar != null) {
            activeSnackbar.dismiss();
            activeSnackbar = null;
        }
        etFarmer.setError(null);
        etQty.setError(null);

        RecordValidator.Result r = RecordValidator.validate(
                etFarmer.getText().toString(), etQty.getText().toString());

        if (!r.ok) {
            EditText bad = (r.field == RecordValidator.Field.FARMER) ? etFarmer : etQty;
            bad.setError(r.message);
            bad.requestFocus();
            Log.d("Validation", "REJECTED: " + r.message);

            activeSnackbar = Snackbar.make(findViewById(android.R.id.content),
                    r.message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbar_dismiss, v -> { });
            activeSnackbar.show();
            return;
        }

        String crop = String.valueOf(spCrop.getSelectedItem());
        FarmerRecord rec = new FarmerRecord(vm.nextId++, r.farmer, crop, r.quantityKg,
                ROW_TIME.format(new Date()));
        vm.records.add(rec);
        adapter.notifyDataSetChanged();
        lvRecords.smoothScrollToPosition(vm.records.size() - 1);
        refreshCount();
        Log.d("Validation", "ACCEPTED: #" + rec.id);

        etFarmer.setText("");
        etQty.setText("");
        etFarmer.requestFocus();

        toast(getString(R.string.msg_record_added, rec.id));
    }

    /** Opens the secondary RecordsActivity displaying recorded harvest lines. */
    private void openRecordsScreen() {
        ArrayList<String> lines = new ArrayList<>();
        for (FarmerRecord rec : vm.records) {
            lines.add(String.format(Locale.US, "#%d  %s  -  %s  -  %.2f kg",
                    rec.id, rec.farmer, rec.crop, rec.quantityKg));
        }
        Intent i = new Intent(this, RecordsActivity.class);
        i.putStringArrayListExtra(RecordsActivity.EXTRA_RECORD_LINES, lines);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    /** Warns user before clearing non-blank draft fields. */
    private void clearFormWithWarning() {
        boolean hasFarmerText = !etFarmer.getText().toString().trim().isEmpty();
        boolean hasQtyText = !etQty.getText().toString().trim().isEmpty();

        if (hasFarmerText || hasQtyText) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_discard_title)
                    .setMessage(R.string.dialog_discard_message)
                    .setPositiveButton(R.string.btn_discard, (dialog, which) -> {
                        etFarmer.setText("");
                        etQty.setText("");
                        etFarmer.setError(null);
                        etQty.setError(null);
                        if (activeSnackbar != null) {
                            activeSnackbar.dismiss();
                            activeSnackbar = null;
                        }
                        toast(R.string.msg_form_cleared);
                    })
                    .setNegativeButton(R.string.btn_keep, null)
                    .show();
        } else {
            toast(R.string.msg_form_empty);
        }
    }

    /** Displays the About dialog. */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void refreshCount() {
        tvCount.setText(getString(R.string.records_count, vm.records.size()));
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
