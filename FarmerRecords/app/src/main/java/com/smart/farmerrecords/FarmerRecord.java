package com.smart.farmerrecords;

/** The model: one accepted farmer record. Immutable, so a list row can never be half-changed. */
public final class FarmerRecord {
    public final int id;
    public final String farmer;
    public final String crop;
    public final double quantityKg;
    public final String recordedAt;

    public FarmerRecord(int id, String farmer, String crop, double quantityKg, String recordedAt) {
        this.id = id;
        this.farmer = farmer;
        this.crop = crop;
        this.quantityKg = quantityKg;
        this.recordedAt = recordedAt;
    }
}
