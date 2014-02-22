package workbenchfx.perspective.metadata.mode;

import javafx.scene.Node;
import workbenchfx.perspective.metadata.MetadataPerspective;
import workbenchfx.perspective.metadata.Mode;

public class RetrieveMode implements Mode {
	
	private MetadataPerspective perspective;
	private RetrieveNavigatorController navigatorController;
	private RetrievePropertiesViewerController propertiesViewerController;
	private RetrieveEditorController editorController;
	
	public RetrieveMode(MetadataPerspective perspective) {
		this.perspective = perspective;
		navigatorController = new RetrieveNavigatorController(this);
		propertiesViewerController = new RetrievePropertiesViewerController(this);
		editorController = new RetrieveEditorController(this);
	}
	
	public MetadataPerspective getPerspective() {
		return perspective;
	}
	
	public RetrieveNavigatorController getNavigatorController() {
		return navigatorController;
	}
	
	public RetrievePropertiesViewerController getPropertiesViewerController() {
		return propertiesViewerController;
	}
	
	public RetrieveEditorController getEditorController() {
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
