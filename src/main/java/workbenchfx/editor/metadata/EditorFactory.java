package workbenchfx.editor.metadata;

import java.util.HashMap;
import java.util.Map;

import workbenchfx.Main;

public class EditorFactory {

	private interface EditorCreator {
		Editor create(Main application);
	}
	
	private static Map<String, EditorCreator> creators;
	static {
		creators = new HashMap<>();
		creators.put(PackageEditor.getType(), a -> new PackageEditor(a));
		// For now, this is commented out while completing the implementation of the default editor.
		// This will be uncommented once the dual view stuff is implemented.
		//creators.put(CustomObjectEditor.getType(), a -> new CustomObjectEditor(a));
	}
	
	public static Editor createEditor(String type, Main application) {
		if (creators.containsKey(type)) {
			return creators.get(type).create(application);
		}
		else {
			return new DefaultEditor(application);
		}
	}
}
