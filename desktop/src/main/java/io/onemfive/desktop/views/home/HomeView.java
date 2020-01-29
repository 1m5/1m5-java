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
package io.onemfive.desktop.views.home;

import com.jfoenix.controls.JFXComboBox;
import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.data.ManCon;
import io.onemfive.data.Tuple2;
import io.onemfive.desktop.DesktopApp;
import io.onemfive.desktop.MVC;
import io.onemfive.desktop.Resources;
import io.onemfive.desktop.components.AutoTooltipLabel;
import io.onemfive.desktop.components.AutoTooltipToggleButton;
import io.onemfive.desktop.components.Badge;
import io.onemfive.desktop.util.KeystrokeUtil;
import io.onemfive.desktop.util.Transitions;
import io.onemfive.desktop.views.*;
import io.onemfive.desktop.views.commons.CommonsView;
import io.onemfive.desktop.views.community.CommunityView;
import io.onemfive.desktop.views.mancon.ManConView;
import io.onemfive.desktop.views.personal.PersonalView;
import io.onemfive.desktop.views.settings.SettingsView;
import io.onemfive.desktop.views.support.SupportView;
import io.onemfive.util.LanguageUtil;
import io.onemfive.util.LocaleUtil;
import io.onemfive.util.Res;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import static io.onemfive.desktop.util.Layout.MIN_WINDOW_HEIGHT;
import static io.onemfive.desktop.util.Layout.MIN_WINDOW_WIDTH;
import static javafx.scene.layout.AnchorPane.*;

public class HomeView extends InitializableView {

    private StackPane rootContainer;
    private Label versionLabel;
    private Runnable onUiReadyHandler;
    private final ToggleGroup navButtons = new ToggleGroup();
    private Transitions transitions = new Transitions();

    private ComboBox<ManConComboBoxItem> manConComboBox;
    private final ObservableList<ManConComboBoxItem> manConComboBoxItems = FXCollections.observableArrayList();
    private final ObjectProperty<ManConComboBoxItem> selectedManConComboBoxItemProperty = new SimpleObjectProperty<>();

    public HomeView() {}

    public void setTransitions(Transitions transitions) {
        this.transitions = transitions;
    }

    public void setOnUiReadyHandler(Runnable onUiReadyHandler) {
        this.onUiReadyHandler = onUiReadyHandler;
    }

    public StackPane getRootContainer() {
        return rootContainer;
    }

    public void blurLight() {
        transitions.blur(rootContainer, Transitions.DEFAULT_DURATION, -0.6, false, 5);
    }

    public void blurUltraLight() {
        transitions.blur(rootContainer, Transitions.DEFAULT_DURATION, -0.6, false, 2);
    }

    public void darken() {
        transitions.darken(rootContainer, Transitions.DEFAULT_DURATION, false);
    }

