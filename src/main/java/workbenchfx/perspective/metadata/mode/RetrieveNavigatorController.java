package workbenchfx.perspective.metadata.mode;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

import workbenchfx.SOAPLogHandler;

public class RetrieveNavigatorController {
	
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
	
	private RetrieveMode mode;
	
	private SortedMap<String, DescribeMetadataObject> orgMetadataDescription;
	private Map<String, SortedMap<String, FileProperties>> orgMetadataLists = new TreeMap<>();
	
	private Map<String, Map<String, SortedMap<String, FileProperties>>> retrievesProperties = new HashMap<>();
	
	private AnchorPane root;
	private ToolBar toolBar;
	private Accordion accordion;
	private TitledPane orgPane;
	private TitledPane retrievesPane;
	private Button describeButton;
	private Button listButton;
	private Button cancelButton;
	private TreeView<String> orgTree;
	private CheckBoxTreeItem<String> orgTreeRoot;
	private TreeView<String> retrievesTree;
	private TreeItem<String> retrievesTreeRoot;
	
	public RetrieveNavigatorController(RetrieveMode mode) {
		this.mode = mode;
		createGraph();
		mode.getPerspective().getApplication().metadataConnection().addListener((o, oldValue, newValue) -> handleMetadataConnectionChanged());
		mode.getPerspective().getApplication().userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void addRetrieve(String name, Map<String, SortedMap<String, FileProperties>> properties, TreeItem<String> tree) {
		retrievesProperties.put(name, properties);
		tree.setExpanded(true);
		retrievesTree.getRoot().getChildren().add(tree);
		
		retrievesPane.setExpanded(true);
	}

	private void createGraph() {
		
		root = new AnchorPane();
		
		toolBar = new ToolBar();
		AnchorPane.setTopAnchor(toolBar, 0.0);
		AnchorPane.setLeftAnchor(toolBar, 0.0);
		AnchorPane.setRightAnchor(toolBar, 0.0);
		root.getChildren().add(toolBar);
		
		accordion = new Accordion();
		AnchorPane.setTopAnchor(accordion, 38.0);
		AnchorPane.setBottomAnchor(accordion, 0.0);
		AnchorPane.setLeftAnchor(accordion, 0.0);
		AnchorPane.setRightAnchor(accordion, 0.0);
		root.getChildren().add(accordion);
			
		describeButton = new Button("Describe");
		describeButton.setDisable(true);
		describeButton.setOnAction(e -> handleDescribeButtonClicked(e));
		toolBar.getItems().add(describeButton);
		
		listButton = new Button("List");
		listButton.setDisable(true);
		listButton.setOnAction(e -> handleListButtonClicked(e));
		toolBar.getItems().add(listButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.setDisable(true);
		toolBar.getItems().add(cancelButton);
		
		orgTree = new TreeView<>();
		orgTree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
		orgTree.setShowRoot(false);
		orgTree.setOnMouseClicked(e -> handleOrgTreeItemClicked(e));
		
		String treeRootText = "";
		GetUserInfoResult uir = mode.getPerspective().getApplication().userInfo().get();
		if (uir != null) {
			treeRootText = uir.getOrganizationName();
			orgTree.setShowRoot(true);
		}
		orgTreeRoot = new CheckBoxTreeItem<>(treeRootText);
		orgTreeRoot.setExpanded(true);
		orgTree.setRoot(orgTreeRoot);		
		
		orgPane = new TitledPane("Org", orgTree);
		orgPane.expandedProperty().addListener(e -> setDisablesForOrgTreeSelection());
		accordion.getPanes().add(orgPane);
		
		retrievesTree = new TreeView<>();
		retrievesTree.setShowRoot(false);
		retrievesTree.setOnMouseClicked(e -> handleRetrievesTreeItemClicked(e));
		
		retrievesTreeRoot = new TreeItem<>("");
		retrievesTreeRoot.setExpanded(true);
		retrievesTree.setRoot(retrievesTreeRoot);
		
		retrievesPane = new TitledPane("Retrieves", retrievesTree);
		orgPane.expandedProperty().addListener(e -> setDisablesForRetrievesTreeSelection());
		accordion.getPanes().add(retrievesPane);
	}
	
	private void handleDescribeButtonClicked(ActionEvent e) {
		
		orgTree.setDisable(true);
		describeButton.setDisable(true);
		
		final DescribeWorker describeWorker = new DescribeWorker(mode.getPerspective().getApplication().metadataConnection().get(), mode.getPerspective().getApplication().apiVersion().get());
		describeWorker.setOnSucceeded(es -> {
			mode.getPropertiesViewerController().showPropertiesForType(null);
			orgMetadataLists.clear();
			orgMetadataDescription = describeWorker.getValue().getDescription();
			ObservableList<TreeItem<String>> describeAndListTreeItems = orgTree.getRoot().getChildren();
			describeAndListTreeItems.clear();
			for (String metadataTypeName : orgMetadataDescription.keySet()) {
				describeAndListTreeItems.add(new CheckBoxTreeItem<String>(metadataTypeName));
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
		
		orgTree.setDisable(true);
		listButton.setDisable(true);
		
		// TODO: Record and maintain scroll position
		
		TreeItem<String> selectedItem = orgTree.getSelectionModel().getSelectedItem();
		String selectedTypeName = selectedItem.getValue().toString();
		
		final ListWorker listWorker = new ListWorker(mode.getPerspective().getApplication().metadataConnection().get(), mode.getPerspective().getApplication().apiVersion().get(), selectedTypeName);
		listWorker.setOnSucceeded(es -> {
			SortedMap<String, FileProperties> listResult = listWorker.getValue().getList();
			orgMetadataLists.put(selectedTypeName, listResult);
			if (listResult != null) {
				TreeItem<String> selectedTypeItem = orgTree.getSelectionModel().getSelectedItem();
				selectedTypeItem.getChildren().clear();
				for (String fullName : listResult.keySet()) {
					selectedTypeItem.getChildren().add(new CheckBoxTreeItem<String>(fullName));
				}
				selectedTypeItem.setExpanded(true);
			}
			mode.getPerspective().getLogController().log(listWorker.getValue().getLogHandler());
			
			orgTree.getSelectionModel().select(selectedItem);
			
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			listWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(listWorker).start();
	}
	
	private void handleOrgTreeItemClicked(MouseEvent e) {
		setDisablesForOrgTreeSelection();
		showPropertiesForOrgTreeSelection();
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
	
	private void handleMetadataConnectionChanged() {
		if (mode.getPerspective().getApplication().metadataConnection().get() != null) {
			setDisablesForOrgTreeSelection();
			showPropertiesForOrgTreeSelection();
		}
		else {
			describeButton.setDisable(true);
			listButton.setDisable(true);
		}
	}
	
	private void handleUserInfoChanged(GetUserInfoResult oldValue, GetUserInfoResult newValue) {
		orgTree.getRoot().getChildren().clear();
		if (orgMetadataDescription != null) {
			orgMetadataDescription.clear();
		}
		orgMetadataLists.clear();
		
		if (newValue != null) {
			orgTreeRoot.setValue(newValue.getOrganizationName());
			orgTree.setShowRoot(true);
			orgTree.getSelectionModel().select(orgTreeRoot);
		}
		else {
			orgTreeRoot.setValue("");
			orgTree.setShowRoot(false);
		}
		
		setDisablesForOrgTreeSelection();
		showPropertiesForOrgTreeSelection();
	}
	
	private void setDisablesForOperationCompletion() {
		cancelButton.setDisable(true);
		orgTree.setDisable(false);
		setDisablesForOrgTreeSelection();
		showPropertiesForOrgTreeSelection();
	}
	
	private void setDisablesForOperationCancellation() {
		cancelButton.setDisable(true);
		// TODO:
		// TODO:
		// TODO:
	}
	
	private void handleCancelButtonClicked(ActionEvent e) {	
		cancelButton.setOnAction(null);
		
		// TODO: Fix this to wait for cancel to complete?
		
		setDisablesForOperationCancellation();
	}
	
	private void setDisablesForOrgTreeSelection() {
		
		if (!orgPane.isExpanded()) {
			describeButton.setDisable(true);
			listButton.setDisable(true);
			return;
		}
		
		TreeItem<String> selectedItem = orgTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
		
		if (selectedItem == orgTree.getRoot()) {
			if (connected) {
				describeButton.setDisable(false);
			}
			listButton.setDisable(true);
		}
		else if (selectedItem.getParent() == orgTree.getRoot()) {
			describeButton.setDisable(true);
			if (connected) {
				listButton.setDisable(false);
			}
		}
		else {
			describeButton.setDisable(true);
			listButton.setDisable(true);
			if (connected) {
				// TODO
			}
		}
	}
	
	private void showPropertiesForOrgTreeSelection() {
		
		TreeItem<String> selectedItem = orgTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		if (selectedItem == orgTree.getRoot()) {
			mode.getPropertiesViewerController().showPropertiesForUserInfo(mode.getPerspective().getApplication().userInfo().get());
		}
		else if (selectedItem.getParent() == orgTree.getRoot()) {
			DescribeMetadataObject dmo = orgMetadataDescription.get(selectedItem.getValue());
			mode.getPropertiesViewerController().showPropertiesForType(dmo);
		}
		else {
			String typeName = selectedItem.getParent().getValue();
			SortedMap<String, FileProperties> fileMap = orgMetadataLists.get(typeName);
			String fullName = selectedItem.getValue();
			FileProperties fp = fileMap.get(fullName);
			mode.getPropertiesViewerController().showPropertiesForFile(fp);
		}
	}
	
	private void setDisablesForRetrievesTreeSelection() {
		
		if (retrievesPane.isExpanded()) {
			describeButton.setDisable(true);
			listButton.setDisable(true);
		}
	}
	
	private void showPropertiesForRetrievesTreeSelection() {
		
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
		
		String retrieveName = selectedItem.getParent().getParent().getValue();
		String typeName = selectedItem.getParent().getValue();
		String fileName = selectedItem.getValue();
		
		Map<String, SortedMap<String, FileProperties>> retrieveProperties = retrievesProperties.get(retrieveName);	
		SortedMap<String, FileProperties> retrievePropertiesForType = retrieveProperties.get(typeName);	
		FileProperties fileProperties = retrievePropertiesForType.get(fileName);
		mode.getPropertiesViewerController().showPropertiesForFile(fileProperties);
	}
}
