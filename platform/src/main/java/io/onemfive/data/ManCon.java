package io.onemfive.data;

public enum ManCon {
    NEO, // MANCON 0
    EXTREME, // MANCON 1
    VERYHIGH, // MANCON 2
    HIGH, // MANCON 3
    MEDIUM, // MANCON 4
    LOW, // MANCON 5
    NONE,
    UNKNOWN;

    public static ManCon fromOrdinal(int i) {
        switch (i) {
            case 0: return NEO;
            case 1: return EXTREME;
            case 2: return VERYHIGH;
            case 3: return HIGH;
            case 4: return MEDIUM;
            case 5: return LOW;
            default: return NONE;
        }
    }

}
