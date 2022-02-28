package br.nom.penha.bruno.kubeson.metrics.gui;

import br.nom.penha.bruno.kubeson.common.gui.IToolbar;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public final class MetricsToolbar extends IToolbar {

    private AutomaticButton automaticButton;

    private RefreshButton refreshButton;

    private ExpandAllButton expandAllButton;

    private CollapseAllButton collapseAllButton;

    public MetricsToolbar(MetricsTab metricTab) {
        //Draw
        automaticButton = new AutomaticButton(metricTab);
        refreshButton = new RefreshButton(metricTab);
        expandAllButton = new ExpandAllButton(metricTab);
        collapseAllButton = new CollapseAllButton(metricTab);

        HBox centralArea = new HBox(automaticButton, refreshButton, collapseAllButton, expandAllButton);
        centralArea.setSpacing(40);
        centralArea.setAlignment(Pos.CENTER);

        HBox.setHgrow(centralArea, Priority.ALWAYS);

        registerListeners(automaticButton, refreshButton, expandAllButton, collapseAllButton);
        addToolbarItem(centralArea);
    }
}