    public void removeEffect() {
        transitions.removeEffect(rootContainer);
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        rootContainer = (StackPane)root;
        if (LanguageUtil.isDefaultLanguageRTL())
            rootContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        final ToggleButton personalButton = new NavButton(PersonalView.class, Res.get("homeView.menu.personal").toUpperCase());

        final ToggleButton communityButton = new NavButton(CommunityView.class, Res.get("homeView.menu.community").toUpperCase());
//        communityButton.disableProperty().setValue(true);
        final ToggleButton commonsButton = new NavButton(CommonsView.class, Res.get("homeView.menu.commons").toUpperCase());
//        commonsButton.disableProperty().setValue(true);

        final ToggleButton supportButton = new NavButton(SupportView.class, Res.get("homeView.menu.support").toUpperCase());
//        supportButton.disableProperty().setValue(true);
        final ToggleButton settingsButton = new NavButton(SettingsView.class, Res.get("homeView.menu.settings").toUpperCase());
//        settingsButton.disableProperty().setValue(true);
        final ToggleButton manconButton = new NavButton(ManConView.class, Res.get("homeView.menu.mancon").toUpperCase());
//        manconButton.disableProperty().setValue(true);

        Badge personalButtonWithBadge = new Badge(personalButton);
        Badge communityButtonWithBadge = new Badge(communityButton);
        Badge commonsButtonWithBadge = new Badge(commonsButton);
        Badge supportButtonWithBadge = new Badge(supportButton);
        Badge settingsButtonWithBadge = new Badge(settingsButton);
        Badge manconButtonWithBadge = new Badge(manconButton);

        DecimalFormat currencyFormat = (DecimalFormat) NumberFormat.getNumberInstance(LocaleUtil.currentLocale);
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(2);

        root.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {

                    if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT1, keyEvent)) {
                        personalButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT2, keyEvent)) {
                        communityButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT3, keyEvent)) {
                        commonsButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT4, keyEvent)) {
                        supportButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT5, keyEvent)) {
                        settingsButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT6, keyEvent)) {
                        manconButton.fire();
                    }
                });
            }
        });


        Tuple2<ComboBox<ManConComboBoxItem>, VBox> manConBox = getManConBox();
        manConComboBox = manConBox.first;

        manConComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedManConComboBoxItemProperty.setValue(newValue);
        });
        ChangeListener<ManConComboBoxItem> selectedManConItemListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                manConComboBox.getSelectionModel().select(newValue);
                OneMFiveAppContext.MANCON = newValue.manConLevel;
                LOG.info("ManCon new value: "+newValue.manConLevel.name());
            }
        };
        selectedManConComboBoxItemProperty.addListener(selectedManConItemListener);
        manConComboBox.setItems(manConComboBoxItems);
        manConComboBoxItems.addAll(Arrays.asList(
                new ManConComboBoxItem(ManCon.NEO),
                new ManConComboBoxItem(ManCon.EXTREME),
                new ManConComboBoxItem(ManCon.VERYHIGH),
                new ManConComboBoxItem(ManCon.HIGH),
                new ManConComboBoxItem(ManCon.MEDIUM),
                new ManConComboBoxItem(ManCon.LOW)
        ));
        manConComboBox.getSelectionModel().select(OneMFiveAppContext.MANCON.ordinal());

        Tuple2<ImageView, VBox> torSensorStatusBox = getStatusBox("image-tor-icon", 25);
        Tuple2<ImageView, VBox> i2pSensorStatusBox = getStatusBox("image-i2p-icon", 32);
        Tuple2<ImageView, VBox> btSensorStatusBox = getStatusBox("image-bluetooth-icon", 32);
        Tuple2<ImageView, VBox> wifiSensorStatusBox = getStatusBox("image-wifi-icon", 32);
        Tuple2<ImageView, VBox> satelliteSensorStatusBox = getStatusBox("image-satellite-icon", 32);
        Tuple2<ImageView, VBox> radioSensorStatusBox = getStatusBox("image-radio-icon", 32);
        Tuple2<ImageView, VBox> lifiSensorStatusBox = getStatusBox("image-lifi-icon", 32);

//        HBox primaryNav = new HBox(dashboardButton, getNavigationSeparator(), browserButton, getNavigationSeparator(),
//                emailButtonWithBadge, getNavigationSeparator(), messengerButtonWithBadge, getNavigationSeparator(), calendarButtonWithBadge);

        HBox primaryNav = new HBox(
                personalButton, getNavigationSeparator(),
                communityButton, getNavigationSeparator(),
                commonsButton);

        primaryNav.setAlignment(Pos.CENTER_LEFT);
        primaryNav.getStyleClass().add("nav-primary");
        HBox.setHgrow(primaryNav, Priority.NEVER);

