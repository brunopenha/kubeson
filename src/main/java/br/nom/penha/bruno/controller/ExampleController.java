package br.nom.penha.bruno.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ExampleController implements Initializable {

    @FXML
    private Tab tab1;
    @FXML
    private Label lblNum1;
    @FXML
    private Label lblNum2;
    @FXML
    private TextField tfNum1;
    @FXML
    private TextField tfNum2;
    @FXML
    private Button btnSoma;
    @FXML
    private Tab tab2;
    @FXML
    private TextArea taInformacao;
    @FXML
    private Button btnRet;
    @FXML
    private TabPane tp;

    @FXML
    private void somaBtnHandler(ActionEvent evento){
        double num1 = Double.parseDouble(tfNum1.getText());
        double num2 = Double.parseDouble(tfNum2.getText());

        double total = num1 +num2;
        taInformacao.appendText(String.valueOf(total));
        taInformacao.appendText("\n");

        tp.getSelectionModel().select(tab2);
    }

    @FXML
    private void returnBtnHandler(ActionEvent evento){
        tp.getSelectionModel().select(tab1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}