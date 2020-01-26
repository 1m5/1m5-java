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

import io.onemfive.desktop.Navigation;
import io.onemfive.desktop.Resources;
import io.onemfive.desktop.components.AutoTooltipLabel;
import io.onemfive.desktop.components.AutoTooltipToggleButton;
import io.onemfive.desktop.components.Badge;
import io.onemfive.desktop.util.KeystrokeUtil;
import io.onemfive.desktop.util.TransitionUtil;
import io.onemfive.desktop.views.*;
import io.onemfive.desktop.views.apps.AppsView;
import io.onemfive.desktop.views.browser.BrowserView;
import io.onemfive.desktop.views.calendar.CalendarView;
import io.onemfive.desktop.views.dashboard.DashboardView;
import io.onemfive.desktop.views.identities.IdentitiesView;
import io.onemfive.desktop.views.messenger.MessengerView;
import io.onemfive.desktop.views.settings.SettingsView;
import io.onemfive.desktop.views.support.SupportView;
import io.onemfive.desktop.views.video.VideoView;
import io.onemfive.desktop.views.voice.VoiceView;
import io.onemfive.util.LanguageUtil;
import io.onemfive.util.LocaleUtil;
import io.onemfive.util.Res;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static javafx.scene.layout.AnchorPane.*;

public class HomeView extends InitializableView {

    private StackPane rootContainer;
    private TransitionUtil transitionUtil;
    private Navigation navigation = new Navigation();
    private Label versionLabel;
    private Runnable onUiReadyHandler;
    private final ToggleGroup navButtons = new ToggleGroup();

    public void setTransitionUtil(TransitionUtil transitionUtil) {
        this.transitionUtil = transitionUtil;
    }

    public void setOnUiReadyHandler(Runnable onUiReadyHandler) {
        this.onUiReadyHandler = onUiReadyHandler;
    }

    public StackPane getRootContainer() {
        return rootContainer;
    }

    public void blurLight() {
        transitionUtil.blur(rootContainer, TransitionUtil.DEFAULT_DURATION, -0.6, false, 5);
    }

    public void blurUltraLight() {
        transitionUtil.blur(rootContainer, TransitionUtil.DEFAULT_DURATION, -0.6, false, 2);
    }

    public void darken() {
        transitionUtil.darken(rootContainer, TransitionUtil.DEFAULT_DURATION, false);
    }

    public void removeEffect() {
        transitionUtil.removeEffect(rootContainer);
    }

    @Override
    protected void initialize() {
        LOG.info("Initializing...");
        rootContainer = (StackPane)root;
        if (LanguageUtil.isDefaultLanguageRTL())
            rootContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        final ToggleButton dashboardButton = new NavButton(DashboardView.class, Res.get("homeView.menu.dashboard").toUpperCase());
        final ToggleButton browserButton = new NavButton(BrowserView.class, Res.get("homeView.menu.browser").toUpperCase());
        final ToggleButton messengerButton = new NavButton(MessengerView.class, Res.get("homeView.menu.messenger").toUpperCase());
        final ToggleButton calendarButton = new NavButton(CalendarView.class, Res.get("homeView.menu.calendar").toUpperCase());
        final ToggleButton voiceButton = new NavButton(VoiceView.class, Res.get("homeView.menu.voice").toUpperCase());
        final ToggleButton videoButton = new NavButton(VideoView.class, Res.get("homeView.menu.video").toUpperCase());
        final ToggleButton appsButton = new NavButton(AppsView.class, Res.get("homeView.menu.apps").toUpperCase());
        final ToggleButton identitiesButton = new NavButton(IdentitiesView.class, Res.get("homeView.menu.identities").toUpperCase());
        final ToggleButton supportButton = new NavButton(SupportView.class, Res.get("homeView.menu.support").toUpperCase());
        final ToggleButton settingsButton = new NavButton(SettingsView.class, Res.get("homeView.menu.settings").toUpperCase());

//        Badge emailButtonWithBadge = new Badge(emailButton);
//        Badge messengerButtonWithBadge = new Badge(messengerButton);
//        Badge calendarButtonWithBadge = new Badge(calendarButton);
//        Badge voiceButtonWithBadge = new Badge(voiceButton);
//        Badge videoButtonWithBadge = new Badge(videoButton);
//        Badge appsButtonWithBadge = new Badge(appsButton);
//        daoButtonWithBadge.getStyleClass().add("new");
//        Badge supportButtonWithBadge = new Badge(supportButton);
//        Badge settingsButtonWithBadge = new Badge(settingsButton);

        DecimalFormat currencyFormat = (DecimalFormat) NumberFormat.getNumberInstance(LocaleUtil.currentLocale);
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(2);

        root.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
                    if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT1, keyEvent)) {
                        dashboardButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT2, keyEvent)) {
                        browserButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT4, keyEvent)) {
                        messengerButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT5, keyEvent)) {
                        calendarButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT6, keyEvent)) {
                        voiceButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT7, keyEvent)) {
                        videoButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT8, keyEvent)) {
                        appsButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.DIGIT9, keyEvent)) {
                        identitiesButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.MINUS, keyEvent)) {
                        supportButton.fire();
                    } else if (KeystrokeUtil.isAltOrCtrlPressed(KeyCode.EQUALS, keyEvent)) {
                        if (settingsButton.isVisible())
                            settingsButton.fire();
                    }
                });
            }
        });