//        HBox secondaryNav = new HBox(voiceButtonWithBadge, getNavigationSpacer(), videoButtonWithBadge,
//                getNavigationSpacer(), appsButtonWithBadge, getNavigationSpacer(), daoButtonWithBadge,
//                getNavigationSeparator(), identitiesButton, getNavigationSeparator(), supportButtonWithBadge,
//                getNavigationSeparator(), settingsButtonWithBadge);
        HBox secondaryNav = new HBox(
                supportButton, getNavigationSeparator(),
                settingsButton, getNavigationSeparator(),
                manconButton);

        secondaryNav.getStyleClass().add("nav-secondary");
        HBox.setHgrow(secondaryNav, Priority.NEVER);
        secondaryNav.setAlignment(Pos.CENTER_LEFT);


        HBox networkStatusHBox = new HBox(
                manConBox.second, getNavigationSeparator(),
                torSensorStatusBox.second, getNavigationSeparator(),
                i2pSensorStatusBox.second, getNavigationSeparator(),
                btSensorStatusBox.second, getNavigationSeparator(),
                wifiSensorStatusBox.second, getNavigationSeparator(),
                satelliteSensorStatusBox.second, getNavigationSeparator(),
                radioSensorStatusBox.second, getNavigationSeparator(),
                lifiSensorStatusBox.second);
        networkStatusHBox.setMaxHeight(41);

        networkStatusHBox.setAlignment(Pos.CENTER_RIGHT);
        networkStatusHBox.setSpacing(9);
        networkStatusHBox.getStyleClass().add("nav-tertiary");

        HBox navPane = new HBox(primaryNav, secondaryNav, networkStatusHBox) {{
            setLeftAnchor(this, 0d);
            setRightAnchor(this, 0d);
            setTopAnchor(this, 0d);
            setPadding(new Insets(0, 0, 0, 0));
            getStyleClass().add("top-navigation");
        }};
        navPane.setAlignment(Pos.CENTER);

        AnchorPane contentContainer = new AnchorPane() {{
            getStyleClass().add("content-pane");
            setLeftAnchor(this, 0d);
            setRightAnchor(this, 0d);
            setTopAnchor(this, 57d);
            setBottomAnchor(this, 0d);
        }};

        AnchorPane applicationContainer = new AnchorPane(navPane, contentContainer) {{
            setId("application-container");
        }};

        BorderPane baseApplicationContainer = new BorderPane(applicationContainer) {{
            setId("base-content-container");
        }};
        baseApplicationContainer.setBottom(createFooter());

//        setupBadge(emailButtonWithBadge, model.getNumUnreadEmails(), model.getShowNumUnreadEmails());
//        setupBadge(emailButtonWithBadge, model.getNumUnreadEmails(), model.getShowNumUnreadEmails());
//        setupBadge(messengerButtonWithBadge, model.getNumUnreadMessages(), model.getShowNumUnreadMessages());
//        setupBadge(calendarButtonWithBadge, model.getNumReminders(), model.getShowNumReminders());
//        setupBadge(callButtonWithBadge, model.getNumVoiceMails(), model.getShowNumVoiceMails());
//        setupBadge(videoButtonWithBadge, model.getNumNewVideos(), model.getShowNumNewVideos());
//        setupBadge(appsButtonWithBadge, model.getNumAppUpdates(), model.getShowNumAppUpdates());
//        setupBadge(daoButtonWithBadge, model.getNumDAONotifications(), model.getShowDAONotifications());
//        setupBadge(supportButtonWithBadge, model.getNumSupportResponses(), model.getShowSupportResponses());
//        setupBadge(settingsButtonWithBadge, model.getNumSettingsNotifications(), model.getShowNumSettingsNotifications());

        MVC.navigation.addListener(viewPath -> {
            if (viewPath.size() != 2 || viewPath.indexOf(HomeView.class) != 0)
                return;

            Class<? extends View> viewClass = viewPath.tip();
            View view = MVC.loadView(viewClass);
            contentContainer.getChildren().setAll(view.getRoot());

            try {
                navButtons.getToggles().stream()
                        .filter(toggle -> toggle instanceof NavButton)
                        .filter(button -> viewClass == ((NavButton) button).viewClass)
                        .findFirst()
                        .orElseThrow(() -> new Exception("No button matching "+viewClass.getName()+" found"))
                        .setSelected(true);
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
        });

        VBox splashScreen = createSplashScreen();
        splashScreen.setMinHeight(DesktopApp.HEIGHT);
        splashScreen.setMinWidth(DesktopApp.WIDTH);
        rootContainer.getChildren().addAll(baseApplicationContainer, splashScreen);
//        rootContainer.getChildren().addAll(baseApplicationContainer);

//        model.getShowAppScreen().addListener((ov, oldValue, newValue) -> {
//            if (newValue) {
//                navigation.navigateToPreviousVisitedView();
//
//                transitionUtil.fadeOutAndRemove(splashScreen, 1500, actionEvent -> disposeSplashScreen());
                transitions.fadeOutAndRemove(splashScreen, 3500, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
//                        navPane.setVisible(true);
                        // Default to Personal Dashboard
                        personalButton.fire();
                    }
                });
        transitions.fadeIn(baseApplicationContainer, 3500);
