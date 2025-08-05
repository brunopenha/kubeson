package br.nom.penha.bruno.kubeson.logs.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class ClearButton extends ButtonBase {

    /**
     * Construtor padrão (sem argumentos) que o FXML precisa para criar o objeto.
     */
    public ClearButton() {
        super("/icons/clear.png", "CLEA_R LOG");
    }

    public ClearButton(LogTab logTab) {
        super("/icons/clear.png", "CLEA_R LOG");
        super.setOnAction(event -> logTab.reset());
    }
}
