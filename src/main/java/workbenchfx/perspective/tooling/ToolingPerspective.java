package workbenchfx.perspective.tooling;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import workbenchfx.LogController;
import workbenchfx.Main;
import workbenchfx.perspective.Perspective;

public class ToolingPerspective implements Perspective {
	
	private Main application;
	
	private LogController logController;
	
	private SplitPane root;
	private AnchorPane mainLeftPane;
	private AnchorPane mainRightPane;
	private SplitPane leftSplitPane;
	private TextArea navigator;
	private TextArea propertiesViewer;
	private SplitPane rightSplitPane;
	private AnchorPane editorPane;
	private TextArea editor;
	private AnchorPane logPane;
	private Node logGraphRoot;

	public ToolingPerspective(Main application) {
		this.application = application;
		logController = application.getLogController();
		createGraph();
	}
	
	public Main getApplication() {
		return application;
	}
	
	public LogController getLogController() {
		return logController;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public Node getModeToolBarRoot() {
		// TODO
		return null;
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
		
		// Navigator pane
		navigator = new TextArea("Navigator: Under Construction");
		AnchorPane.setTopAnchor(navigator, 0.0);
		AnchorPane.setBottomAnchor(navigator, 0.0);
		AnchorPane.setLeftAnchor(navigator, 0.0);
		AnchorPane.setRightAnchor(navigator, 0.0);
		leftSplitPane.getItems().add(navigator);
		
		// Properties pane
		propertiesViewer = new TextArea("Properties Viewer: Under Construction");
		AnchorPane.setTopAnchor(propertiesViewer, 0.0);
		AnchorPane.setBottomAnchor(propertiesViewer, 0.0);
		AnchorPane.setLeftAnchor(propertiesViewer, 0.0);
		AnchorPane.setRightAnchor(propertiesViewer, 0.0);
		leftSplitPane.getItems().add(propertiesViewer);
		
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
		editor = new TextArea("Editor: Under Construction");
		AnchorPane.setTopAnchor(editor, 0.0);
		AnchorPane.setBottomAnchor(editor, 0.0);
		AnchorPane.setLeftAnchor(editor, 0.0);
		AnchorPane.setRightAnchor(editor, 0.0);
		editorPane.getChildren().add(editor);
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
