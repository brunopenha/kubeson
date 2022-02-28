package br.nom.penha.bruno.kubeson.metrics.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class ExpandAllButton extends ButtonBase {

    public ExpandAllButton(MetricsTab metricTab) {
        super("icons/expand_35x35.png", "_EXPAND ALL");
        super.setOnAction(event -> metricTab.setAllExpanded(true));
    }
}
