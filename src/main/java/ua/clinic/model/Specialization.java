package ua.clinic.model;

public enum Specialization {
    CARDIOLOGY(1.5),
    NEUROLOGY(1.3),
    PEDIATRICS(1.1),
    DENTISTRY(1.8);

    private final double feeMultiplier;

    Specialization(double feeMultiplier) {
        this.feeMultiplier = feeMultiplier;
    }

    public double getFeeMultiplier() {
        return feeMultiplier;
    }
}
