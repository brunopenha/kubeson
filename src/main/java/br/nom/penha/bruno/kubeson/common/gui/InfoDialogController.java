package br.nom.penha.bruno.kubeson.common.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InfoDialogController {

    @FXML
    private Label infoLabel;

    public void setInfoText(String text) {
        infoLabel.setText(text);
    }
}
