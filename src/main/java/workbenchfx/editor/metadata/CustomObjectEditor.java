package workbenchfx.editor.metadata;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.Metadata;

import workbenchfx.Main;

public class CustomObjectEditor implements Editor {

	private static final String GRAPH_FILE = "CustomObjectEditor.fxml";
	private static final String TYPE = "CustomObject";
	private static final String FILE_EXTENSION = "object";
	
	private Main application;
	
	private BooleanProperty dirtyProperty = new SimpleBooleanProperty();
	
	public CustomObjectEditor(Main application) {
		this.application = application;
	}
	
	public static String getType() {
		return TYPE;
	}
	
	public static String getFileExtension() {
		return FILE_EXTENSION;
	}
	
	private CustomObject metadata;
	
	@FXML
	private AnchorPane root;
	
	@FXML
	private TextField label;
	
	@FXML
	private TextField pluralLabel;
	
	public CustomObjectEditor() {
		loadGraph();
	}
	
	public Node getRoot() {
		return root;
	}
	
	public Node getToolBarRoot() {
		return null;
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(Metadata metadata) {
		if (metadata != null && !(metadata instanceof CustomObject)) {
			return;
		}
		if (metadata != null) {
			this.metadata = (CustomObject)metadata;
		}
		else {
			metadata = null;
		}
		if (metadata != null) {
			setUIFromMetadata();
		}
	}
	
	public String getMetadataAsXml() {
		// TODO:
		return null;
	}
	
	public void setMetadataAsXml(String xml) {
		// TODO:
	}
	
	public BooleanProperty dirty() {
		return dirtyProperty;
	}
	
	public void lock() {
		// TODO:
	}
	
	public void unlock() {
		// TODO:
	}
	
	public void setEditable(boolean editable) {
		// TODO
	}
	
	public boolean isEditable() {
		// TODO
		return false;
	}
	
	private void loadGraph() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(GRAPH_FILE));
		loader.setController(this);
		try {
			root = (AnchorPane)loader.load();
		}
		catch (IOException e) {
			root = new AnchorPane();
			Label errorMessage = new Label("Failed to load graph file");
			root.getChildren().add(errorMessage);
		}
	}
	
	private void setUIFromMetadata() {
		if (metadata == null) {
			return;
		}
		
		label.setText(metadata.getLabel());
		pluralLabel.setText(metadata.getPluralLabel());
	}
	
	private void setMetadataFromUI() {
		if (metadata == null) {
			return;
		}
	}
}
