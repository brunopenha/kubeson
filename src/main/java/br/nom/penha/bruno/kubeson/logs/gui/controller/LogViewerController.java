package br.nom.penha.bruno.kubeson.logs.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class LogViewerController implements Initializable {


    @FXML
    private ListView<String> logListView;

    @FXML
    private TextArea jsonDetailArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Exemplo: listener de seleção
        logListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // buscar e exibir detalhes JSON
            jsonDetailArea.setText("Detalhe do log: " + newVal);
        });
    }

}