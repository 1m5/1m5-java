/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
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
