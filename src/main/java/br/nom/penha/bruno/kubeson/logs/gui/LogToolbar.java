package br.nom.penha.bruno.kubeson.logs.gui;

import br.nom.penha.bruno.kubeson.common.gui.IToolbar;
import br.nom.penha.bruno.kubeson.common.gui.TabPill;
import br.nom.penha.bruno.kubeson.common.gui.TabPill.Orientation;
import br.nom.penha.bruno.kubeson.logs.model.HttpMethod;
import br.nom.penha.bruno.kubeson.logs.model.LogCategory;
import br.nom.penha.bruno.kubeson.logs.model.LogLevel;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LogToolbar extends IToolbar {

    @FXML
    private ToolBar logToolbar;

    @FXML
    private Button exportButton;

    @FXML
    private TextField searchField;

    private LogTab logTab; // Mantenha uma referência se precisar


    private Text searchCounter;

    @FXML
    private ClearButton clearButton;

    @FXML
    private StopButton stopButton;

    private SearchBox searchBox;

    public LogToolbar(LogTab logTab) {
        // Set Log Level Pill
        TabPill<LogLevel> logLevelPill = new TabPill<>(54, Orientation.HORIZONTAL, logTab, LogLevel.class);

        // Buttons
        clearButton = new ClearButton(logTab);
        stopButton = new StopButton(logTab);
        HBox buttons = new HBox(clearButton, stopButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setStyle("-fx-padding: 0 20 0 20");
        buttons.setSpacing(23);
        buttons.setPrefWidth(Region.USE_PREF_SIZE);
        //HBox.setHgrow(buttons, Priority.ALWAYS);

        // Set Log Category Pill
        TabPill<LogCategory> ulfCategoryPill = new TabPill<>(54, Orientation.HORIZONTAL, logTab, LogCategory.class);

        TabPill<HttpMethod> httpMethodsUp = new TabPill<>(69, Orientation.VERTICAL, logTab, HttpMethod.class);
        ulfCategoryPill.setPopup(1, httpMethodsUp);

        TabPill<HttpMethod> httpMethodsDown = new TabPill<>(69, Orientation.VERTICAL, logTab, HttpMethod.class);
        ulfCategoryPill.setPopup(2, httpMethodsDown);

        HBox centralArea = new HBox(logLevelPill.draw(), buttons, ulfCategoryPill.draw());
        centralArea.setAlignment(Pos.CENTER);
        centralArea.setStyle("-fx-padding: 0 20 0 20");
        //centralArea.setSpacing(23);
        addToolbarItem(centralArea);
        HBox.setHgrow(centralArea, Priority.ALWAYS);

        // Set Search Area
        searchCounter = new Text();
        searchCounter.getStyleClass().add("search-counter");
        searchCounter.setFill(Color.WHITE);

        searchBox = new SearchBox(logTab);

        HBox hBox = new HBox(searchBox, searchCounter);
        hBox.setSpacing(9);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMinWidth(470);
        hBox.prefWidth(470);

        registerListeners(clearButton, stopButton, searchBox);
        addToolbarItem(hBox);
    }

    public SearchBox getSearchBox() {
        return searchBox;
    }

    public void printCounter(String text) {
        searchCounter.setText(text);
    }
    // Construtor padrão usado pelo FXML
    public LogToolbar() {
        super();
    }

    // Para injetar a dependência DEPOIS que o FXML for carregado
    public void setLogTab(LogTab logTab) {
        this.logTab = logTab;

        // Se precisar configurar os botões com o logTab, faça aqui
        // Ex: clearButton.setLogTab(logTab);
        // Ex: stopButton.setLogTab(logTab);
    }

    @FXML
    private void onClear() {
        if (logTab != null) {
            logTab.reset();
            System.out.println("Limpando logs...");
        }
    }

    @FXML
    private void onStop() {
        if (logTab != null) {
            logTab.stop();
            System.out.println("Parando logs...");
        }
    }

    @FXML
    private void onExport() {
        System.out.println("Exportando logs...");
    }

}
