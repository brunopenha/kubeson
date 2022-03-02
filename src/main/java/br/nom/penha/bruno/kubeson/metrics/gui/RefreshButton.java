package br.nom.penha.bruno.kubeson.metrics.gui;

import br.nom.penha.bruno.kubeson.common.gui.ButtonBase;

public final class RefreshButton extends ButtonBase {

    public RefreshButton(MetricsTab metricTab) {
        super("/icons/refresh_35x35.png", "_REFRESH");
        super.setOnAction(event -> metricTab.refreshMetrics());
    }
}