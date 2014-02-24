package workbenchfx.editor.metadata;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;

import com.sforce.soap.metadata.Metadata;

public interface Editor {

	Node getRoot();
	
	Node getToolBarRoot();
	
	Metadata getMetadata();
	void setMetadata(Metadata metadata);
	
	String getMetadataAsXml();
	void setMetadataAsXml(String xml);
	
	BooleanProperty dirty();
	
	void lock();
	void unlock();
	
	void setEditable(boolean editable);
	boolean isEditable();
}
