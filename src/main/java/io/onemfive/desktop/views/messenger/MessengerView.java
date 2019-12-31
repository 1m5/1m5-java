package io.onemfive.desktop.views.messenger;

import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.views.InitializableView;
import javafx.scene.layout.StackPane;

public class MessengerView extends InitializableView<StackPane, MessengerViewModel> {

    private final Navigation navigation;

    public MessengerView(Navigation navigation) {
        this.navigation = navigation;
    }

    @Override
    protected void initialize() {

    }


}
