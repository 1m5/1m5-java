package io.onemfive.network.sensors.fullspectrum.bands;

import java.util.ArrayList;
import java.util.List;

public class ISMBands {

    private List<Band> bands = new ArrayList<>();

    public ISMBands() {
        bands.add(new Band(6765000,6795000) {});
        bands.add(new Band(13553000,13567000) {});
        bands.add(new Band(26957000,27283000) {});
        bands.add(new Band(40660000,40700000) {});
        bands.add(new Band(433050000,434790000) {});
        bands.add(new Band(902000000,928000000) {});
        bands.add(new Band(2400000000L,2500000000L) {});
        bands.add(new Band(5725000000L,5875000000L) {});
        bands.add(new Band(24000000000L,24250000000L) {});
        bands.add(new Band(61000000000L,61500000000L) {});
        bands.add(new Band(122000000000L,123000000000L) {});
        bands.add(new Band(244000000000L,246000000000L) {});
    }

}
