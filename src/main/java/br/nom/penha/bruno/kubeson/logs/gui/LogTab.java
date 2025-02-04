package br.nom.penha.bruno.kubeson.logs.gui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import br.nom.penha.bruno.kubeson.Configuration;
import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.controller.K8SClient;
import br.nom.penha.bruno.kubeson.common.controller.K8SClientListener;
import br.nom.penha.bruno.kubeson.common.controller.K8SResourceChange;
import br.nom.penha.bruno.kubeson.common.gui.TabBase;
import br.nom.penha.bruno.kubeson.common.gui.TabLabel;
import br.nom.penha.bruno.kubeson.common.model.ItemType;
import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.K8SPod;
import br.nom.penha.bruno.kubeson.common.model.PodLogFeedListener;
import br.nom.penha.bruno.kubeson.common.model.SelectedItem;
import br.nom.penha.bruno.kubeson.common.util.TreeList;
import br.nom.penha.bruno.kubeson.logs.model.HttpMethod;
import br.nom.penha.bruno.kubeson.logs.model.LogCategory;
import br.nom.penha.bruno.kubeson.logs.model.LogLevel;
import br.nom.penha.bruno.kubeson.logs.model.LogLine;
import br.nom.penha.bruno.kubeson.logs.model.LogLineContainer;
import br.nom.penha.bruno.kubeson.logs.model.LogSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTab extends TabBase<LogToolbar> {

    private static Logger LOGGER = LogManager.getLogger(LogTab.class);

    private int logLineId;

    private SplitPane tabSplitPane;

    private LogListView logListView;

    private JsonViewerPane jsonViewerPane;

    private List<SelectedItem> selectedItems;

    private ObservableList<LogLineContainer> logLines;

    private FilteredList<LogLineContainer> filteredLogLines;

    private Set<LogLevel> logLevelStates;

    private Map<LogCategory, Set<HttpMethod>> logCategoryStates;

    private PodLogFeedListener podLogFeedListener;

    private K8SClientListener k8sListener;

    private SearchManager searchManager;

    private int running;

    private long lastLogLineTime;

    private int logIdColorIdx;

    private int logSourceColorIdx;

    public LogTab(File logFile, TabLabel tabLabel) {
        super(tabLabel);
        LOGGER.debug("Creating tab for file [" + logFile + "]");

        super.setTooltip(new Tooltip(logFile.toString()));
        init();

        super.setOnClosed((event) -> {
            logLines.clear();
            logListView.dispose();
        });

        //Read file content
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line = reader.readLine();
            while (line != null) {
                LogLine logLine = new LogLine(line);
                logLines.add(new LogLineContainer(null, logLine, logLineId(), Configuration.LOG_ID_COLORS[0]));
                checkEnableJsonViewer(logLine);
                line = reader.readLine();
            }
        } catch (IOException e) {
            String message = "Failed to read file \"" + logFile + "\"";
            LOGGER.error(message, e);
            Main.showErrorMessage(message, e);
        }
    }

    public LogTab(List<SelectedItem> selectedItems, TabLabel tabLabel, boolean showLogsFromStart) {
        super(tabLabel);

        selectedItems.forEach(item -> LOGGER.debug("Starting tab '{}' for {}", tabLabel.getText(), item.getPod()));

        this.selectedItems = selectedItems;
        setRunning(true);

        if (selectedItems.size() > 1) {
            StringBuilder sb = new StringBuilder();
            selectedItems.forEach(item -> sb.append(item.getText()).append('\n'));
            super.setTooltip(new Tooltip(sb.toString()));
        }

        this.podLogFeedListener = new PodLogFeedListener() {

            @Override
            public void onNewLogLine(LogSource logSource, LogLine logLine) {
                Platform.runLater(() -> addItem(logSource, logLine));
            }

            @Override
            public void onLogLineRemoved(LogLine logLine) {
                Platform.runLater(() -> {
                    logLines.remove(new LogLineContainer(logLine));
                });
            }

            @Override
            public void onPodLogFeedTerminated(K8SPod pod) {
                for (SelectedItem selectedItem : selectedItems) {
                    if (selectedItem.isRunning() && !selectedItem.getPod().equals(pod)) {
                        return;
                    }
                }
                setRunning(false);
                Platform.runLater(() -> printPodTerminatedMessage());
            }
        };

        this.k8sListener = new K8SClientListener() {

            @Override
            public void onPodChange(K8SResourceChange<K8SPod> changes) {
                if (running >= 0) {
                    for (K8SPod newPod : changes.getAdded()) {
                        for (SelectedItem selectedItem : selectedItems) {
                            if (selectedItem.getType() == ItemType.LABEL && selectedItem.getText().equals(newPod.getAppLabel())) {
                                LOGGER.debug("New {}. Stopping previous log stream and starting stream for new pod", newPod);
                                selectedItem.getPod().removeListener(podLogFeedListener, false);

                                if (selectedItems.size() == 1) {
                                    reset();
                                }

                                // Wait a little before starting the new pod log stream
                                try {
                                    TimeUnit.MILLISECONDS.sleep(500);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    LOGGER.error("Thread interruption received before starting stream for new pod");
                                    return;
                                }

                                newPod.addListener(null,
                                        getLogSource(selectedItem.getText(), selectedItems.size()),
                                        podLogFeedListener,
                                        true);
                                selectedItem.setPod(newPod);
                                setRunning(true);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onConfigMapChange(K8SResourceChange<K8SConfigMap> changes) {

            }
        };
        K8SClient.addListener(k8sListener);
        init();

        super.setOnClosed((event) -> {
            logLines.clear();
            logListView.dispose();
            selectedItems.forEach(selectedItem -> selectedItem.getPod().removeListener(this.podLogFeedListener, false));
            K8SClient.removeListener(k8sListener);
        });

        // Start printing log lines
        selectedItems.forEach(selectedItem -> {
            LOGGER.error("selectedItem -> " + selectedItem.getText());
            if(null != selectedItem.getPod()){
                selectedItem.getPod()
                        .addListener(selectedItem.getContainer(), getLogSource(selectedItem.getText(), selectedItems.size()), this.podLogFeedListener,
                                showLogsFromStart);
            }
        });
    }

    private void init() {
        super.setToolbar(new LogToolbar(this));
        this.logLines = FXCollections.synchronizedObservableList(FXCollections.observableList(new TreeList<>()));
        //this.logLines = FXCollections.observableList(new TreeList<>());
        this.filteredLogLines = new FilteredList<>(logLines, s -> true);
        this.searchManager = new SearchManager(this);
        this.logListView = new LogListView(this);
        this.jsonViewerPane = new JsonViewerPane(this);
        this.tabSplitPane = new SplitPane(this.logListView.draw());
        this.tabSplitPane.setStyle("-fx-background-color: black;-fx-control-inner-background: black;");
        this.logLevelStates = new HashSet<>();
        this.logCategoryStates = new HashMap<>();

        super.setContent(this.tabSplitPane);
    }

    private LogSource getLogSource(String name, int size) {
        if (size <= 1) {
            return null;
        }
        final Color color = Configuration.LOG_SOURCE_COLORS[logSourceColorIdx];
        logSourceColorIdx++;
        logSourceColorIdx = logSourceColorIdx % Configuration.LOG_SOURCE_COLORS.length;

        return new LogSource(name, color);
    }

    public void stop() {
        stop(false);
    }

    public void stop(boolean keepLogSource) {
        if (running == 1) {
            selectedItems.forEach(selectedItem -> selectedItem.getPod().removeListener(podLogFeedListener, keepLogSource));
            Platform.runLater(this::printLogFeedStoppedMessage);
            setRunning(false);
        }
    }

    public void reset() {
        Platform.runLater(() -> {
            logLines.clear();
            logListView.reset();
        });
        logLineId = 0;
        lastLogLineTime = 0;
        logIdColorIdx = 0;
    }

    private int logLineId() {
        return ++logLineId;
    }

    private void addItem(LogSource logSource, LogLine logLine) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogLineTime > 5000) {
            logIdColorIdx++;
            logIdColorIdx = logIdColorIdx % 2;
        }
        logLines.add(new LogLineContainer(logSource, logLine, logLineId(), Configuration.LOG_ID_COLORS[logIdColorIdx]));
        lastLogLineTime = currentTime;
        checkEnableJsonViewer(logLine);
    }

    private void checkEnableJsonViewer(LogLine logLine) {
        if (!jsonViewerPane.isDrawn() && logLine.isJson()) {
            tabSplitPane.setDividerPosition(0, Configuration.LOG_LIST_PANEL_SPLIT);
            tabSplitPane.getItems().add(jsonViewerPane.draw());
        }
    }

    private void printPodTerminatedMessage() {
        msgLogLine("");
        msgLogLine("*************************************************");
        msgLogLine("                   KUBERNETES POD TERMINATED");
        msgLogLine("*************************************************");
        msgLogLine("");
    }

    private void printLogFeedStoppedMessage() {
        msgLogLine("");
        msgLogLine("*************************************************");
        msgLogLine("                           LOG FEED STOPPED");
        msgLogLine("*************************************************");
        msgLogLine("");
    }

    private void msgLogLine(String text) {
        logLines.add(new LogLineContainer(null, new LogLine(text, true), 0, null));
    }

/*
    private void printNewPodStartingMessage(String podName) {
        Color color = Color.GREEN;
        String msg = "STARTING POD: " + podName;
        String filler = StringUtils.repeat('*', msg.length() + 22);
        logLines.add(new LogLine(logLineId(), filler, color));
        logLines.add(new LogLine(logLineId(), "          " + msg, color));
        logLines.add(new LogLine(logLineId(), filler, color));
        logLines.add(new LogLine(logLineId(), "", color));
    }
*/

    public void filter(Object filterValue, Object subFilterValue, boolean state) {
        if (filterValue instanceof LogLevel) {
            if (state) {
                logLevelStates.add((LogLevel) filterValue);
            } else {
                logLevelStates.remove(filterValue);
            }
        }
        if (filterValue instanceof LogCategory) {
            if (state) {
                Set<HttpMethod> httpMethods = logCategoryStates.get(filterValue);
                if (httpMethods == null) {
                    httpMethods = new HashSet<>();
                }
                if (subFilterValue != null) {
                    httpMethods.add((HttpMethod) subFilterValue);
                } else {
                    httpMethods.clear();
                }
                logCategoryStates.put((LogCategory) filterValue, httpMethods);
            } else {
                if (subFilterValue != null) {
                    Set<HttpMethod> httpMethods = logCategoryStates.get(filterValue);
                    httpMethods.remove(subFilterValue);
                    if (httpMethods.isEmpty()) {
                        logCategoryStates.remove(filterValue);
                    }
                } else {
                    logCategoryStates.remove(filterValue);
                }
            }
        }
        filter();
    }

    private void filter() {
        jsonViewerPane.clear();
        filteredLogLines.setPredicate(logLineContainer -> {
            if (!logLevelStates.isEmpty() && !logLevelStates.contains(logLineContainer.getLogLine().getLogLevel())) {
                return false;
            }
            if (!logCategoryStates.isEmpty()) {
                Set<HttpMethod> httpMethods = logCategoryStates.get(logLineContainer.getLogLine().getLogCategory());
                if (httpMethods == null || (!httpMethods.isEmpty() && !httpMethods.contains(logLineContainer.getLogLine().getHttpMethod()))) {
                    return false;
                }
            }
            return true;
        });
        searchManager.refresh();
    }

    public void stopAndContinueInNewTab() {
        stop(true);
        running = -1;
        TabPane tabPane = super.getTabPane();
        int pos = tabPane.getTabs().indexOf(this) + 1;

        LogTab logTab = new LogTab(selectedItems, getTabLabel().clone(), false);
        tabPane.getTabs().add(pos, logTab);
        tabPane.getSelectionModel().select(pos);
    }

    public FilteredList<LogLineContainer> getLogLines() {
        return filteredLogLines;
    }

    public void clearAll() {
        searchManager.clear();
        logListView.refresh();
    }

    public LogListView getLogListView() {
        return logListView;
    }

    public JsonViewerPane getJsonViewerPane() {
        return jsonViewerPane;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public boolean isRunning() {
        return running == 1;
    }

    private void setRunning(boolean running) {
        if (running) {
            this.running = 1;
        } else {
            this.running = 0;
        }
        getTabLabel().setErrorColor(!running);
    }

    @Override
    public void onSelected() {
        super.onSelected();
        logListView.requestFocus();
    }
}
