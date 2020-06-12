package io.onemfive.data;

/**
 * In Escalating Order from least censorship resistant to most censorship resistant.
 */
public enum Network {
    // Invisible Matrix Services (1M5) - Internal Only
    IMS,
    // Hyper-Text Transfer Protocol Secured
    HTTPS,
    // The Onion Router
    TOR,
    // Invisible Internet Project
    I2P,
    Bluetooth,
    WiFiDirect,
    Satellite,
    // Full-Spectrum Radio (using SDR - Software Defined Radio)
    FSRadio,
    // Light Fidelity
    LiFi
}
