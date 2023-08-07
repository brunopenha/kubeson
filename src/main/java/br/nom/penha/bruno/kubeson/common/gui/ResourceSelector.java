package br.nom.penha.bruno.kubeson.common.gui;

import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.controller.K8SClient;
import br.nom.penha.bruno.kubeson.common.controller.K8SClientListener;
import br.nom.penha.bruno.kubeson.common.controller.K8SResourceChange;
import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.K8SPod;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.util.Config;
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

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

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
        client =  new KubernetesClientBuilder().build();
        try {
            lista = client.namespaces().list();
            lista.getItems()
                    .stream()
                    .map((namespace) -> namespace.getMetadata().getName() )
                    .forEach((name) -> namespaceList.add(name));
        } catch (KubernetesClientException e) {
            Platform.runLater(() -> {
                Alert dialog = new Alert(Alert.AlertType.ERROR, "Error to connect into minikube: \n Check your minikube then start Kubeson again."
                        , ButtonType.OK);
                LOGGER.error(e.getMessage());
                LOGGER.error(e.getStackTrace());
                dialog.show();
                dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
                    @Override
                    public void handle(DialogEvent dialogEvent) {
                        System.exit(1);
                    }
                });
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
//        namespaceBox.getItems().clear();
        namespaceBox.setItems(namespaceList);
//        namespaceBox.getItems().addAll(K8SClient.getNamespaces());
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
