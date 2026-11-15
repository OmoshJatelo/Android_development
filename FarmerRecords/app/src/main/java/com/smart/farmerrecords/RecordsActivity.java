package com.smart.farmerrecords;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Secondary read-only screen displaying recorded harvest entries passed from MainActivity.
 */
public class RecordsActivity extends AppCompatActivity {

    public static final String EXTRA_RECORD_LINES = "extra_record_lines";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        setTitle(R.string.title_records);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<String> lines = getIntent().getStringArrayListExtra(EXTRA_RECORD_LINES);
        if (lines == null) {
            lines = new ArrayList<>();
        }

        TextView tvRecordsCount = findViewById(R.id.tvRecordsCount);
        ListView lvRecordLines = findViewById(R.id.lvRecordLines);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        tvRecordsCount.setText(getString(R.string.records_count, lines.size()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, lines);
        lvRecordLines.setAdapter(adapter);

        if (lines.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvRecordLines.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvRecordLines.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Overridden to finish RecordsActivity instead of navigating up by intent.
     * Default Up navigation would recreate MainActivity and lose its in-memory records.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
