package io.onemfive.data;

public enum Sensitivity { // with default sensors chosen
    NONE, // HTTP - MANCON 6
    LOW, // HTTPS - MANCON 5
    MEDIUM, // Tor - MANCON 4
    HIGH, // I2P - MANCON 3
    VERYHIGH, // I2P Delayed - MANCON 2
    EXTREME, // 1DN - MANCON 1
    NEO // Intelligent Combination of Tor, I2P, and 1DN - MANCON 0
}
