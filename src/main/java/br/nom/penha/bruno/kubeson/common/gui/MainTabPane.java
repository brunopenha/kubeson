package br.nom.penha.bruno.kubeson.common.gui;

import java.io.File;
import java.util.List;

import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.SelectedItem;
import br.nom.penha.bruno.kubeson.common.model.TabType;
import br.nom.penha.bruno.kubeson.gui.ConfigMapTab;
import br.nom.penha.bruno.kubeson.logs.gui.LogTab;
import br.nom.penha.bruno.kubeson.metrics.gui.MetricsTab;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class MainTabPane {

    private static final KeyCombination CTRL_V = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);

    private static TabPane mainTabPane;

    static {
        initLogTabPane();
    }

    private MainTabPane() {
    }

    public static TabPane getMainTabPane() {
        return mainTabPane;
    }

    private static void initLogTabPane() {
        mainTabPane = new TabPane();
        mainTabPane.setStyle("-fx-background-color: black;");
        mainTabPane.setFocusTraversable(false);
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TabBase tab = (TabBase) newValue;
            if (tab != null) {
                tab.onSelected();
            } else {
                MainToolbar.clear();
            }
        });
        mainTabPane.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN) {
                keyEvent.consume();
            }
        });
        mainTabPane.setOnDragOver((event) -> {
            if (event.getGestureSource() != mainTabPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        mainTabPane.setOnDragDropped((event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    success = createLogTab(file);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);
    }

    public static void onGlobalKeyPressedEvent(KeyEvent keyEvent) {
        if (CTRL_V.match(keyEvent)) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasFiles()) {
                for (File file : clipboard.getFiles()) {
                    createLogTab(file);
                }
            }
        }
    }

    public static Parent draw() {
        return mainTabPane;
    }

    public static boolean createMetricTab(SelectedItem selectedItem, TabLabel tabLabel) {
        // Return false if Tab with same name already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getGraphic().equals(tabLabel)) {
                return false;
            }
        }

        mainTabPane.getTabs().add(new MetricsTab(selectedItem, tabLabel));
        mainTabPane.getSelectionModel().selectLast();

        return true;
    }

    public static boolean createConfigMapTab(K8SConfigMap configMap, TabLabel tabLabel) {
        // Return false if Tab with same name already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getGraphic().equals(tabLabel)) {
                return false;
            }
        }

        mainTabPane.getTabs().add(new ConfigMapTab(configMap, tabLabel));
        mainTabPane.getSelectionModel().selectLast();

        return true;
    }

    public static boolean createLogTab(List<SelectedItem> selectedItems, TabLabel tabLabel) {
        // Return false if Tab with same name already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getGraphic().equals(tabLabel)) {
                return false;
            }
        }

        mainTabPane.getTabs().add(new LogTab(selectedItems, tabLabel, true));
        mainTabPane.getSelectionModel().selectLast();

        return true;
    }

    private static boolean createLogTab(File logFile) {
        // Return false if Tab with same name already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getTooltip() != null && logFile.toString().equals(tab.getTooltip().getText())) {
                return false;
            }
        }
        LogTab logTab = new LogTab(logFile, new TabLabel(TabType.LOG, logFile.getName()));
        mainTabPane.getTabs().add(logTab);
        mainTabPane.getSelectionModel().selectLast();

        return true;
    }

    public static TabBase getSelectedTab() {
        return (TabBase) mainTabPane.getSelectionModel().getSelectedItem();
    }
}
