package br.nom.penha.bruno.kubeson.common.gui;

import br.nom.penha.bruno.kubeson.common.controller.K8SClient;
import br.nom.penha.bruno.kubeson.common.controller.K8SClientListener;
import br.nom.penha.bruno.kubeson.common.controller.K8SResourceChange;
import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.K8SPod;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ResourceSelector {

    private static Logger LOGGER = LogManager.getLogger();

    private static final String DEFAULT_NAMESPACE = "default";

    private NamespacedKubernetesClient namespaces;

    private static ChoiceBox<String> namespaceBox;

    private static ObservableList<String> namespaceList = FXCollections.observableArrayList();

    private static ResourceComboBox resourceBox;

    private static String selectedNamespace;

    static {
        //selectedNamespace = DEFAULT_NAMESPACE;
        init();
    }

    private ResourceSelector() {
    }

    private static void init() {

        namespaceBox = new ChoiceBox<>();
        namespaceBox.setMinWidth(110);
        namespaceBox.maxWidth(110);
        namespaceBox.setPrefWidth(110);
        KubernetesClient client  = null;
        NamespaceList lista;
        Config config = new ConfigBuilder()
                .withConnectionTimeout(500) // 2 seconds
                .withRequestTimeout(600) // 3 seconds
                .build();

        try {
            client = new KubernetesClientBuilder().withConfig(config).build();
            client.getVersion();

            lista = client.namespaces().list();
            lista.getItems()
                    .stream()
                    .map((namespace) -> namespace.getMetadata().getName() )
                    .forEach((name) -> namespaceList.add(name));
        } catch (KubernetesClientException e) {
            Platform.runLater(() -> {
                String errorMessage = "Error connecting to Minikube. Please ensure it is running and correctly configured.";
                if (e.getCause() instanceof java.net.ConnectException) {
                    errorMessage = "Could not connect to Minikube. Please make sure it is started.";
                } else if (e.getStatus() != null && (e.getStatus().getCode() == 401 || e.getStatus().getCode() == 403)) {
                    errorMessage = "Authentication/Authorization error connecting to Kubernetes. Please check your kubeconfig.";
                }

                Alert dialog = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
                LOGGER.error("Failed to connect to Kubernetes", e);
                dialog.show();
                dialog.setOnCloseRequest(dialogEvent -> System.exit(1));
            });
        }

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

        namespaceBox.setItems(namespaceList);

    }

    private static void update() {
        updateNamespaceBox();
        updatePodNameBox();
    }

    private static void updateNamespaceBox() {
        namespaceBox.setItems(namespaceList);
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

    public static String getSelectedNamespace() {
        return selectedNamespace;
    }

    public static ObservableList<String> getNamespaceList() {
        return namespaceList;
    }
}
