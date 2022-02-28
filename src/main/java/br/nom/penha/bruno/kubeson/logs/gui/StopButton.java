package br.nom.penha.bruno.kubeson.logs.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class StopButton extends ButtonBase {

    public StopButton(LogTab logTab) {
        super("icons/stop.png", "_STOP LOG FEED");
        super.setOnAction(event -> logTab.stop());
    }
}
