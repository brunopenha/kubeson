package br.nom.penha.bruno;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Inicio extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/paginas_fxml/LogViewer.fxml"));
		Parent content = loader.load();

		Scene scene = new Scene(content);
		String appCss = getClass().getResource("/css/application.css").toExternalForm();
		scene.getStylesheets().add(appCss);
		primaryStage.getIcons().addAll(getAppIcons());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
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
		InputStream is = Inicio.class.getClassLoader().getResourceAsStream(path);
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
