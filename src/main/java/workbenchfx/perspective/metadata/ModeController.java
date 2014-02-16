package workbenchfx.perspective.metadata;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

public class ModeController {
	
	private static final String CRUD = "CRUD";
	private static final String RETRIEVE = "Retrieve";
	private static final String DEPLOY = "Deploy";
	private static final String MEASURE = "Measure";
	private static final String SCRIPT = "Script";
	
	private MetadataPerspective perspective;
	
	private ObjectProperty<Mode> activeModeProperty = new SimpleObjectProperty<>();
	
	private Map<ModeFactory.Type, Mode> modes = new HashMap<>();
	private Map<String, ModeFactory.Type>modeTypes = new HashMap<>(); 
	{
		modeTypes.put(CRUD, ModeFactory.Type.CRUD);
		modeTypes.put(RETRIEVE, ModeFactory.Type.RETRIEVE);
		modeTypes.put(DEPLOY, ModeFactory.Type.DEPLOY);
		modeTypes.put(MEASURE, ModeFactory.Type.MEASURE);
		modeTypes.put(SCRIPT, ModeFactory.Type.SCRIPT);
	}
	
	private ToolBar toolBarRoot;
	private HBox modeBox;
	private ToggleGroup modeGroup;
	private ToggleButton crudButton;
	private ToggleButton retrieveButton;
	private ToggleButton deployButton;
	private ToggleButton measureButton;
	private ToggleButton scriptButton;
	
	public ModeController(MetadataPerspective perspective) {
		this.perspective = perspective;
		createToolBarGraph();
	}
	
	public Node getToolBarRoot() {
		return toolBarRoot;
	}
	
	public ObjectProperty<Mode> activeMode() {
		return activeModeProperty;
	}
	
	public Node getNavigatorRoot() {

		if (activeModeProperty.get() == null) {
			ToggleButton selectedButton = (ToggleButton)modeGroup.getSelectedToggle();
			setActiveMode(modeTypes.get(selectedButton.getText()));
		}
		
		return activeModeProperty.get().getNavigatorRoot();
	}
	
	public Node getPropertiesViewerRoot() {

		if (activeModeProperty.get() == null) {
			ToggleButton selectedButton = (ToggleButton)modeGroup.getSelectedToggle();
			setActiveMode(modeTypes.get(selectedButton.getText()));
		}
		
		return activeModeProperty.get().getPropertiesViewerRoot();
	}
	
	public Node getEditorRoot() {

		if (activeModeProperty.get() == null) {
			ToggleButton selectedButton = (ToggleButton)modeGroup.getSelectedToggle();
			setActiveMode(modeTypes.get(selectedButton.getText()));
		}
		
		return activeModeProperty.get().getEditorRoot();
	}

	private void createToolBarGraph() {
		
		toolBarRoot = new ToolBar();
		
		modeBox = new HBox();
		toolBarRoot.getItems().add(modeBox);
		
		modeGroup = new ToggleGroup();
		
		crudButton = new ToggleButton(CRUD);
		crudButton.getStyleClass().add("left-pill");
		crudButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(crudButton);
		
		retrieveButton = new ToggleButton(RETRIEVE);
		retrieveButton.getStyleClass().add("center-pill");
		retrieveButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(retrieveButton);
		
		deployButton = new ToggleButton(DEPLOY);
		deployButton.getStyleClass().add("center-pill");
		deployButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(deployButton);
		
		measureButton = new ToggleButton(MEASURE);
		measureButton.getStyleClass().add("center-pill");
		measureButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(measureButton);
		
		scriptButton = new ToggleButton(SCRIPT);
		scriptButton.getStyleClass().add("right-pill");
		scriptButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(scriptButton);
		
		modeGroup.selectToggle(crudButton);
		modeGroup.selectedToggleProperty().addListener(e -> handleModeToggleChanged());
	}
	
	private void handleModeToggleChanged() {
		ToggleButton selectedButton = (ToggleButton)modeGroup.getSelectedToggle();
		if (selectedButton != null) {
			setActiveMode(modeTypes.get(selectedButton.getText()));
		}
	}
	
	private void setActiveMode(ModeFactory.Type modeType) {
		if (!modes.containsKey(modeType)) {
			Mode mode = ModeFactory.createMode(modeType, perspective);
			// TODO: Get rid of this
			if (mode != null) {
				modes.put(modeType, mode);
			}
		}
		
		// TODO: Change this to not have to check first
		if (modes.containsKey(modeType)) {
			Mode mode = modes.get(modeType);
			activeModeProperty.set(mode);
		}
	}
}
