package workbenchfx.perspective.metadata;

import java.util.HashMap;
import java.util.Map;

import workbenchfx.perspective.metadata.mode.CRUDMode;
import workbenchfx.perspective.metadata.mode.DeployMode;
import workbenchfx.perspective.metadata.mode.MeasureMode;
import workbenchfx.perspective.metadata.mode.RetrieveMode;
import workbenchfx.perspective.metadata.mode.ScriptMode;

public class ModeFactory {

	public enum Type {
		CRUD,
		RETRIEVE,
		DEPLOY,
		MEASURE,
		SCRIPT
	}
	
	private interface ModeCreator {
		Mode create(MetadataPerspective perspective);
	}
	
	private static Map<Type, ModeCreator> creators;
	static {
		creators = new HashMap<>();
		creators.put(Type.CRUD, perspective -> new CRUDMode(perspective));
		creators.put(Type.RETRIEVE, perspective -> new RetrieveMode(perspective));
		creators.put(Type.DEPLOY, perspective -> new DeployMode(perspective));
		creators.put(Type.MEASURE, perspective -> new MeasureMode(perspective));
		creators.put(Type.SCRIPT, perspective -> new ScriptMode(perspective));
	}
	
	public static Mode createMode(Type type, MetadataPerspective perspective) {
		if (creators.containsKey(type)) {
			return creators.get(type).create(perspective);
		}
		else {
			return null;
		}
	}
}