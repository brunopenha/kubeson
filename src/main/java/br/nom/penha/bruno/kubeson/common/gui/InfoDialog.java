package br.nom.penha.bruno.kubeson.common.gui;

import br.nom.penha.bruno.kubeson.Configuration;
import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.controller.Upgrade;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class InfoDialog extends Alert {

    private Button upgradeButton;

    private ProgressBar progressBar;

    private Label upgradeMessage;

    InfoDialog() {
        super(AlertType.INFORMATION);

        upgradeButton = new Button("Upgrade");
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        upgradeMessage = new Label();

        upgradeButton.setOnAction(me -> Upgrade.startDownload());

        progressBar.progressProperty().unbind();
        //FIXME progressBar.progressProperty().bind(Upgrade.getDownloadWorker().progressProperty());

        //FIXME refreshUpgrade();

        setTitle(Configuration.APP_NAME);

        setHeaderText("Kubeson - Version: " + getClass().getPackage().getImplementationVersion());

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        Label lbl1 = new Label("Kubeson provides an interface to visualize JSON logs generated by kubernetes' pods");
        Label lbl2 = new Label("Created by: Felipe Vianna Perez");
        Label lbl3 = new Label("Adapted by: Bruno C. Penha");
        Label lbl4 = new Label("Licensed under Apache License Version 2.0");

        vbox.getChildren().addAll(lbl1, lbl2, lbl3, lbl4, upgradeButton, upgradeMessage, progressBar);

        getDialogPane().contentProperty().set(vbox);
        String appCss = getClass().getResource("/css/application.css").toExternalForm();
        getDialogPane().getStylesheets().add(appCss);
        getDialogPane().getStyleClass().add("info-dialog");

        // Set Icon
        Stage primaryStage = Main.getPrimaryStage();
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().addAll(Main.getAppIcons());
        stage.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 340);
        stage.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 220);
    }

    void refreshUpgrade() {
        switch (Upgrade.getState()) {
            case DOWNLOADING:
                upgradeButton.setDisable(true);
                progressBar.setVisible(true);
                upgradeMessage.setVisible(true);
                break;

            case UPGRADE_AVAILABLE:
            case UPGRADE_ERROR:
                upgradeButton.setDisable(false);
                progressBar.setVisible(false);
                upgradeMessage.setVisible(true);
                break;

            case VALIDATING:
            case UNPACKING:
            case UPGRADE_SUCCESSFUL:
                upgradeButton.setDisable(true);
                progressBar.setVisible(false);
                upgradeMessage.setVisible(true);
                break;

            default:
                upgradeButton.setDisable(true);
                progressBar.setVisible(false);
                upgradeMessage.setVisible(false);
        }
        upgradeMessage.setText(Upgrade.getMessage());
    }
}
