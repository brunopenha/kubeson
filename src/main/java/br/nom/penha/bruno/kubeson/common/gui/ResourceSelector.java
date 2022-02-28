package br.nom.penha.bruno.kubeson.common.gui;

import br.nom.penha.bruno.kubeson.common.controller.K8SClient;
import br.nom.penha.bruno.kubeson.common.controller.K8SClientListener;
import br.nom.penha.bruno.kubeson.common.controller.K8SResourceChange;
import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.K8SPod;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public final class ResourceSelector {

    private static final String DEFAULT_NAMESPACE = "default";

    private static ChoiceBox<String> namespaceBox;

    private static ResourceComboBox resourceBox;

    private static String selectedNamespace;

    static {
        selectedNamespace = DEFAULT_NAMESPACE;
        init();
    }

    private ResourceSelector() {
    }

    private static void init() {
        namespaceBox = new ChoiceBox<>();
        namespaceBox.setMinWidth(110);
        namespaceBox.maxWidth(110);
        namespaceBox.setPrefWidth(110);
        namespaceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedNamespace = newValue;
                updatePodNameBox();
            }
        });

        resourceBox = new ResourceComboBox();

        K8SClient.addListener(new K8SClientListener() {

            @Override
            public void onPodChange(K8SResourceChange<K8SPod> changes) {
                Platform.runLater(ResourceSelector::update);
            }

            @Override
            public void onConfigMapChange(K8SResourceChange<K8SConfigMap> changes) {
                Platform.runLater(ResourceSelector::update);
            }
        });
    }

    private static void update() {
        updateNamespaceBox();
        updatePodNameBox();
    }

    private static void updateNamespaceBox() {
        namespaceBox.getItems().clear();
        namespaceBox.getItems().addAll(K8SClient.getNamespaces());
        namespaceBox.getSelectionModel().select(selectedNamespace);
    }

    private static void updatePodNameBox() {
        resourceBox.getItems().clear();
        if (K8SClient.getNamespaces().contains(selectedNamespace)) {
            resourceBox.getItems().addAll(K8SClient.getPodSelectorList(selectedNamespace));
        }
    }

    public static Parent draw() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(4);
        gridPane.setVgap(2.5);
        Text t1 = new Text("Namespace");
        t1.getStyleClass().add("selector-label");
        t1.setFill(Color.WHITE);
        gridPane.add(t1, 1, 1);
        gridPane.add(namespaceBox, 1, 2);
        Text t2 = new Text("Resource");
        t2.getStyleClass().add("selector-label");
        t2.setFill(Color.WHITE);
        gridPane.add(t2, 2, 1);
        gridPane.add(resourceBox, 2, 2);

        return gridPane;
    }

}
