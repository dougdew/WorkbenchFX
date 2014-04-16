package workbenchfx.perspective;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

import workbenchfx.Main;

public class PerspectiveController {
	
	private static final String CSS_FILE = "perspective.css";
	
	private static final String MD = "Metadata";
	private static final String TOOLING = "Tooling";
	private static final String DATA = "Data";
	private static final String CONNECT = "Connect";
	
	public static String getCSSFileName() {
		return CSS_FILE;
	}
	
	private Main application;
	
	private ToolBar toolBarRoot;
	private HBox perspectiveBox;
	private ToggleGroup perspectiveGroup;
	private ToggleButton mdApiButton;
	private ToggleButton toolingApiButton;
	private ToggleButton dataApiButton;
	private ToggleButton connectApiButton;
	
	private ObjectProperty<Perspective> activePerspectiveProperty = new SimpleObjectProperty<>();
	
	private Map<PerspectiveFactory.Type, Perspective> perspectives = new HashMap<>();
	private Map<String, PerspectiveFactory.Type> perspectiveTypes = new HashMap<>();
	{
		perspectiveTypes.put(MD, PerspectiveFactory.Type.METADATA);
		perspectiveTypes.put(TOOLING, PerspectiveFactory.Type.TOOLING);
		perspectiveTypes.put(DATA, PerspectiveFactory.Type.DATA);
		perspectiveTypes.put(CONNECT, PerspectiveFactory.Type.CONNECT);
	}
	
	
	public PerspectiveController(Main application) {
		this.application = application;
		createToolBarGraph();
	}
	
	public ObjectProperty<Perspective> activePerspective() {
		return activePerspectiveProperty;
	}
	
	public Node getToolBarRoot() {
		return toolBarRoot;
	}
	
	public Node getPerspectiveRoot() {

		if (activePerspectiveProperty.get() == null) {
			ToggleButton selectedButton = (ToggleButton)perspectiveGroup.getSelectedToggle();
			setActivePerspective(perspectiveTypes.get(selectedButton.getText()));
		}
		
		return activePerspectiveProperty.get().getRoot();
	}
	
	public Node getPerspectiveModeToolBarRoot() {
		
		if (activePerspectiveProperty.get() != null) {
			return activePerspectiveProperty.get().getModeToolBarRoot();
		}
		else {
			return null;
		}
	}

	private void createToolBarGraph() {
		
		toolBarRoot = new ToolBar();
		
		perspectiveBox = new HBox();
		toolBarRoot.getItems().add(perspectiveBox);
		
		perspectiveGroup = new ToggleGroup();
		
		mdApiButton = new ToggleButton(MD);
		mdApiButton.getStyleClass().add("left-pill");
		mdApiButton.setToggleGroup(perspectiveGroup);
		perspectiveBox.getChildren().add(mdApiButton);
		
		toolingApiButton = new ToggleButton(TOOLING);
		toolingApiButton.getStyleClass().add("right-pill");
		toolingApiButton.setToggleGroup(perspectiveGroup);
		perspectiveBox.getChildren().add(toolingApiButton);
		
		/*
		dataApiButton = new ToggleButton(DATA);
		dataApiButton.getStyleClass().add("center-pill");
		dataApiButton.setToggleGroup(perspectiveGroup);
		perspectiveBox.getChildren().add(dataApiButton);
		
		connectApiButton = new ToggleButton(CONNECT);
		connectApiButton.getStyleClass().add("right-pill");
		connectApiButton.setToggleGroup(perspectiveGroup);
		perspectiveBox.getChildren().add(connectApiButton);
		*/
		
		perspectiveGroup.selectToggle(mdApiButton);
		perspectiveGroup.selectedToggleProperty().addListener(e -> handlePerspectiveToggleChanged());
	}
	
	private void handlePerspectiveToggleChanged() {
		ToggleButton selectedButton = (ToggleButton)perspectiveGroup.getSelectedToggle();
		if (selectedButton != null) {
			setActivePerspective(perspectiveTypes.get(selectedButton.getText()));
		}
	}
	
	private void setActivePerspective(PerspectiveFactory.Type perspectiveType) {
		if (!perspectives.containsKey(perspectiveType)) {
			Perspective perspective = PerspectiveFactory.createPerspective(perspectiveType, application);
			// TODO: Get rid of this
			if (perspective != null) {
				perspectives.put(perspectiveType, perspective);
			}
		}
		
		// TODO: Change this to not have to check first
		if (perspectives.containsKey(perspectiveType)) {
			Perspective perspective = perspectives.get(perspectiveType);
			perspective.captureLog();
			activePerspectiveProperty.set(perspective);
		}
	}
}
