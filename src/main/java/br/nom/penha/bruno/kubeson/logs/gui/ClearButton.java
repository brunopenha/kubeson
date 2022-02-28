package br.nom.penha.bruno.kubeson.logs.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class ClearButton extends ButtonBase {

    public ClearButton(LogTab logTab) {
        super("icons/clear.png", "CLEA_R LOG");
        super.setOnAction(event -> logTab.reset());
    }
}
