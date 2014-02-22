package workbenchfx.perspective.metadata.mode;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.DeleteResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.ws.ConnectionException;

import workbenchfx.SOAPLogHandler;

public class CRUDNavigatorController {
	
	private static class DescribeWorkerResults {
		
		private SOAPLogHandler logHandler;
		private SortedMap<String, DescribeMetadataObject> description;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setDescription(SortedMap<String, DescribeMetadataObject> description) {
			this.description = description;
		}
		public SortedMap<String, DescribeMetadataObject> getDescription() {
			return description;
		}
	}
	
	private static class DescribeWorker extends Task<DescribeWorkerResults> {
		
		private MetadataConnection connection;
		private double apiVersion;
		
		public DescribeWorker(MetadataConnection connection, double apiVersion) {
			this.connection = connection;
			this.apiVersion = apiVersion;
		}
		
		@Override
		protected DescribeWorkerResults call() throws Exception {
			
			DescribeWorkerResults workerResults = new DescribeWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Describe");
				logHandler.setSummary(String.format("Api Version: %2.1f", apiVersion));
				connection.getConfig().addMessageHandler(logHandler);
				workerResults.setLogHandler(logHandler);
				
				DescribeMetadataResult mdapiDescribe = connection.describeMetadata(apiVersion);
				
				SortedMap<String, DescribeMetadataObject> describeResult = new TreeMap<>();
				for (DescribeMetadataObject dmo : mdapiDescribe.getMetadataObjects()) {
					describeResult.put(dmo.getXmlName(), dmo);
				}
				workerResults.setDescription(describeResult);
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				// TODO: Fix this
				e.printStackTrace();
			}
			return workerResults;
		}
	}
	
	private static class ListWorkerResults {
		
		private SOAPLogHandler logHandler;
		private SortedMap<String, FileProperties> list;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setList(SortedMap<String, FileProperties> list) {
			this.list = list;
		}
		public SortedMap<String, FileProperties> getList() {
			return list;
		}
	}
	
	private static class ListWorker extends Task<ListWorkerResults> {
		
		private MetadataConnection connection;
		private double apiVersion;
		private String typeName;
		
		public ListWorker(MetadataConnection connection, double apiVersion, String typeName) {
			this.connection = connection;
			this.apiVersion = apiVersion;
			this.typeName = typeName;
		}
		
		@Override
		protected ListWorkerResults call() throws Exception {
			
			ListWorkerResults workerResults = new ListWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("List");
				logHandler.setSummary(String.format("Type: %s", typeName));
				connection.getConfig().addMessageHandler(logHandler);
				workerResults.setLogHandler(logHandler);
				
				SortedMap<String, FileProperties> listResult = new TreeMap<>();
				ListMetadataQuery query = new ListMetadataQuery();
				query.setType(typeName);
				
				FileProperties[] mdapiList = connection.listMetadata(new ListMetadataQuery[]{query}, apiVersion);
				
				for (FileProperties fp : mdapiList) {
					listResult.put(fp.getFullName(), fp);
				}
				workerResults.setList(listResult);
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				// TODO: Fix this
				e.printStackTrace();
			}
			
			return workerResults;
		}
	}
	
	private static class DeleteWorkerResults {
		
		private SOAPLogHandler logHandler;
		private boolean success;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public boolean getSuccess() {
			return success;
		}
	}
	
	private static class DeleteWorker extends Task<DeleteWorkerResults> {
		
		private MetadataConnection connection;
		private String typeName;
		private String fullName;
		
		public DeleteWorker(MetadataConnection connection, String typeName, String fullName) {
			this.connection = connection;
			this.typeName = typeName;
			this.fullName = fullName;
		}
		
		@Override
		protected DeleteWorkerResults call() throws Exception {
			
			DeleteWorkerResults workerResults = new DeleteWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Delete");
				logHandler.setSummary(String.format("Type: %s, Full Name: %s", typeName, fullName));
				connection.getConfig().addMessageHandler(logHandler);
				workerResults.setLogHandler(logHandler);
				
				DeleteResult[] mdapiDelete = connection.deleteMetadata(typeName, new String[]{fullName});
				if (mdapiDelete != null && mdapiDelete.length == 1 && mdapiDelete[0].getSuccess()) {
					workerResults.setSuccess(true);
				} 
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				// TODO: Fix this
				e.printStackTrace();
			}
			
			return workerResults;
		}
	}
	
	private static class RenameWorkerResults {
		
		private SOAPLogHandler logHandler;
		private boolean success;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public boolean getSuccess() {
			return success;
		}
	}
	
	private static class RenameWorker extends Task<RenameWorkerResults> {
		
		private MetadataConnection connection;
		private String typeName;
		private String oldFullName;
		private String newFullName;
		
		public RenameWorker(MetadataConnection connection, String typeName, String oldFullName, String newFullName) {
			this.connection = connection;
			this.typeName = typeName;
			this.oldFullName = oldFullName;
			this.newFullName = newFullName;
		}
		
		@Override
		public RenameWorkerResults call() throws Exception {
			
			RenameWorkerResults workerResults = new RenameWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Rename");
				logHandler.setSummary(String.format("Type: %s, Old Full Name: %s, New Full Name: %s", typeName, oldFullName, newFullName));
				connection.getConfig().addMessageHandler(logHandler);
				workerResults.setLogHandler(logHandler);
				
				SaveResult mdapiRename = connection.renameMetadata(typeName, oldFullName, newFullName);
				if (mdapiRename != null) {
					workerResults.setSuccess(mdapiRename.getSuccess());
				}
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				e.printStackTrace();
			}
			
			return workerResults;
		}
	}
	
	private CRUDMode mode;
	
	private SortedMap<String, DescribeMetadataObject> metadataDescription;
	private Map<String, SortedMap<String, FileProperties>> metadataLists = new TreeMap<>();
	
	private AnchorPane root;
	private ToolBar toolBar;
	private TreeView<String> descriptionAndListsTree;
	private TreeItem<String> descriptionAndListsTreeRoot;
	private Button describeButton;
	private Button listButton;
	private Button readButton;
	private Button renameButton;
	private Button deleteButton;
	private Button cancelButton;
	
	public CRUDNavigatorController(CRUDMode mode) {
		this.mode = mode;
		createGraph();
		mode.getPerspective().getApplication().metadataConnection().addListener((o, oldValue, newValue) -> handleMetadataConnectionChanged());
		mode.getPerspective().getApplication().userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}
	
	public Node getRoot() {
		return root;
	}
	
	private void createGraph() {
		
		root = new AnchorPane();
		
		toolBar = new ToolBar();
		AnchorPane.setTopAnchor(toolBar, 0.0);
		AnchorPane.setLeftAnchor(toolBar, 0.0);
		AnchorPane.setRightAnchor(toolBar, 0.0);
		root.getChildren().add(toolBar);
			
		describeButton = new Button("Describe");
		describeButton.setDisable(true);
		describeButton.setOnAction(e -> handleDescribeButtonClicked(e));
		toolBar.getItems().add(describeButton);
		
		listButton = new Button("List");
		listButton.setDisable(true);
		listButton.setOnAction(e -> handleListButtonClicked(e));
		toolBar.getItems().add(listButton);
		
		readButton = new Button("Read");
		readButton.setDisable(true);
		readButton.setOnAction(e -> handleReadButtonClicked(e));
		toolBar.getItems().add(readButton);
		
		renameButton = new Button("Rename");
		renameButton.setDisable(true);
		renameButton.setOnAction(e -> handleRenameButtonClicked(e));
		toolBar.getItems().add(renameButton);
		
		deleteButton = new Button("Delete");
		deleteButton.setDisable(true);
		deleteButton.setOnAction(e -> handleDeleteButtonClicked(e));
		toolBar.getItems().add(deleteButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.setDisable(true);
		toolBar.getItems().add(cancelButton);
		
		descriptionAndListsTree = new TreeView<>();
		descriptionAndListsTree.setCellFactory(TextFieldTreeCell.forTreeView());
		descriptionAndListsTree.setShowRoot(false);
		descriptionAndListsTree.setOnMouseClicked(e -> handleTreeItemClicked(e));
		descriptionAndListsTree.setOnEditCommit(e -> handleTreeItemEdited(e));
		descriptionAndListsTree.setOnEditCancel(e -> handleTreeItemEditCancelled(e));
		AnchorPane.setTopAnchor(descriptionAndListsTree, 38.0);
		AnchorPane.setBottomAnchor(descriptionAndListsTree, 0.0);
		AnchorPane.setLeftAnchor(descriptionAndListsTree, 0.0);
		AnchorPane.setRightAnchor(descriptionAndListsTree, 0.0);
		root.getChildren().add(descriptionAndListsTree);
		
		String treeRootText = "";
		GetUserInfoResult uir = mode.getPerspective().getApplication().userInfo().get();
		if (uir != null) {
			treeRootText = uir.getOrganizationName();
			descriptionAndListsTree.setShowRoot(true);
		}
		descriptionAndListsTreeRoot = new TreeItem<>(treeRootText);
		descriptionAndListsTree.setRoot(descriptionAndListsTreeRoot);
		descriptionAndListsTreeRoot.setExpanded(true);
	}
	
	private void handleMetadataConnectionChanged() {
		if (mode.getPerspective().getApplication().metadataConnection().get() != null) {
			setDisablesForTreeSelection();
			showPropertiesForTreeSelection();
		}
		else {
			describeButton.setDisable(true);
			listButton.setDisable(true);
			readButton.setDisable(true);
			renameButton.setDisable(true);
			deleteButton.setDisable(true);
		}
	}
	
	private void handleUserInfoChanged(GetUserInfoResult oldValue, GetUserInfoResult newValue) {
		descriptionAndListsTree.getRoot().getChildren().clear();
		if (metadataDescription != null) {
			metadataDescription.clear();
		}
		metadataLists.clear();
		
		if (newValue != null) {
			descriptionAndListsTreeRoot.setValue(newValue.getOrganizationName());
			descriptionAndListsTree.setShowRoot(true);
			descriptionAndListsTree.getSelectionModel().select(descriptionAndListsTreeRoot);
		}
		else {
			descriptionAndListsTreeRoot.setValue("");
			descriptionAndListsTree.setShowRoot(false);
		}
		
		setDisablesForTreeSelection();
		showPropertiesForTreeSelection();
	}
	
	private void handleTreeItemClicked(MouseEvent e) {
		setDisablesForTreeSelection();
		showPropertiesForTreeSelection();
		
		if (e.getClickCount() == 2) {
			
			TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
			if (selectedItem == null) {
				// Should never get here
				return;
			}
			
			if (selectedItem == descriptionAndListsTree.getRoot()) {
				return;
			}
			
			if (selectedItem.getParent() == descriptionAndListsTree.getRoot()) {
				return;
			}
			
			boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
			if (connected) {
				String typeName = selectedItem.getParent().getValue();
				String fullName = selectedItem.getValue();
				mode.getEditorController().edit(typeName, fullName);
			}
		}
	}
	
	private void setDisablesForTreeSelection() {
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			// Should never get here
			return;
		}
		
		boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
		
		if (selectedItem == descriptionAndListsTree.getRoot()) {
			if (connected) {
				describeButton.setDisable(false);
			}
			listButton.setDisable(true);
			readButton.setDisable(true);
			renameButton.setDisable(true);
			deleteButton.setDisable(true);
		}
		else if (selectedItem.getParent() == descriptionAndListsTree.getRoot()) {
			describeButton.setDisable(true);
			if (connected) {
				listButton.setDisable(false);
			}
			readButton.setDisable(true);
			renameButton.setDisable(true);
			deleteButton.setDisable(true);
		}
		else {
			describeButton.setDisable(true);
			listButton.setDisable(true);
			if (connected) {
				readButton.setDisable(false);
				renameButton.setDisable(false);
				deleteButton.setDisable(false);
			}
		}
	}
	
	private void setDisablesForOperationCompletion() {
		cancelButton.setDisable(true);
		descriptionAndListsTree.setDisable(false);
		setDisablesForTreeSelection();
		showPropertiesForTreeSelection();
	}
	
	private void setDisablesForOperationCancellation() {
		cancelButton.setDisable(true);
		setDisablesForTreeSelection();
	}
	
	private void showPropertiesForTreeSelection() {
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		if (selectedItem == descriptionAndListsTree.getRoot()) {
			mode.getPropertiesViewerController().showPropertiesForUserInfo(mode.getPerspective().getApplication().userInfo().get());
		}
		else if (selectedItem.getParent() == descriptionAndListsTree.getRoot()) {
			DescribeMetadataObject dmo = metadataDescription.get(selectedItem.getValue());
			mode.getPropertiesViewerController().showPropertiesForType(dmo);
		}
		else {
			String typeName = selectedItem.getParent().getValue();
			SortedMap<String, FileProperties> fileMap = metadataLists.get(typeName);
			String fullName = selectedItem.getValue();
			FileProperties fp = fileMap.get(fullName);
			mode.getPropertiesViewerController().showPropertiesForFile(fp);
		}
	}
	
	private void handleTreeItemEdited(EditEvent<String> e) {
		
		descriptionAndListsTree.setDisable(true);
		
		TreeItem<String> editedItem = descriptionAndListsTree.getEditingItem();	
		if (editedItem == descriptionAndListsTree.getRoot() ||
			editedItem.getParent() == descriptionAndListsTree.getRoot()) {
			// Should never get here.
			return;
		}
		
		String typeName = editedItem.getParent().getValue();
		String oldFullName = e.getOldValue();
		String newFullName = e.getNewValue();
		
		final RenameWorker renameWorker = new RenameWorker(mode.getPerspective().getApplication().metadataConnection().get(), typeName, oldFullName, newFullName);
		renameWorker.setOnSucceeded(es -> {
			
			if (renameWorker.getValue().getSuccess()) {
				mode.getPerspective().getLogController().log(renameWorker.getValue().getLogHandler());
				
				SortedMap<String, FileProperties> list = metadataLists.get(typeName);
				FileProperties fp = list.get(oldFullName);
				list.remove(oldFullName);
				
				mode.getEditorController().close(typeName, oldFullName);
				
				DescribeMetadataObject dmo = metadataDescription.get(typeName);
				String directory = dmo.getDirectoryName();
				String suffix = dmo.getSuffix();
				fp.setFileName(directory + "/" + newFullName + "." + suffix);
				fp.setFullName(newFullName);
				list.put(newFullName, fp);
				
				mode.getPropertiesViewerController().showPropertiesForFile(fp);
				
				descriptionAndListsTree.setEditable(false);
			}
			else {
				editedItem.setValue(oldFullName);
			}
			
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			renameWorker.cancel();
			// TODO: Reset tree item to old fullName, or perhaps run a list to confirm status and then
			// check to see if necessary to set tree item to old fullName.
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(renameWorker).start();
	}
	
	private void handleTreeItemEditCancelled(EditEvent<String> e) {
		
		descriptionAndListsTree.setEditable(false);
		setDisablesForTreeSelection();
	}
	
	private void handleDescribeButtonClicked(ActionEvent e) {
		
		descriptionAndListsTree.setDisable(true);
		describeButton.setDisable(true);
		
		final DescribeWorker describeWorker = new DescribeWorker(mode.getPerspective().getApplication().metadataConnection().get(), mode.getPerspective().getApplication().apiVersion().get());
		describeWorker.setOnSucceeded(es -> {
			mode.getPropertiesViewerController().showPropertiesForType(null);
			metadataLists.clear();
			metadataDescription = describeWorker.getValue().getDescription();
			ObservableList<TreeItem<String>> describeAndListTreeItems = descriptionAndListsTree.getRoot().getChildren();
			describeAndListTreeItems.clear();
			for (String metadataTypeName : metadataDescription.keySet()) {
				describeAndListTreeItems.add(new TreeItem<String>(metadataTypeName));
			}
			mode.getPerspective().getLogController().log(describeWorker.getValue().getLogHandler());
			
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			describeWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(describeWorker).start();
	}
	
	private void handleListButtonClicked(ActionEvent e) {
		
		descriptionAndListsTree.setDisable(true);
		listButton.setDisable(true);
		
		// TODO: Record and maintain scroll position
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		String selectedTypeName = selectedItem.getValue().toString();
		
		final ListWorker listWorker = new ListWorker(mode.getPerspective().getApplication().metadataConnection().get(), mode.getPerspective().getApplication().apiVersion().get(), selectedTypeName);
		listWorker.setOnSucceeded(es -> {
			SortedMap<String, FileProperties> listResult = listWorker.getValue().getList();
			metadataLists.put(selectedTypeName, listResult);
			if (listResult != null) {
				TreeItem<String> selectedTypeItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
				selectedTypeItem.getChildren().clear();
				for (String fullName : listResult.keySet()) {
					selectedTypeItem.getChildren().add(new TreeItem<String>(fullName));
				}
				selectedTypeItem.setExpanded(true);
			}
			mode.getPerspective().getLogController().log(listWorker.getValue().getLogHandler());
			
			descriptionAndListsTree.getSelectionModel().select(selectedItem);
			
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			listWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(listWorker).start();
	}
	
	private void handleReadButtonClicked(ActionEvent e) {
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		String typeName = selectedItem.getParent().getValue();
		String fullName = selectedItem.getValue();
		
		mode.getEditorController().edit(typeName, fullName);
	}
	
	private void handleRenameButtonClicked(ActionEvent e) {
		
		readButton.setDisable(true);
		renameButton.setDisable(true);
		deleteButton.setDisable(true);
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		
		descriptionAndListsTree.setEditable(true);
		descriptionAndListsTree.edit(selectedItem);
	}
	
	private void handleDeleteButtonClicked(ActionEvent e) {
		
		descriptionAndListsTree.setDisable(true);
		readButton.setDisable(true);
		deleteButton.setDisable(true);
		
		TreeItem<String> selectedItem = descriptionAndListsTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		String typeName = selectedItem.getParent().getValue();
		SortedMap<String, FileProperties> fileMap = metadataLists.get(typeName);
		String fullName = selectedItem.getValue();
		
		final DeleteWorker deleteWorker = new DeleteWorker(mode.getPerspective().getApplication().metadataConnection().get(), typeName, fullName);
		deleteWorker.setOnSucceeded(es -> {
			
			boolean deleted = deleteWorker.getValue().getSuccess();
			if (deleted) {
				mode.getEditorController().close(typeName, fullName);
				// TODO: Set selection somewhere helpful
				selectedItem.getParent().getChildren().remove(selectedItem);
				fileMap.remove(fullName);
			}
			else {
				deleteButton.setDisable(false);
				readButton.setDisable(false);
			}
			mode.getPerspective().getLogController().log(deleteWorker.getValue().getLogHandler());
			
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			deleteWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(deleteWorker).start();
	}
	
	private void handleCancelButtonClicked(ActionEvent e) {	
		cancelButton.setOnAction(null);
		
		// TODO: Fix this to wait for cancel to complete?
		
		setDisablesForOperationCancellation();
	}
}
