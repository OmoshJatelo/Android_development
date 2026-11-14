package com.smart.farmerrecords;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the records and the on-screen lifecycle trace.
 * A ViewModel SURVIVES ROTATION (the Activity is destroyed, the ViewModel is kept),
 * but it lives only in memory, so it does NOT survive process death or a force-stop.
 * It is screen state, not persistent storage.
 */
public class RecordsViewModel extends ViewModel {
    public final List<FarmerRecord> records = new ArrayList<>();
    public final List<String> trace = new ArrayList<>();
    public int nextId = 1;
}
