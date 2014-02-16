package workbenchfx.perspective.metadata;

import javafx.scene.Node;

public interface Mode {

	Node getNavigatorRoot();
	
	Node getPropertiesViewerRoot();
	
	Node getEditorRoot();
}
