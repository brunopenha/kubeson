package com.fvp.kubeson.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fvp.kubeson.model.SelectedItem;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class LogTabPane {

    private static TabPane logTabPane;

    private static List<TabListener> tabListeners;

    static {
        tabListeners = new ArrayList<>();
        initLogTabPane();
    }

    private LogTabPane() {
    }

    private static void initLogTabPane() {
        logTabPane = new TabPane();
        logTabPane.setStyle("-fx-background-color: black;");
        logTabPane.setFocusTraversable(false);
        logTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            LogTab logTab = (LogTab) newValue;
            if (logTab != null) {
                tabListeners.forEach(listener -> listener.onTabChange(logTab));
                logTab.getLogListView().requestFocus();
            }
        });
        logTabPane.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN) {
                keyEvent.consume();
            }
        });
        logTabPane.setOnDragOver((event) -> {
            if (event.getGestureSource() != logTabPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        logTabPane.setOnDragDropped((event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    success = createTab(file);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        VBox.setVgrow(logTabPane, Priority.ALWAYS);
    }

    public static Parent draw() {
        return logTabPane;
    }

    public static void addListener(TabListener tabListener) {
        tabListeners.add(tabListener);
    }

    public static boolean createTab(List<SelectedItem> selectedItems, String name) {
        // Return false if Tab with same name already exists
        for (Tab tab : logTabPane.getTabs()) {
            if (tab.getText().equals(name)) {
                return false;
            }
        }
        LogTab logTab = new LogTab(selectedItems, name, true);
        logTabPane.getTabs().add(logTab);
        logTabPane.getSelectionModel().selectLast();

        return true;
    }

    private static boolean createTab(File logFile) {
        // Return false if Tab with same name already exists
        for (Tab tab : logTabPane.getTabs()) {
            if (tab.getTooltip() != null && logFile.toString().equals(tab.getTooltip().getText())) {
                return false;
            }
        }
        LogTab logTab = new LogTab(logFile);
        logTabPane.getTabs().add(logTab);
        logTabPane.getSelectionModel().selectLast();

        return true;
    }

    static void broadcastOnTabClosed(LogTab logTab) {
        tabListeners.forEach(listener -> listener.onTabClosed(logTab));
    }
}