//            }
//        });

        // Delay a bit to give time for rendering the splash screen
//        UserThread.execute(() -> onUiReadyHandler.run());
        LOG.info("Initialized.");
    }

    private Separator getNavigationSeparator() {
        final Separator separator = new Separator(Orientation.VERTICAL);
        HBox.setHgrow(separator, Priority.ALWAYS);
        separator.setMaxHeight(22);
        separator.setMaxWidth(Double.MAX_VALUE);
        return separator;
    }

    private Region getNavigationSpacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Tuple2<ComboBox<ManConComboBoxItem>, VBox> getManConBox() {
        VBox manConVBox = new VBox();
        manConVBox.setAlignment(Pos.CENTER);

        ComboBox<ManConComboBoxItem> manConComboBox = new JFXComboBox<>();
        manConComboBox.setVisibleRowCount(6);
        manConComboBox.setFocusTraversable(false);
        manConComboBox.setId("mancon-combo");
        manConComboBox.setPadding(new Insets(0, 0, 0, 0));
        manConComboBox.setCellFactory(p -> getManConComboBoxListCell());
        ListCell<ManConComboBoxItem> buttonCell = getManConComboBoxListCell();
        buttonCell.setId("mancon-combo");
        manConComboBox.setButtonCell(buttonCell);

        manConVBox.getChildren().addAll(manConComboBox);

        return new Tuple2<>(manConComboBox, manConVBox);
    }

    private ListCell<ManConComboBoxItem> getManConComboBoxListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ManConComboBoxItem item, boolean empty) {
                super.updateItem(item, empty);
                if(item == null || empty) {
                    setGraphic(null);
                } else {
                    ImageView iconImageView = new ImageView(new Image(Resources.getManConIcon(this.getIndex()).toString()));
                    iconImageView.setFitHeight(25);
                    iconImageView.setPreserveRatio(true);
                    setGraphic(iconImageView);
                }
            }
        };
    }

    private Tuple2<ImageView, VBox> getStatusBox(String statusImageID, int fitHeight) {
        ImageView statusImageView = new ImageView();
        statusImageView.setFitHeight(fitHeight);
        statusImageView.setPreserveRatio(true);
        statusImageView.setId(statusImageID);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(statusImageView);
        return new Tuple2<>(statusImageView, vBox);
    }

//    private Tuple2<ImageView, VBox> getStatusBox(URL statusImageURL, int fitHeight) {
//        ImageView statusImageView = new ImageView(new Image(statusImageURL.toString()));
//        statusImageView.setFitHeight(fitHeight);
//        statusImageView.setPreserveRatio(true);
//
//        VBox vBox = new VBox();
//        vBox.setAlignment(Pos.CENTER_LEFT);
//        vBox.getChildren().addAll(statusImageView);
//        return new Tuple2<>(statusImageView, vBox);
//    }

