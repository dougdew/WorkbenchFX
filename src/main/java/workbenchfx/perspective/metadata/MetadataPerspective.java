package workbenchfx.perspective.metadata;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import workbenchfx.LogController;
import workbenchfx.Main;
import workbenchfx.perspective.Perspective;

public class MetadataPerspective implements Perspective {
	
	private Main application;
	
	private ModeController modeController;
	private LogController logController;
	
	private SplitPane root;
	private AnchorPane mainLeftAnchorPane;
	private AnchorPane mainRightAnchorPane;
	private SplitPane leftPane;
	private SplitPane rightPane;
	private AnchorPane editorPane;
	private AnchorPane logPane;
	private Node logGraphRoot;
	private Node navigatorRoot;
	private Node propertiesViewerRoot;
	private Node editorRoot;

	public MetadataPerspective(Main application) {
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
		return modeController.getToolBarRoot();
	}
	
	public void captureLog() {
		addLogToGraph();
	}
	
	private void createGraph() {
		
		modeController = new ModeController(this);
		
		// Main split pane
		root = new SplitPane();
		root.setDividerPosition(0, 0.35);
		mainLeftAnchorPane = new AnchorPane();
		mainRightAnchorPane = new AnchorPane();
		root.getItems().addAll(mainLeftAnchorPane, mainRightAnchorPane);
		
		// Left pane
		leftPane = new SplitPane();
		leftPane.setOrientation(Orientation.VERTICAL);
		AnchorPane.setTopAnchor(leftPane, 0.0);
		AnchorPane.setBottomAnchor(leftPane, 0.0);
		AnchorPane.setLeftAnchor(leftPane, 0.0);
		AnchorPane.setRightAnchor(leftPane, 0.0);
		mainLeftAnchorPane.getChildren().add(leftPane);
		
		// Right pane
		rightPane = new SplitPane();
		rightPane.setOrientation(Orientation.VERTICAL);
		rightPane.setDividerPosition(0, 0.6);
		AnchorPane.setTopAnchor(rightPane, 0.0);
		AnchorPane.setBottomAnchor(rightPane, 0.0);
		AnchorPane.setLeftAnchor(rightPane, 0.0);
		AnchorPane.setRightAnchor(rightPane, 0.0);
		mainRightAnchorPane.getChildren().add(rightPane);
		editorPane = new AnchorPane();
		logPane = new AnchorPane();
		rightPane.getItems().addAll(editorPane, logPane);
		
		handleModeChanged();
		modeController.activeMode().addListener(e -> handleModeChanged());
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
	
	private void handleModeChanged() {
		
		if (navigatorRoot != null) {
			leftPane.getItems().remove(navigatorRoot);
			navigatorRoot = null;
		}
		
		if (propertiesViewerRoot != null) {
			leftPane.getItems().remove(propertiesViewerRoot);
			propertiesViewerRoot = null;
		}
		
		if (editorRoot != null) {
			editorPane.getChildren().remove(editorRoot);
			editorRoot = null;
		}
		
		// Navigator
		navigatorRoot = modeController.getNavigatorRoot();
		if (navigatorRoot == null) {
			navigatorRoot = new TextArea("Navigator: Under Construction");
		}
		AnchorPane.setTopAnchor(navigatorRoot, 0.0);
		AnchorPane.setBottomAnchor(navigatorRoot, 0.0);
		AnchorPane.setLeftAnchor(navigatorRoot, 0.0);
		AnchorPane.setRightAnchor(navigatorRoot, 0.0);
		leftPane.getItems().add(navigatorRoot);
		
		// Properties pane
		propertiesViewerRoot = modeController.getPropertiesViewerRoot();
		if (propertiesViewerRoot == null) {
			propertiesViewerRoot = new TextArea("Properties Viewer: Under Construction");
		}
		AnchorPane.setTopAnchor(propertiesViewerRoot, 0.0);
		AnchorPane.setBottomAnchor(propertiesViewerRoot, 0.0);
		AnchorPane.setLeftAnchor(propertiesViewerRoot, 0.0);
		AnchorPane.setRightAnchor(propertiesViewerRoot, 0.0);
		leftPane.getItems().add(propertiesViewerRoot);
		
		// Editor pane
		editorRoot = modeController.getEditorRoot();
		if (editorRoot == null) {
			editorRoot = new TextArea("Editor: Under Construction");
		}
		AnchorPane.setTopAnchor(editorRoot, 0.0);
		AnchorPane.setBottomAnchor(editorRoot, 0.0);
		AnchorPane.setLeftAnchor(editorRoot, 0.0);
		AnchorPane.setRightAnchor(editorRoot, 0.0);
		editorPane.getChildren().add(editorRoot);
	}
}
