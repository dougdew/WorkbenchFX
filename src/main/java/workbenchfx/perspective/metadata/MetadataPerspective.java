package workbenchfx.perspective.metadata;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;

import workbenchfx.LogController;
import workbenchfx.Main;
import workbenchfx.perspective.Perspective;

public class MetadataPerspective implements Perspective {
	
	private Main application;
	
	private NavigatorController navigatorController;
	private PropertiesController propertiesController;
	private EditorController editorController;
	private LogController logController;
	
	private SplitPane root;
	private AnchorPane mainLeftPane;
	private AnchorPane mainRightPane;
	private SplitPane leftSplitPane;
	private SplitPane rightSplitPane;
	private AnchorPane editorPane;
	private AnchorPane logPane;
	private Node logGraphRoot;

	public MetadataPerspective(Main application) {
		this.application = application;
		logController = application.getLogController();
		createGraph();
	}
	
	public Main getApplication() {
		return application;
	}
	
	public NavigatorController getNavigatorController() {
		return navigatorController;
	}
	
	public PropertiesController getPropertiesController() {
		return propertiesController;
	}
	
	public EditorController getEditorController() {
		return editorController;
	}
	
	public LogController getLogController() {
		return logController;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void captureLog() {
		addLogToGraph();
	}
	
	private void createGraph() {
		
		// Main split pane
		root = new SplitPane();
		root.setDividerPosition(0, 0.35);
		mainLeftPane = new AnchorPane();
		mainRightPane = new AnchorPane();
		root.getItems().addAll(mainLeftPane, mainRightPane);
		
		// Left pane
		leftSplitPane = new SplitPane();
		leftSplitPane.setOrientation(Orientation.VERTICAL);
		AnchorPane.setTopAnchor(leftSplitPane, 0.0);
		AnchorPane.setBottomAnchor(leftSplitPane, 0.0);
		AnchorPane.setLeftAnchor(leftSplitPane, 0.0);
		AnchorPane.setRightAnchor(leftSplitPane, 0.0);
		mainLeftPane.getChildren().add(leftSplitPane);
		
		// Describe and list pane
		navigatorController = new NavigatorController(this);
		Node describeAndListGraphRoot = navigatorController.getRoot();
		AnchorPane.setTopAnchor(describeAndListGraphRoot, 0.0);
		AnchorPane.setBottomAnchor(describeAndListGraphRoot, 0.0);
		AnchorPane.setLeftAnchor(describeAndListGraphRoot, 0.0);
		AnchorPane.setRightAnchor(describeAndListGraphRoot, 0.0);
		leftSplitPane.getItems().add(describeAndListGraphRoot);
		
		// Properties pane
		propertiesController = new PropertiesController(this);
		Node propertiesGraphRoot = propertiesController.getRoot();
		AnchorPane.setTopAnchor(propertiesGraphRoot, 0.0);
		AnchorPane.setBottomAnchor(propertiesGraphRoot, 0.0);
		AnchorPane.setLeftAnchor(propertiesGraphRoot, 0.0);
		AnchorPane.setRightAnchor(propertiesGraphRoot, 0.0);
		leftSplitPane.getItems().add(propertiesGraphRoot);
		
		// Right split pane
		rightSplitPane = new SplitPane();
		rightSplitPane.setOrientation(Orientation.VERTICAL);
		rightSplitPane.setDividerPosition(0, 0.6);
		AnchorPane.setTopAnchor(rightSplitPane, 0.0);
		AnchorPane.setBottomAnchor(rightSplitPane, 0.0);
		AnchorPane.setLeftAnchor(rightSplitPane, 0.0);
		AnchorPane.setRightAnchor(rightSplitPane, 0.0);
		mainRightPane.getChildren().add(rightSplitPane);
		editorPane = new AnchorPane();
		logPane = new AnchorPane();
		rightSplitPane.getItems().addAll(editorPane, logPane);
		
		// Editor pane
		editorController = new EditorController(this);
		Node editorGraphRoot = editorController.getRoot();
		AnchorPane.setTopAnchor(editorGraphRoot, 0.0);
		AnchorPane.setBottomAnchor(editorGraphRoot, 0.0);
		AnchorPane.setLeftAnchor(editorGraphRoot, 0.0);
		AnchorPane.setRightAnchor(editorGraphRoot, 0.0);
		editorPane.getChildren().add(editorGraphRoot);
	}
	
	private void addLogToGraph() {
		
		if (logPane != null && logGraphRoot != null) {
			logPane.getChildren().remove(logGraphRoot);
			logGraphRoot = null;
		}
		
		logGraphRoot = logController.getRoot();
		AnchorPane.setTopAnchor(logGraphRoot, 0.0);
		AnchorPane.setBottomAnchor(logGraphRoot, 0.0);
		AnchorPane.setLeftAnchor(logGraphRoot, 0.0);
		AnchorPane.setRightAnchor(logGraphRoot, 0.0);
		logPane.getChildren().add(logGraphRoot);
	}
}
