package workbenchfx.perspective.metadata.mode;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import com.sforce.soap.metadata.FileProperties;

public class RetrieveNavigatorController {
	
	private RetrieveMode mode;
	
	private Map<String, Map<String, SortedMap<String, FileProperties>>> retrievesProperties = new HashMap<>();
	
	private AnchorPane root;
	private BorderPane toolBarPane;
	private ToolBar toolBar;
	private Button saveButton;
	private TreeView<String> retrievesTree;
	private TreeItem<String> retrievesTreeRoot;
	
	public RetrieveNavigatorController(RetrieveMode mode) {
		this.mode = mode;
		createGraph();
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void addRetrieve(String name, Map<String, SortedMap<String, FileProperties>> properties, TreeItem<String> tree) {
		
		retrievesProperties.put(name, properties);
		retrievesTree.getRoot().getChildren().add(tree);
		tree.setExpanded(true);
	}

	private void createGraph() {
		
		root = new AnchorPane();
		
		toolBarPane = new BorderPane();
		AnchorPane.setTopAnchor(toolBarPane, 0.0);
		AnchorPane.setLeftAnchor(toolBarPane, 0.0);
		AnchorPane.setRightAnchor(toolBarPane, 0.0);
		root.getChildren().add(toolBarPane);
		
		toolBar = new ToolBar();
		toolBarPane.setCenter(toolBar);
		
		saveButton = new Button("Save");
		saveButton.setDisable(true);
		toolBar.getItems().add(saveButton);
		
		retrievesTree = new TreeView<>();
		retrievesTree.setShowRoot(false);
		retrievesTree.setOnMouseClicked(e -> handleRetrievesTreeItemClicked(e));
		AnchorPane.setTopAnchor(retrievesTree, 38.0);
		AnchorPane.setBottomAnchor(retrievesTree, 0.0);
		AnchorPane.setLeftAnchor(retrievesTree, 0.0);
		AnchorPane.setRightAnchor(retrievesTree, 0.0);
		root.getChildren().add(retrievesTree);
		
		retrievesTreeRoot = new TreeItem<>("");
		retrievesTreeRoot.setExpanded(true);
		retrievesTree.setRoot(retrievesTreeRoot);
	}
	
	private void handleRetrievesTreeItemClicked(MouseEvent e) {
		
		setDisablesForRetrievesTreeSelection();
		showPropertiesForRetrievesTreeSelection();
		
		if (e.getClickCount() == 2) {
			
			TreeItem<String> selectedItem = retrievesTree.getSelectionModel().getSelectedItem();
			if (selectedItem == null) {
				// Should never get here
				return;
			}
			
			if (selectedItem == retrievesTree.getRoot()) {
				// Root of retrieves tree is hidden
				return;
			}
			
			if (selectedItem.getParent() == retrievesTree.getRoot()) {
				// This is the root of a single retrieve
				return;
			}
			
			if (selectedItem.getParent().getParent() == retrievesTree.getRoot()) {
				// This is a type for a single retrieve
				return;
			}
			
			if (!selectedItem.isLeaf()) {
				// Playing it safe. This is still a work in progress and the structure of the tree
				// is still in development
				return;
			}
			
			boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
			if (connected) {
				String retrieveName = selectedItem.getParent().getParent().getValue();
				String fileName = selectedItem.getValue();
				mode.getEditorController().show(retrieveName, fileName);
			}
		}
	}
	
	private void setDisablesForRetrievesTreeSelection() {

	}
	
	private void showPropertiesForRetrievesTreeSelection() {
		
		TreeItem<String> selectedItem = retrievesTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			// Should never get here
			mode.getPropertiesViewerController().showPropertiesForFile(null);
			return;
		}
		
		if (selectedItem == retrievesTree.getRoot()) {
			// Root of retrieves tree is hidden
			mode.getPropertiesViewerController().showPropertiesForFile(null);
			return;
		}
		
		if (selectedItem.getParent() == retrievesTree.getRoot()) {
			// This is the root of a single retrieve
			mode.getPropertiesViewerController().showPropertiesForFile(null);
			return;
		}
		
		if (selectedItem.getParent().getParent() == retrievesTree.getRoot()) {
			// This is a type for a single retrieve
			mode.getPropertiesViewerController().showPropertiesForFile(null);
			return;
		}
		
		if (!selectedItem.isLeaf()) {
			// Playing it safe. This is still a work in progress and the structure of the tree
			// is still in development
			mode.getPropertiesViewerController().showPropertiesForFile(null);
			return;
		}
		
		String retrieveName = selectedItem.getParent().getParent().getValue();
		String typeName = selectedItem.getParent().getValue();
		String fileName = selectedItem.getValue();
		
		Map<String, SortedMap<String, FileProperties>> retrieveProperties = retrievesProperties.get(retrieveName);	
		SortedMap<String, FileProperties> retrievePropertiesForType = retrieveProperties.get(typeName);	
		FileProperties fileProperties = retrievePropertiesForType.get(fileName);
		mode.getPropertiesViewerController().showPropertiesForFile(fileProperties);
	}
}
