package workbenchfx.perspective;

import java.util.HashMap;
import java.util.Map;

import workbenchfx.Main;
import workbenchfx.perspective.connect.ConnectPerspective;
import workbenchfx.perspective.data.DataPerspective;
import workbenchfx.perspective.metadata.MetadataPerspective;
import workbenchfx.perspective.tooling.ToolingPerspective;

public class PerspectiveFactory {

	public enum Type {
		CONNECT,
		DATA,
		METADATA,
		TOOLING
	}
	
	private interface PerspectiveCreator {
		Perspective create(Main application);
	}
	
	private static Map<Type, PerspectiveCreator> creators;
	static {
		creators = new HashMap<>();
		creators.put(Type.CONNECT, application -> new ConnectPerspective(application));
		creators.put(Type.DATA, application -> new DataPerspective(application));
		creators.put(Type.METADATA, application -> new MetadataPerspective(application));
		creators.put(Type.TOOLING, application -> new ToolingPerspective(application));
	}
	
	public static Perspective createPerspective(Type type, Main application) {
		if (creators.containsKey(type)) {
			return creators.get(type).create(application);
		}
		else {
			return null;
		}
	}
}
