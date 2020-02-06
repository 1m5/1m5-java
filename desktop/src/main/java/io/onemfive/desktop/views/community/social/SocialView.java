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
package io.onemfive.desktop.views.community.social;

import io.onemfive.OneMFivePlatform;
import io.onemfive.data.DID;
import io.onemfive.data.Envelope;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.DesktopService;
import io.onemfive.desktop.views.InitializableView;
import io.onemfive.did.DIDService;
import io.onemfive.util.DLC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class SocialView extends InitializableView {

    private ObservableList<String> contactAddresses = FXCollections.observableArrayList();

    public void updateContacts(List<DID> contacts) {
        contactAddresses.clear();
        for(DID c : contacts) {
            contactAddresses.add(c.getUsername() + ": "+c.getPublicKey().getAddress());
        }
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        StackPane pane = (StackPane)root;


        LOG.info("Initialized.");
    }

}
