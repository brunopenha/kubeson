package br.nom.penha.bruno.kubeson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javafx.scene.paint.Color;

public final class Configuration {

    private final Properties propriedadesK8s;

    public static final String APP_NAME = "Kubeson - Kubernetes Json Log Viewer";

    public static final Color[] LOG_ID_COLORS = new Color[]{Color.CORNFLOWERBLUE, Color.GREEN};

    public static final Color SYSTEM_MSG_COLOR = Color.GREEN;

    public static final Color[] LOG_SOURCE_COLORS = new Color[]{
            Color.web("#00bfff"), Color.web("#ff00ff"), Color.web("#94b8b8"), Color.web("#bf00ff"), Color.web("#ffccff"), Color.web("#ff794d"),
            Color.web("#a3a3c2"), Color.web("#a3a375"), Color.web("#d27979"), Color.web("#6666cc")
    };

    public static final int MAX_JSON_FIELD_SIZE = 1200;

    public static final int MAX_JSON_SIZE = MAX_JSON_FIELD_SIZE + 1000;

    public static final String MAX_JSON_FIELD_MESSAGE = "******* CONTENT REMOVED, FIELD SIZE=%d *******";

    public static final int MAX_LOG_LINES = 10000; //70

    public static final int MAX_KUBERNETES_CLIENT_ATTEMPTS = 3;

    public static final int CLIPBOARD_COPY_MAX_LOG_LINES = 500;

    public static final double LOG_LIST_PANEL_SPLIT = 0.7;

    public static final int KUBERNETES_WORKER_WAIT_TIME_MS = 500;

    public static final List<String> KUBERNETES_APP_LABELS = Arrays.asList("app", "app.kubernetes.io/name");

    public static final String GITHUB_TOKEN = "invalido";

    public static final String GITHUB_RELEASES = "https://api.github.com/repos/brunopenha/kubeson/releases";

    public static final int CHECK_FOR_UPGRADE_WORKER_WAIT_TIME_MS = 43200000;

    public static final String METRICS_IP = "192.168.99.100";

    public static final String METRICS_KUBERNETES_SERVICE_PORT_NAME = "metrics";

    public static final int MAX_METRICS_VALUE_HISTORY = 6;

    public static final long METRICS_AUTOMATIC_REFRESH_DELAY_MS = 2000;

    public Configuration() {
        this.propriedadesK8s = new Properties();

        try {
            propriedadesK8s.load(getClass().getResourceAsStream("/configuracoes.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