//        Tuple2<ComboBox<PriceFeedComboBoxItem>, VBox> marketPriceBox = getMarketPriceBox();
//        ComboBox<PriceFeedComboBoxItem> priceComboBox = marketPriceBox.first;
//
//        priceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            model.setPriceFeedComboBoxItem(newValue);
//        });
//        ChangeListener<PriceFeedComboBoxItem> selectedPriceFeedItemListener = (observable, oldValue, newValue) -> {
//            if (newValue != null)
//                priceComboBox.getSelectionModel().select(newValue);
//
//        };
//        model.getSelectedPriceFeedComboBoxItemProperty().addListener(selectedPriceFeedItemListener);
//        priceComboBox.setItems(model.getPriceFeedComboBoxItems());
//
//        Tuple2<Label, VBox> availableBalanceBox = getBalanceBox(Res.get("mainView.balance.available"));
//        availableBalanceBox.first.textProperty().bind(model.getAvailableBalance());
//        availableBalanceBox.first.setPrefWidth(100);
//        availableBalanceBox.first.tooltipProperty().bind(new ObjectBinding<>() {
//            {
//                bind(model.getAvailableBalance());
//                bind(model.getMarketPrice());
//            }
//
//            @Override
//            protected Tooltip computeValue() {
//                String tooltipText = Res.get("mainView.balance.available");
//                try {
//                    double availableBalance = Double.parseDouble(
//                            model.getAvailableBalance().getValue().replace("BTC", ""));
//                    double marketPrice = Double.parseDouble(model.getMarketPrice().getValue());
//                    tooltipText += "\n" + currencyFormat.format(availableBalance * marketPrice) +
//                            " " + model.getPreferences().getPreferredTradeCurrency().getCode();
//                } catch (NullPointerException | NumberFormatException e) {
//                    // Either the balance or market price is not available yet
//                }
//                return new Tooltip(tooltipText);
//            }
//        });
//
//        Tuple2<Label, VBox> reservedBalanceBox = getBalanceBox(Res.get("mainView.balance.reserved.short"));
//        reservedBalanceBox.first.textProperty().bind(model.getReservedBalance());
//        reservedBalanceBox.first.tooltipProperty().bind(new ObjectBinding<>() {
//            {
//                bind(model.getReservedBalance());
//                bind(model.getMarketPrice());
//            }
//
//            @Override
//            protected Tooltip computeValue() {
//                String tooltipText = Res.get("mainView.balance.reserved");
//                try {
//                    double reservedBalance = Double.parseDouble(
//                            model.getReservedBalance().getValue().replace("BTC", ""));
//                    double marketPrice = Double.parseDouble(model.getMarketPrice().getValue());
//                    tooltipText += "\n" + currencyFormat.format(reservedBalance * marketPrice) +
//                            " " + model.getPreferences().getPreferredTradeCurrency().getCode();
//                } catch (NullPointerException | NumberFormatException e) {
//                    // Either the balance or market price is not available yet
//                }
//                return new Tooltip(tooltipText);
//            }
//        });

