package com.smart.farmerrecords;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

/** The adapter: turns each FarmerRecord (model) into a row view for the ListView. */
public class RecordAdapter extends ArrayAdapter<FarmerRecord> {

    public RecordAdapter(Context context, List<FarmerRecord> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_record, parent, false);
        }
        FarmerRecord r = getItem(position);
        TextView title = convertView.findViewById(R.id.tvTitle);
        TextView sub = convertView.findViewById(R.id.tvSub);
        title.setText("#" + r.id + "  " + r.farmer + "  -  " + r.crop);
        sub.setText(String.format(Locale.US, "%.2f kg   recorded %s", r.quantityKg, r.recordedAt));
        return convertView;
    }
}
