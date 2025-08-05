package br.nom.penha.bruno.kubeson.gui;

import br.nom.penha.bruno.kubeson.common.gui.IToolbar;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ConfigMapToolbar extends IToolbar {

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private ConfigMapDataNameSelector configMapDataNameSelector;

    private SaveConfigMapDataButton saveConfigMapDataButton;

    private DeletePodsButton deletePodsButton;

    public ConfigMapToolbar(ConfigMapTab configMapTab) {
        configMapDataNameSelector = new ConfigMapDataNameSelector(configMapTab);
        saveConfigMapDataButton = new SaveConfigMapDataButton(configMapTab);
        deletePodsButton = new DeletePodsButton(configMapTab);

        HBox centralArea = new HBox(configMapDataNameSelector, saveConfigMapDataButton, deletePodsButton);
        centralArea.setSpacing(40);
        centralArea.setAlignment(Pos.CENTER);

        HBox.setHgrow(centralArea, Priority.ALWAYS);

        registerListeners(saveConfigMapDataButton, deletePodsButton);
        addToolbarItem(centralArea);
    }

    public void refreshConfigMapDataNameSelector() {
        configMapDataNameSelector.refresh();
    }

    @FXML
    private void onSave() {
        // Ação de salvar
    }

    @FXML
    private void onDelete() {
        // Ação de deletar
    }

}
