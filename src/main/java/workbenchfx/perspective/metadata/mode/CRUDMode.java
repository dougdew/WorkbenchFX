package workbenchfx.perspective.metadata.mode;

import javafx.scene.Node;
import workbenchfx.perspective.metadata.MetadataPerspective;
import workbenchfx.perspective.metadata.Mode;

public class CRUDMode implements Mode {
	
	private MetadataPerspective perspective;
	private CRUDNavigatorController navigatorController;
	private CRUDPropertiesViewerController propertiesViewerController;
	private CRUDEditorController editorController;
	
	public CRUDMode(MetadataPerspective perspective) {
		this.perspective = perspective;
		navigatorController = new CRUDNavigatorController(this);
		propertiesViewerController = new CRUDPropertiesViewerController(this);
		editorController = new CRUDEditorController(this);
	}
	
	public MetadataPerspective getPerspective() {
		return perspective;
	}
	
	public CRUDNavigatorController getNavigatorController() {
		return navigatorController;
	}
	
	public CRUDPropertiesViewerController getPropertiesViewerController() {
		return propertiesViewerController;
	}
	
	public CRUDEditorController getEditorController() {
		return editorController;
	}

	@Override
	public Node getNavigatorRoot() {
		return navigatorController.getRoot();
	}

	@Override
	public Node getPropertiesViewerRoot() {
		return propertiesViewerController.getRoot();
	}

	@Override
	public Node getEditorRoot() {
		return editorController.getRoot();
	}

}