//        HBox primaryNav = new HBox(dashboardButton, getNavigationSeparator(), browserButton, getNavigationSeparator(),
//                emailButtonWithBadge, getNavigationSeparator(), messengerButtonWithBadge, getNavigationSeparator(), calendarButtonWithBadge);

        HBox primaryNav = new HBox(
                messengerButton, getNavigationSeparator(),
                browserButton, getNavigationSeparator(),
                videoButton, getNavigationSeparator(),
                voiceButton, getNavigationSeparator(),
                calendarButton);

        primaryNav.setAlignment(Pos.CENTER_LEFT);
        primaryNav.getStyleClass().add("nav-primary");
        HBox.setHgrow(primaryNav, Priority.SOMETIMES);

//        HBox secondaryNav = new HBox(voiceButtonWithBadge, getNavigationSpacer(), videoButtonWithBadge,
//                getNavigationSpacer(), appsButtonWithBadge, getNavigationSpacer(), daoButtonWithBadge,
//                getNavigationSeparator(), identitiesButton, getNavigationSeparator(), supportButtonWithBadge,
//                getNavigationSeparator(), settingsButtonWithBadge);
        HBox secondaryNav = new HBox(
                dashboardButton, getNavigationSeparator(),
                identitiesButton, getNavigationSeparator(),
                appsButton, getNavigationSeparator(),
                supportButton, getNavigationSeparator(),
                settingsButton);

        secondaryNav.getStyleClass().add("nav-secondary");
        secondaryNav.setAlignment(Pos.CENTER);
        HBox.setHgrow(secondaryNav, Priority.SOMETIMES);

//        HBox priceAndBalance = new HBox(marketPriceBox.second, getNavigationSeparator(), availableBalanceBox.second,
//                getNavigationSeparator(), reservedBalanceBox.second, getNavigationSeparator(), lockedBalanceBox.second);
//        priceAndBalance.setMaxHeight(41);
//
//        priceAndBalance.setAlignment(Pos.CENTER);
//        priceAndBalance.setSpacing(11);
//        priceAndBalance.getStyleClass().add("nav-price-balance");

