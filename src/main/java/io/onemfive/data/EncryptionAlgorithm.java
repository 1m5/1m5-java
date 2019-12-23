package io.onemfive.data;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public enum EncryptionAlgorithm {

    CAST5("CAST-5"),
    AES256("AES-256"),
    AES512("AES-512");

    private String name;

    EncryptionAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EncryptionAlgorithm value(String name) {
        switch(name){
            case "CAST-5": return CAST5;
            case "AES-256": return AES256;
            case "AES-512": return AES512;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
