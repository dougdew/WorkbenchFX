package workbenchfx.perspective;

import javafx.scene.Node;

public interface Perspective {
	
	public Node getRoot();
	
	public void captureLog();
}
