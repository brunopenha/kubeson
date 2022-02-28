package br.nom.penha.bruno.kubeson.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class DeletePodsButton extends ButtonBase {

    public DeletePodsButton(ConfigMapTab configMapTab) {
        super("icons/delete_35x35.png", "_DELETE PODS");
        super.setOnAction(event -> configMapTab.deletePods());
    }
}
