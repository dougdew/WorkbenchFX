package workbenchfx.perspective.metadata.mode;

import javafx.scene.Node;
import workbenchfx.perspective.metadata.MetadataPerspective;
import workbenchfx.perspective.metadata.Mode;

public class MeasureMode implements Mode {
	
	private MetadataPerspective perspective;
	
	public MeasureMode(MetadataPerspective perspective) {
		this.perspective = perspective;
	}

	@Override
	public Node getNavigatorRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getPropertiesViewerRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getEditorRoot() {
		// TODO Auto-generated method stub
		return null;
	}

}
