package br.nom.penha.bruno;

import br.nom.penha.bruno.kubeson.common.gui.MainToolbar;
import br.nom.penha.bruno.kubeson.common.util.ThreadFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Inicio extends Application {

	private static final Logger logger = LogManager.getLogger(Inicio.class);
	private static Application application;
	private static Stage primaryStage;

	public static void main(String[] args) {
		logger.info("Running app with Java Version " + System.getProperty("java.version") + " Arch " + System.getProperty("sun.arch.data.model"));

		// Configurações do sistema (semelhante à classe Main)
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		System.setProperty("java.net.useSystemProxies", "true");
		System.setProperty("log4j2.disable.jmx", Boolean.TRUE.toString());

		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		// Define o manipulador de exceções e as referências estáticas
		Thread.setDefaultUncaughtExceptionHandler(Inicio::logError);
		Inicio.application = this;
		Inicio.primaryStage = stage;

		// 1. Cria um VBox como layout raiz, assim como na classe Main
		VBox root = new VBox();

		// 2. Carrega o conteúdo da sua tela principal a partir do FXML
		Parent logViewerContent = FXMLLoader.load(getClass().getResource("/paginas_fxml/LogViewer.fxml"));
		// Faz com que o conteúdo do log cresça para preencher o espaço vertical disponível
		VBox.setVgrow(logViewerContent, Priority.ALWAYS);

		// 3. Adiciona a barra de ferramentas e o conteúdo do FXML ao layout raiz
		root.getChildren().addAll(MainToolbar.draw(), logViewerContent);

		// 4. Cria a cena com um tamanho padrão
		Scene scene = new Scene(root, 1600, 800);

		// 5. Aplica a folha de estilos
		String appCss = getClass().getResource("/css/application.css").toExternalForm();
		scene.getStylesheets().add(appCss);

		// 6. Configura e exibe a janela principal (Stage)
		stage.setTitle("Kubeson - Log Viewer");
		stage.getIcons().addAll(getAppIcons());
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.setOnCloseRequest(event -> ThreadFactory.shutdownAll());
		stage.show();
	}

	private static void logError(Thread t, Throwable e) {
		if (Platform.isFxApplicationThread()) {
			logger.error("An error occurred in JavaFx thread", e);
		} else {
			logger.error("An unexpected error occurred", e);
		}
	}

	public static List<Image> getAppIcons() {
		List<Image> ret = new ArrayList<>();
		Image img = getImage("/icons/app16.png");
		if (img != null) {
			ret.add(img);
		}
		img = getImage("/icons/app32.png");
		if (img != null) {
			ret.add(img);
		}
		return ret;
	}


	public static Image getImage(String path) {
		InputStream is = Inicio.class.getResourceAsStream(path);
		if (is != null) {
			return new Image(is);
		}
		return null;
	}

	private WebView preLoadJsonViewerPage() {
		WebView webview = new WebView();
		webview.setVisible(false);
		webview.setPrefSize(0, 0);
		WebEngine webEngine = webview.getEngine();
		String jsonViewer = this.getClass().getClassLoader().getResource("json-viewer/index.html").toExternalForm();
		webEngine.load(jsonViewer);
		return webview;
	}
}