//    private String getPriceProvider() {
//        return model.getIsFiatCurrencyPriceFeedSelected().get() ? "BitcoinAverage" : "Poloniex";
//    }
//
//
//    private String getPriceProviderTooltipString() {
//
//        String res;
//        if (model.getIsFiatCurrencyPriceFeedSelected().get()) {
//            res = Res.get("mainView.marketPrice.tooltip",
//                    "https://bitcoinaverage.com",
//                    "",
//                    formatter.formatTime(model.getPriceFeedService().getLastRequestTimeStampBtcAverage()),
//                    model.getPriceFeedService().getProviderNodeAddress());
//        } else {
//            String altcoinExtra = "\n" + Res.get("mainView.marketPrice.tooltip.altcoinExtra");
//            res = Res.get("mainView.marketPrice.tooltip",
//                    "https://poloniex.com",
//                    altcoinExtra,
//                    formatter.formatTime(model.getPriceFeedService().getLastRequestTimeStampPoloniex()),
//                    model.getPriceFeedService().getProviderNodeAddress());
//        }
//        return res;
//    }

    private VBox createSplashScreen() {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setId("splash");

        ImageView logo = new ImageView();
        logo.setId("image-splash-logo");

        // createBitcoinInfoBox
//        btcSplashInfo = new AutoTooltipLabel();
//        btcSplashInfo.textProperty().bind(model.getBtcInfo());
//        walletServiceErrorMsgListener = (ov, oldValue, newValue) -> {
//            btcSplashInfo.setId("splash-error-state-msg");
//            btcSplashInfo.getStyleClass().add("error-text");
//        };
//        model.getWalletServiceErrorMsg().addListener(walletServiceErrorMsgListener);
//
//        btcSyncIndicator = new JFXProgressBar();
//        btcSyncIndicator.setPrefWidth(305);
//        btcSyncIndicator.progressProperty().bind(model.getCombinedSyncProgress());
//
//        ImageView btcSyncIcon = new ImageView();
//        btcSyncIcon.setVisible(false);
//        btcSyncIcon.setManaged(false);
//
//        btcSyncIconIdListener = (ov, oldValue, newValue) -> {
//            btcSyncIcon.setId(newValue);
//            btcSyncIcon.setVisible(true);
//            btcSyncIcon.setManaged(true);
//
//            btcSyncIndicator.setVisible(false);
//            btcSyncIndicator.setManaged(false);
//        };
//        model.getBtcSplashSyncIconId().addListener(btcSyncIconIdListener);
//
//
//        HBox blockchainSyncBox = new HBox();
//        blockchainSyncBox.setSpacing(10);
//        blockchainSyncBox.setAlignment(Pos.CENTER);
//        blockchainSyncBox.setPadding(new Insets(40, 0, 0, 0));
//        blockchainSyncBox.setPrefHeight(50);
//        blockchainSyncBox.getChildren().addAll(btcSplashInfo, btcSyncIcon);


        // create P2PNetworkBox
//        splashP2PNetworkLabel = new AutoTooltipLabel();
//        splashP2PNetworkLabel.setWrapText(true);
//        splashP2PNetworkLabel.setMaxWidth(500);
//        splashP2PNetworkLabel.setTextAlignment(TextAlignment.CENTER);
//        splashP2PNetworkLabel.getStyleClass().add("sub-info");
//        splashP2PNetworkLabel.textProperty().bind(model.getP2PNetworkInfo());
//
//        Button showTorNetworkSettingsButton = new AutoTooltipButton(Res.get("settings.net.openTorSettingsButton"));
//        showTorNetworkSettingsButton.setVisible(false);
//        showTorNetworkSettingsButton.setManaged(false);
//        showTorNetworkSettingsButton.setOnAction(e -> model.getTorNetworkSettingsWindow().show());
//
//        splashP2PNetworkBusyAnimation = new BusyAnimation(false);
//
//        splashP2PNetworkErrorMsgListener = (ov, oldValue, newValue) -> {
//            if (newValue != null) {
//                splashP2PNetworkLabel.setId("splash-error-state-msg");
//                splashP2PNetworkLabel.getStyleClass().remove("sub-info");
//                splashP2PNetworkLabel.getStyleClass().add("error-text");
//                splashP2PNetworkBusyAnimation.setDisable(true);
//                splashP2PNetworkBusyAnimation.stop();
//                showTorNetworkSettingsButton.setVisible(true);
//                showTorNetworkSettingsButton.setManaged(true);
//                if (model.getUseTorForBTC().get()) {
//                    // If using tor for BTC, hide the BTC status since tor is not working
//                    btcSyncIndicator.setVisible(false);
//                    btcSplashInfo.setVisible(false);
//                }
//            } else if (model.getSplashP2PNetworkAnimationVisible().get()) {
//                splashP2PNetworkBusyAnimation.setDisable(false);
//                splashP2PNetworkBusyAnimation.play();
//            }
//        };
//        model.getP2pNetworkWarnMsg().addListener(splashP2PNetworkErrorMsgListener);

//        ImageView splashP2PNetworkIcon = new ImageView();
//        splashP2PNetworkIcon.setId("image-connection-tor");
//        splashP2PNetworkIcon.setVisible(false);
//        splashP2PNetworkIcon.setManaged(false);
//        HBox.setMargin(splashP2PNetworkIcon, new Insets(0, 0, 5, 0));
//
//        Timer showTorNetworkSettingsTimer = UserThread.runAfter(() -> {
//            showTorNetworkSettingsButton.setVisible(true);
//            showTorNetworkSettingsButton.setManaged(true);
//        }, SHOW_TOR_SETTINGS_DELAY_SEC);
//
//        splashP2PNetworkIconIdListener = (ov, oldValue, newValue) -> {
//            splashP2PNetworkIcon.setId(newValue);
//            splashP2PNetworkIcon.setVisible(true);
//            splashP2PNetworkIcon.setManaged(true);
//
//            // if we can connect in 10 sec. we know that tor is working
//            showTorNetworkSettingsTimer.stop();
//        };
//        model.getP2PNetworkIconId().addListener(splashP2PNetworkIconIdListener);
//
//        splashP2PNetworkVisibleListener = (ov, oldValue, newValue) -> {
//            splashP2PNetworkBusyAnimation.setDisable(!newValue);
//            if (newValue) splashP2PNetworkBusyAnimation.play();
//        };
//
//        model.getSplashP2PNetworkAnimationVisible().addListener(splashP2PNetworkVisibleListener);
//
//        HBox splashP2PNetworkBox = new HBox();
//        splashP2PNetworkBox.setSpacing(10);
//        splashP2PNetworkBox.setAlignment(Pos.CENTER);
//        splashP2PNetworkBox.setPrefHeight(40);
//        splashP2PNetworkBox.getChildren().addAll(splashP2PNetworkLabel, splashP2PNetworkBusyAnimation, splashP2PNetworkIcon, showTorNetworkSettingsButton);
//
//        vBox.getChildren().addAll(logo, blockchainSyncBox, btcSyncIndicator, splashP2PNetworkBox);
        vBox.getChildren().add(logo);
        return vBox;
    }

    private void disposeSplashScreen() {
//        model.getWalletServiceErrorMsg().removeListener(walletServiceErrorMsgListener);
//        model.getBtcSplashSyncIconId().removeListener(btcSyncIconIdListener);
//
//        model.getP2pNetworkWarnMsg().removeListener(splashP2PNetworkErrorMsgListener);
//        model.getP2PNetworkIconId().removeListener(splashP2PNetworkIconIdListener);
//        model.getSplashP2PNetworkAnimationVisible().removeListener(splashP2PNetworkVisibleListener);
//
//        btcSplashInfo.textProperty().unbind();
//        btcSyncIndicator.progressProperty().unbind();
//
//        splashP2PNetworkLabel.textProperty().unbind();
//
//        model.onSplashScreenRemoved();
    }

    private AnchorPane createFooter() {
        // line
        Separator separator = new Separator();
        separator.setId("footer-pane-line");
        separator.setPrefHeight(1);
        setLeftAnchor(separator, 0d);
        setRightAnchor(separator, 0d);
        setTopAnchor(separator, 0d);

        // BTC
//        Label btcInfoLabel = new AutoTooltipLabel();
//        btcInfoLabel.setId("footer-pane");
//        btcInfoLabel.textProperty().bind(model.getBtcInfo());
//
//        ProgressBar blockchainSyncIndicator = new JFXProgressBar(-1);
//        blockchainSyncIndicator.setPrefWidth(80);
//        blockchainSyncIndicator.setMaxHeight(10);
//        blockchainSyncIndicator.progressProperty().bind(model.getCombinedSyncProgress());
//
//        model.getWalletServiceErrorMsg().addListener((ov, oldValue, newValue) -> {
//            if (newValue != null) {
//                btcInfoLabel.setId("splash-error-state-msg");
//                btcInfoLabel.getStyleClass().add("error-text");
//                if (btcNetworkWarnMsgPopup == null) {
//                    btcNetworkWarnMsgPopup = new Popup<>().warning(newValue);
//                    btcNetworkWarnMsgPopup.show();
//                }
//            } else {
//                btcInfoLabel.setId("footer-pane");
//                if (btcNetworkWarnMsgPopup != null)
//                    btcNetworkWarnMsgPopup.hide();
//            }
//        });
//
//        model.getCombinedSyncProgress().addListener((ov, oldValue, newValue) -> {
//            if ((double) newValue >= 1) {
//                blockchainSyncIndicator.setVisible(false);
//                blockchainSyncIndicator.setManaged(false);
//            }
//        });

//        HBox blockchainSyncBox = new HBox();
//        blockchainSyncBox.setSpacing(10);
//        blockchainSyncBox.setAlignment(Pos.CENTER);
//        blockchainSyncBox.getChildren().addAll(btcInfoLabel, blockchainSyncIndicator);
//        setLeftAnchor(blockchainSyncBox, 10d);
//        setBottomAnchor(blockchainSyncBox, 7d);

        // version
        String version = System.getProperty("1m5.version");
        versionLabel = new AutoTooltipLabel();
        versionLabel.setId("footer-pane");
        versionLabel.setTextAlignment(TextAlignment.CENTER);
        versionLabel.setAlignment(Pos.BASELINE_CENTER);
        versionLabel.setText("v" + version);
//        rootContainer.widthProperty().addListener((ov, oldValue, newValue) -> {
//            versionLabel.setLayoutX(((double) newValue - versionLabel.getWidth()) / 2);
//        });
        setBottomAnchor(versionLabel, 7d);
//        model.getNewVersionAvailableProperty().addListener((observable, oldValue, newValue) -> {
//            versionLabel.getStyleClass().removeAll("version-new", "version");
//            if (newValue) {
//                versionLabel.getStyleClass().add("version-new");
//                versionLabel.setOnMouseClicked(e -> model.onOpenDownloadWindow());
//                versionLabel.setText("v" +version+ " " + Resources.get("homeView.version.update"));
//            } else {
//                versionLabel.getStyleClass().add("version");
//                versionLabel.setOnMouseClicked(null);
//                versionLabel.setText("v" + version;
//            }
//        });

        // P2P Networks
//        Label p2PNetworkLabel = new AutoTooltipLabel();
//        p2PNetworkLabel.setId("footer-pane");
//        p2PNetworkLabel.textProperty().bind(model.getP2PNetworkInfo());
//
//        ImageView p2PNetworkIcon = new ImageView();
//        setRightAnchor(p2PNetworkIcon, 10d);
//        setBottomAnchor(p2PNetworkIcon, 5d);
//        p2PNetworkIcon.setOpacity(0.4);
//        p2PNetworkIcon.idProperty().bind(model.getP2PNetworkIconId());
//        p2PNetworkLabel.idProperty().bind(model.getP2pNetworkLabelId());
//        model.getP2pNetworkWarnMsg().addListener((ov, oldValue, newValue) -> {
//            if (newValue != null) {
//                p2PNetworkWarnMsgPopup = new Popup<>().warning(newValue);
//                p2PNetworkWarnMsgPopup.show();
//            } else if (p2PNetworkWarnMsgPopup != null) {
//                p2PNetworkWarnMsgPopup.hide();
//            }
//        });
//
//        model.getUpdatedDataReceived().addListener((observable, oldValue, newValue) -> {
//            p2PNetworkIcon.setOpacity(1);
//            p2pNetworkProgressBar.setProgress(0);
//        });
//
//        p2pNetworkProgressBar = new JFXProgressBar(-1);
//        p2pNetworkProgressBar.setMaxHeight(2);
//        p2pNetworkProgressBar.prefWidthProperty().bind(p2PNetworkLabel.widthProperty());
//
//        VBox vBox = new VBox();
//        vBox.setAlignment(Pos.CENTER_RIGHT);
//        vBox.getChildren().addAll(p2PNetworkLabel, p2pNetworkProgressBar);
//        setRightAnchor(vBox, 33d);
//        setBottomAnchor(vBox, 5d);

//        return new AnchorPane(separator, blockchainSyncBox, versionLabel, vBox, p2PNetworkIcon) {{
//            setId("footer-pane");
//            setMinHeight(30);
//            setMaxHeight(30);
//        }};

        // Tor Network
        Label torNetworkLabel = new AutoTooltipLabel();
        torNetworkLabel.setId("footer-pane");

        // I2P Network
        Label i2pNetworkLabel = new AutoTooltipLabel();
        i2pNetworkLabel.setId("footer-pane");

        // Bluetooth Network
        Label bluetoothNetworkLabel = new AutoTooltipLabel();
        bluetoothNetworkLabel.setId("footer-pane");

        // WiFi-Direct Network
        Label wifiDirectNetworkLabel = new AutoTooltipLabel();
        wifiDirectNetworkLabel.setId("footer-pane");

        // Satellite Network
        Label satelliteNetworkLabel = new AutoTooltipLabel();
        satelliteNetworkLabel.setId("footer-pane");

        // Full Spectrum Radio Network
        Label fsRadioNetworkLabel = new AutoTooltipLabel();
        fsRadioNetworkLabel.setId("footer-pane");

        // LiFi Network
        Label lifiNetworkLabel = new AutoTooltipLabel();
        lifiNetworkLabel.setId("footer-pane");

        return new AnchorPane(separator, versionLabel) {{
            setId("footer-pane");
            setMinHeight(30);
            setMaxHeight(30);
        }};
    }

    private void setupBadge(Badge buttonWithBadge, StringProperty badgeNumber, BooleanProperty badgeEnabled) {
        buttonWithBadge.textProperty().bind(badgeNumber);
        buttonWithBadge.setEnabled(badgeEnabled.get());
        badgeEnabled.addListener((observable, oldValue, newValue) -> {
            buttonWithBadge.setEnabled(newValue);
            buttonWithBadge.refreshBadge();
        });

        buttonWithBadge.setPosition(Pos.TOP_RIGHT);
        buttonWithBadge.setMinHeight(34);
        buttonWithBadge.setMaxHeight(34);
    }

    private class NavButton extends AutoTooltipToggleButton {

        private final Class<? extends View> viewClass;

        NavButton(Class<? extends View> viewClass, String title) {
            super(title);

            this.viewClass = viewClass;

            this.setToggleGroup(navButtons);
            this.getStyleClass().add("nav-button");
            // Japanese fonts are dense, increase top nav button text size
//            if (model.getPreferences().getUserLanguage().equals("ja")) {
//                this.getStyleClass().add("nav-button-japanese");
//            }

            this.selectedProperty().addListener((ov, oldValue, newValue) -> this.setMouseTransparent(newValue));

            this.setOnAction(e -> MVC.navigation.navigateTo(HomeView.class, viewClass));
        }

    }

    private class ManConComboBoxItem {
        public final ManCon manConLevel;
        public final ImageView manConImageView;

        public ManConComboBoxItem(ManCon manConLevel) {
            this.manConLevel = manConLevel;
            this.manConImageView = new ImageView(new Image(Resources.getManConIcon(manConLevel).toString()));
        }

    }

}