//        HBox navPane = new HBox(primaryNav, secondaryNav,
//                priceAndBalance) {{
//            setLeftAnchor(this, 0d);
//            setRightAnchor(this, 0d);
//            setTopAnchor(this, 0d);
//            setPadding(new Insets(0, 0, 0, 0));
//            getStyleClass().add("top-navigation");
//        }};

        HBox navPane = new HBox(primaryNav, secondaryNav) {{
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
            setTopAnchor(this, 40d);
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

        navigation.addListener(viewPath -> {
            if (viewPath.size() != 2 || viewPath.indexOf(HomeView.class) != 0)
                return;

            Class<? extends View> viewClass = viewPath.tip();
            View view = ViewLoader.load(viewClass);
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
        rootContainer.getChildren().addAll(baseApplicationContainer, splashScreen);
//        rootContainer.getChildren().addAll(baseApplicationContainer);

//        model.getShowAppScreen().addListener((ov, oldValue, newValue) -> {
//            if (newValue) {
//                navigation.navigateToPreviousVisitedView();
//
//                transitionUtil.fadeOutAndRemove(splashScreen, 1500, actionEvent -> disposeSplashScreen());
//                transitionUtil.fadeOutAndRemove(splashScreen, 1500);
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

//    private Tuple2<Label, VBox> getBalanceBox(String text) {
//        Label balanceDisplay = new Label();
//        balanceDisplay.getStyleClass().add("nav-balance-display");
//
//        Label label = new Label(text);
//        label.getStyleClass().add("nav-balance-label");
//        label.maxWidthProperty().bind(balanceDisplay.widthProperty());
//        label.setPadding(new Insets(0, 0, 0, 0));
//        VBox vBox = new VBox();
//        vBox.setAlignment(Pos.CENTER_LEFT);
//        vBox.getChildren().addAll(balanceDisplay, label);
//        return new Tuple2<>(balanceDisplay, vBox);
//    }
//
//    private ListCell<PriceFeedComboBoxItem> getPriceFeedComboBoxListCell() {
//        return new ListCell<PriceFeedComboBoxItem>() {
//            @Override
//            protected void updateItem(PriceFeedComboBoxItem item, boolean empty) {
//                super.updateItem(item, empty);
//
//                if (!empty && item != null) {
//                    textProperty().bind(item.displayStringProperty);
//                } else {
//                    textProperty().unbind();
//                }
//            }
//        };
//    }
//
//    private Tuple2<ComboBox<PriceFeedComboBoxItem>, VBox> getMarketPriceBox() {
//
//        VBox marketPriceBox = new VBox();
//        marketPriceBox.setAlignment(Pos.CENTER_LEFT);
//
//        ComboBox<PriceFeedComboBoxItem> priceComboBox = new JFXComboBox<>();
//        priceComboBox.setVisibleRowCount(12);
//        priceComboBox.setFocusTraversable(false);
//        priceComboBox.setId("price-feed-combo");
//        priceComboBox.setPadding(new Insets(0, 0, -4, 0));
//        priceComboBox.setCellFactory(p -> getPriceFeedComboBoxListCell());
//        ListCell<PriceFeedComboBoxItem> buttonCell = getPriceFeedComboBoxListCell();
//        buttonCell.setId("price-feed-combo");
//        priceComboBox.setButtonCell(buttonCell);
//
//        Label marketPriceLabel = new Label();
//
//        updateMarketPriceLabel(marketPriceLabel);
//
//        marketPriceLabel.getStyleClass().add("nav-balance-label");
//        marketPriceLabel.setPadding(new Insets(-2, 0, 4, 9));
//
//        marketPriceBox.getChildren().addAll(priceComboBox, marketPriceLabel);
//
//        model.getMarketPriceUpdated().addListener((observable, oldValue, newValue) -> {
//            updateMarketPriceLabel(marketPriceLabel);
//        });
//
//        return new Tuple2<>(priceComboBox, marketPriceBox);
//    }
//
//    private String getPriceProvider() {
//        return model.getIsFiatCurrencyPriceFeedSelected().get() ? "BitcoinAverage" : "Poloniex";
//    }
//
//    private void updateMarketPriceLabel(Label label) {
//        if (model.getIsPriceAvailable().get()) {
//            if (model.getIsExternallyProvidedPrice().get()) {
//                label.setText(Res.get("mainView.marketPriceWithProvider.label", getPriceProvider()));
//                label.setTooltip(new Tooltip(getPriceProviderTooltipString()));
//            } else {
//                label.setText(Res.get("mainView.marketPrice.bisqInternalPrice"));
//                final Tooltip tooltip = new Tooltip(Res.get("mainView.marketPrice.tooltip.bisqInternalPrice"));
//                tooltip.getStyleClass().add("market-price-tooltip");
//                label.setTooltip(tooltip);
//            }
//        } else {
//            label.setText("");
//            label.setTooltip(null);
//        }
//    }
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
        rootContainer.widthProperty().addListener((ov, oldValue, newValue) -> {
            versionLabel.setLayoutX(((double) newValue - versionLabel.getWidth()) / 2);
        });
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

        // P2P Network
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

            this.setOnAction(e -> navigation.navigateTo(HomeView.class, viewClass));
        }

    }

}
