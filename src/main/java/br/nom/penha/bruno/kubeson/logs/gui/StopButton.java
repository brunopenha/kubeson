package br.nom.penha.bruno.kubeson.logs.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

import java.io.IOException;

public final class StopButton extends ButtonBase {

    public StopButton() {
        super("/icons/stop.png", "_STOP LOG FEED");
        loadFxml();
    }

    // Este construtor é usado quando o botão é criado programaticamente.
    public StopButton(LogTab logTab) {
        super("/icons/stop.png", "_STOP LOG FEED");
        super.setOnAction(event -> logTab.stop());
        loadFxml();
    }

    private void loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/paginas_fxml/StopButton.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar FXML para StopButton", e);
        }
    }

    @FXML
    private void handleStop() {
        // Dispara o evento de clique para que o onAction definido no FXML que USA este componente funcione.
        this.fire();
    }

    /**
     * Como StopButton é um botão, isso aqui pode simplesmente retornar a própria instância.
     */
    public Button getButton() {
        return this;
    }
}