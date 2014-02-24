package workbenchfx;
	
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.MetadataConnection;

import workbenchfx.perspective.PerspectiveController;

public class Main extends Application {
	
	private static final String TITLE = "WorkbenchFX";
	private static final String CSS_FILE = "application.css";
	private static final String METADATA_NAMESPACE = "http://soap.sforce.com/2006/04/metadata";
	
	private AnchorPane root;
	private BorderPane toolBarPane;
	private HBox toolBarLeftBox;
	private Node loginToolBarRoot;
	private Node perspectiveToolBarRoot;
	private Node perspectiveRoot;
	private Node perspectiveModeToolBarRoot;
	
	private LoginController loginController;
	private PerspectiveController perspectiveController;
	private LogController logController;
	
	private ObjectProperty<EnterpriseConnection> enterpriseConnectionProperty = new SimpleObjectProperty<>();
	private ObjectProperty<MetadataConnection> metadataConnectionProperty = new SimpleObjectProperty<>();
	private DoubleProperty apiVersionProperty = new SimpleDoubleProperty();
	private ObjectProperty<GetUserInfoResult> userInfoProperty = new SimpleObjectProperty<>();
	
	public static String getMetadataNamespace() {
		return METADATA_NAMESPACE;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) {		
		try {
			stage.setTitle(TITLE);
			stage.setScene(createScene());
			stage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {	
		if (loginController != null) {
			loginController.logout();
		}
	}
	
	public LoginController getLoginController() {
		return loginController;
	}
	
	public PerspectiveController getPerspectiveController() {
		return perspectiveController;
	}
	
	public LogController getLogController() {
		return logController;
	}
	
	public ObjectProperty<EnterpriseConnection> enterpriseConnection() {
		return enterpriseConnectionProperty;
	}
	
	public ObjectProperty<MetadataConnection> metadataConnection() {
		return metadataConnectionProperty;
	}
	
	public DoubleProperty apiVersion() {
		return apiVersionProperty;
	}
	
	public ObjectProperty<GetUserInfoResult> userInfo() {
		return userInfoProperty;
	}
	
	private Scene createScene() {
		
		logController = new LogController(this);
		
		root = new AnchorPane();
		Scene scene = new Scene(root, 1200, 800, Color.WHITE);
		scene.getStylesheets().add(getClass().getResource(CSS_FILE).toExternalForm());
		
		// Toolbar pane
		toolBarPane = new BorderPane();
		AnchorPane.setTopAnchor(toolBarPane, 0.0);
		AnchorPane.setLeftAnchor(toolBarPane, 0.0);
		AnchorPane.setRightAnchor(toolBarPane, 0.0);
		root.getChildren().add(toolBarPane);
		
		// Toolbar left box
		toolBarLeftBox = new HBox();
		toolBarPane.setLeft(toolBarLeftBox);
		
		// Login toolbar
		loginController = new LoginController(this);
		loginToolBarRoot = loginController.getRoot();
		//toolBarPane.setLeft(loginToolBarRoot);
		toolBarLeftBox.getChildren().add(loginToolBarRoot);
		
		// Perspective toolbar
		perspectiveController = new PerspectiveController(this);
		perspectiveToolBarRoot = perspectiveController.getToolBarRoot();
		scene.getStylesheets().add(PerspectiveController.class.getResource(PerspectiveController.getCSSFileName()).toExternalForm());
		toolBarPane.setCenter(perspectiveToolBarRoot);
		
		handlePerspectiveChanged();
		perspectiveController.activePerspective().addListener(e -> handlePerspectiveChanged());
		
		return scene;
	}
	
	private void handlePerspectiveChanged() {
		
		if (perspectiveRoot != null) {
			root.getChildren().remove(perspectiveRoot);
			perspectiveRoot = null;
		}
		
		if (perspectiveModeToolBarRoot != null) {
			toolBarPane.getChildren().remove(perspectiveModeToolBarRoot);
			perspectiveModeToolBarRoot = null;
		}
		
		perspectiveRoot = perspectiveController.getPerspectiveRoot();
		AnchorPane.setTopAnchor(perspectiveRoot, 37.0);
		AnchorPane.setBottomAnchor(perspectiveRoot, 0.0);
		AnchorPane.setLeftAnchor(perspectiveRoot, 0.0);
		AnchorPane.setRightAnchor(perspectiveRoot, 0.0);
		root.getChildren().add(perspectiveRoot);
		
		perspectiveModeToolBarRoot = perspectiveController.getPerspectiveModeToolBarRoot();
		if (perspectiveModeToolBarRoot != null) {
			toolBarPane.setCenter(null);
			toolBarLeftBox.getChildren().add(perspectiveToolBarRoot);
			toolBarPane.setCenter(perspectiveModeToolBarRoot);
		}
		else {
			toolBarPane.setCenter(null);
			toolBarLeftBox.getChildren().remove(perspectiveToolBarRoot);
			toolBarPane.setCenter(perspectiveToolBarRoot);
		}
	}
}
