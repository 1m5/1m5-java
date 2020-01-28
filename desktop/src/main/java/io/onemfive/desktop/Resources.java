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
package io.onemfive.desktop;

import io.onemfive.data.ManCon;

import java.net.URL;

public class Resources {

    // Web
    public static final URL IMS_WEB_INDEX = Resources.class.getResource("/web/1m5/index.html");

    // ManCon Icons
    public static final URL ICON_BLUE = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_blue.png");
    public static final URL ICON_GRAY = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_gray.png");
    public static final URL ICON_GREEN = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_green.png");
    public static final URL ICON_ORANGE = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_orange.png");
    public static final URL ICON_RED = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_red.png");
    public static final URL ICON_WHITE = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_white.png");
    public static final URL ICON_YELLOW = Resources.class.getResource("/io/onemfive/desktop/images/icons/icon_yellow.png");

    public static final URL ICON_TOR = Resources.class.getResource("/io/onemfive/desktop/images/network/tor-64.png");
    public static final URL ICON_I2P = Resources.class.getResource("/io/onemfive/desktop/images/network/i2p-orig.png");
    public static final URL ICON_BT = Resources.class.getResource("/io/onemfive/desktop/images/network/bluetooth-black-50.png");
    public static final URL ICON_WIFI = Resources.class.getResource("/io/onemfive/desktop/images/network/wifi-black-24.png");
    public static final URL ICON_SATELLITE = Resources.class.getResource("/io/onemfive/desktop/images/network/satellite-black-64.png");
    public static final URL ICON_RADIO = Resources.class.getResource("/io/onemfive/desktop/images/network/radio-black-64.png");
    public static final URL ICON_LIFI = Resources.class.getResource("/io/onemfive/desktop/images/network/lifi-black.png");

    public static URL getManConIcon(ManCon manCon) {
        switch (manCon) {
            case LOW: return ICON_GREEN;
            case MEDIUM: return ICON_BLUE;
            case HIGH: return ICON_YELLOW;
            case VERYHIGH: return ICON_ORANGE;
            case EXTREME: return ICON_RED;
            case NEO: return ICON_GRAY;
            default: return ICON_YELLOW;
        }
    }

    public static URL getManConIcon(Integer index) {
        switch (index) {
            case 0: return ICON_GRAY;
            case 1: return ICON_RED;
            case 2: return ICON_ORANGE;
            case 3: return ICON_YELLOW;
            case 4: return ICON_BLUE;
            case 5: return ICON_GREEN;
            default: return ICON_YELLOW;
        }
    }

}
