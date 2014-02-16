package workbenchfx.perspective;

import javafx.scene.Node;

public interface Perspective {
	
	Node getRoot();
	
	Node getModeToolBarRoot();
	
	void captureLog();
}
