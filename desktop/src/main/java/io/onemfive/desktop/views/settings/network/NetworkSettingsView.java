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
package io.onemfive.desktop.views.settings.network;

import io.onemfive.desktop.util.Layout;
import io.onemfive.desktop.views.ActivatableView;
import io.onemfive.util.Res;
import javafx.scene.layout.GridPane;

import static io.onemfive.desktop.util.FormBuilder.*;

public class NetworkSettingsView extends ActivatableView {

    private GridPane pane;
    private int gridRow = 0;

    public NetworkSettingsView() {
        super();
    }

    public void initialize() {
        LOG.info("Initializing...");
        pane = (GridPane)root;

        addTitledGroupBg(pane, gridRow, 3, Res.get("settings.net.localNode"));
        addCompactTopLabelTextField(pane, ++gridRow, Res.get("settings.net.1m5Fingerprint"), "+sKVViuz2FPsl/XQ+Da/ivbNfOI=", Layout.FIRST_ROW_DISTANCE);
        addCompactTopLabelTextAreaWithText(pane, "mQENBF43FaEDCACtMtZJu3oSchRgtaUzTmMJRbJmdfSpEaG2nW7U2YinHeMUkIpFCQGu2/OgmCuE4kVEQ4y6kKvqCiMvahtv+OqID0Lk7JEofFpwH8UUUis+p99qnw7RYy1q4IrjBpFSZHLi/nCyZOp4L7jG0CgJEFoZZEd2Uby1vnmePxts7srWkBjlmUWj+e/G89r+ZYpRN7dwdwl69Qk2s3UWTq1xyVyMqg/RuFC9kUgsmkL8vIpO4KYX7DfRKmYT29gfwjrvbVd18oeFECFVU/E6118N4P/8zIj0vhOiuar5hdKiq3oU5ka1hlQqP3IrQz2+feh2Q34+TP/BBEKOvbSv6V/6/6T/ABEBAAG0BUFsaWNliQEuBBMDAgAYBQJeNxWkAhsDBAsJCAcGFQgCCQoLAh4BAAoJEPg2v4r2zXzihH8H/iKc0ZBoWbeP/FykApYjG9m8ze54Pr9noRUw7JDAs6a7Y4IjNuE42NLMMwcxCoekzVmUwMyLrQDW+pLMaZupX2i8yU720F9WMh4f9eC4lXg64IMTnNUZqI4U52wZV22nxiGdGqacHwSSRcG5rHBskdrOJ8BX0QQ7Qt+iw4xyaxMPSPnULiJv3Z+kwLVLbxMQsmtLy7BZW6Pn848oONRNodg9tWn3PA/jTFg4ak+9lzfc1HnAWe/FeQ7O6jZ3h5eAbC4Y9KQqxVI7QzOkwIpRHMbkrVHdEcZMOa36wznC6SCXxpB/uGNrVnCJ0og9RN701QbxOu0XcevMjAOcE5dsC3g=", ++gridRow, Res.get("settings.net.1m5AddressLabel"), true);

        LOG.info("Initialized");
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

}

