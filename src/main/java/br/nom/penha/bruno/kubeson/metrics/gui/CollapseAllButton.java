package br.nom.penha.bruno.kubeson.metrics.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class CollapseAllButton extends ButtonBase {

    public CollapseAllButton(MetricsTab metricTab) {
        super("/icons/collapse_35x35.png", "COLLA_PSE ALL");
        super.setOnAction(event -> metricTab.setAllExpanded(false));
    }
}
