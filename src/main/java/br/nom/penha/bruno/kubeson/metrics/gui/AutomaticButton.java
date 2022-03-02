package br.nom.penha.bruno.kubeson.metrics.gui;

import br.nom.penha.bruno.kubeson.common.gui.ToggleButtonBase;

public final class AutomaticButton extends ToggleButtonBase {

    public AutomaticButton(MetricsTab metricTab) {
        super("/icons/automatic_35x35.png", "_AUTOMATIC REFRESH");
        super.setOnAction(event -> metricTab.setAutomaticRefresh(super.isSelected()));
    }
}
