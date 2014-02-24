package workbenchfx.editor.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Package;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.TypeInfo;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;
import com.sforce.ws.parser.XmlOutputStream;
import com.sforce.ws.wsdl.Constants;

import workbenchfx.Main;
import workbenchfx.SOAPLogHandler;

public class PackageEditor implements Editor {
	
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
	
	private static final String PACKAGE_TYPE = "Package";
	private static final String MANIFEST_NAME = "package.xml";
	
	private static final String SOURCE_VIEW = "Source";
	private static final String FORM_VIEW = "Form";
	
	public static String getManifestName() {
		return MANIFEST_NAME;
	}
	
	public static String getType() {
		return PACKAGE_TYPE;
	}
	
	private Main application;
	
	private Package metadata;
	
	private ToolBar toolBarRoot;
	private HBox modeBox;
	private ToggleGroup modeGroup;
	private ToggleButton sourceViewButton;
	private ToggleButton formViewButton;
	
	private Node root;
	
	private TextArea sourceViewRoot;
	
	private BorderPane formViewRoot;
	private GridPane formViewLeft;
	private AnchorPane formViewRight;
	private BorderPane navigatorToolBarPane;
	private ToolBar navigatorToolBar;
	private Button describeButton;
	private Button listButton;
	private Button cancelButton;
	
	private boolean editable;
	
	private BooleanProperty dirtyProperty = new SimpleBooleanProperty();
	
	private SortedMap<String, DescribeMetadataObject> orgMetadataDescription;
	private Map<String, SortedMap<String, FileProperties>> orgMetadataLists = new TreeMap<>();
	private TreeView<String> orgTree;
	private CheckBoxTreeItem<String> orgTreeRoot;
	
	public PackageEditor(Main application) {
		this.application = application;
		
		createSourceViewGraph();
		createFormViewGraph();
		createToolBarGraph();
		
		application.metadataConnection().addListener((o, oldValue, newValue) -> handleMetadataConnectionChanged());
		application.userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}

	public Node getRoot() {
		return root;
	}
	
	public Node getToolBarRoot() {
		// TODO: Uncomment this when the two-way editing code is
		// complete.
		//return toolBarRoot;
		return null;
	}
	
	public Metadata getMetadata() {
		if (dirtyProperty.get()) {
			setMetadataFromUI();
		}
		return metadata;
	}
	
	public void setMetadata(Metadata metadata) {
		
		if (!(metadata instanceof Package)) {
			return;
		}
		
		this.metadata = (Package)metadata;
		setUIFromMetadata();
		dirtyProperty.set(false);
	}
	
	public String getMetadataAsXml() {
		if (dirtyProperty.get()) {
			setMetadataFromUI();
		}
		return getMetadataAsXmlInternal();
	}
	
	public void setMetadataAsXml(String xml) {
		setMetadataFromXml(xml);
		setUIFromMetadata();
	}
	
	public BooleanProperty dirty() {
		return dirtyProperty;
	}
	
	public void lock() {
		sourceViewRoot.setDisable(true);
		// TODO Disable formView
	}
	
	public void unlock() {
		sourceViewRoot.setDisable(false);
		// TODO Enable form view
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		sourceViewRoot.setEditable(editable);
		// TODO Set editable on form view
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	private void createSourceViewGraph() {
		
		sourceViewRoot = new TextArea();
		sourceViewRoot.setEditable(true);
		sourceViewRoot.textProperty().addListener(e -> dirtyProperty.set(true));
	}
	
	private void createFormViewGraph() {
		
		formViewRoot = new BorderPane();
		
		formViewLeft = new GridPane();
		formViewRoot.setLeft(formViewLeft);
		
		// TODO
		formViewLeft.add(new Label("Hello"), 0, 0);
		formViewLeft.add(new Label("Doug"), 1, 0);
		
		formViewRight = new AnchorPane();
		formViewRoot.setCenter(formViewRight);
		
		navigatorToolBarPane = new BorderPane();
		AnchorPane.setTopAnchor(navigatorToolBarPane, 0.0);
		AnchorPane.setLeftAnchor(navigatorToolBarPane, 0.0);
		AnchorPane.setRightAnchor(navigatorToolBarPane, 0.0);
		formViewRight.getChildren().add(navigatorToolBarPane);
		
		navigatorToolBar = new ToolBar();
		navigatorToolBarPane.setCenter(navigatorToolBar);
		
		describeButton = new Button("Describe");
		describeButton.setDisable(true);
		describeButton.setOnAction(e -> handleDescribeButtonClicked(e));
		navigatorToolBar.getItems().add(describeButton);
		
		listButton = new Button("List");
		listButton.setDisable(true);
		listButton.setOnAction(e -> handleListButtonClicked(e));
		navigatorToolBar.getItems().add(listButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.setDisable(true);
		cancelButton.setOnAction(e -> handleCancelButtonClicked(e));
		navigatorToolBar.getItems().add(cancelButton);
		
		orgTree = new TreeView<>();
		orgTree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
		orgTree.setShowRoot(false);
		orgTree.setOnMouseClicked(e -> handleOrgTreeItemClicked(e));
		AnchorPane.setTopAnchor(orgTree, 38.0);
		AnchorPane.setBottomAnchor(orgTree, 0.0);
		AnchorPane.setLeftAnchor(orgTree, 0.0);
		AnchorPane.setRightAnchor(orgTree, 0.0);
		formViewRight.getChildren().add(orgTree);
		
		String treeRootText = "";
		GetUserInfoResult uir = application.userInfo().get();
		if (uir != null) {
			treeRootText = uir.getOrganizationName();
			orgTree.setShowRoot(true);
		}
		orgTreeRoot = new CheckBoxTreeItem<>(treeRootText);
		orgTreeRoot.setExpanded(true);
		orgTree.setRoot(orgTreeRoot);
	}
	
	private void createToolBarGraph() {
		
		toolBarRoot = new ToolBar();
		
		modeBox = new HBox();
		toolBarRoot.getItems().add(modeBox);
		
		modeGroup = new ToggleGroup();
		
		sourceViewButton = new ToggleButton(SOURCE_VIEW);
		sourceViewButton.getStyleClass().add("left-pill");
		sourceViewButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(sourceViewButton);
		
		formViewButton = new ToggleButton(FORM_VIEW);
		formViewButton.getStyleClass().add("right-pill");
		formViewButton.setToggleGroup(modeGroup);
		modeBox.getChildren().add(formViewButton);
		
		modeGroup.selectToggle(sourceViewButton);
		handleModeToggleChanged();
		modeGroup.selectedToggleProperty().addListener(e -> handleModeToggleChanged());
	}
	
	private void setUIFromMetadata() {
		
		if (isSourceViewMode()) {
			setSourceViewFromMetadata();
		}
		else {
			setFormViewFromMetadata();
		}
	}
	
	private void setSourceViewFromMetadata() {

		sourceViewRoot.clear();
		if (metadata != null) {
			String xml = getMetadataAsXmlInternal();
			sourceViewRoot.appendText(xml);
	        sourceViewRoot.setScrollTop(0.0);
		}
	}
	
	private void setFormViewFromMetadata() {
		// TODO
	}
	
	private void setMetadataFromUI() {
		
		if (isSourceViewMode()) {
			setMetadataFromSourceView();
		}
		else {
			setMetadataFromFormView();
		}
	}
	
	private void setMetadataFromSourceView() {
		
		setMetadataFromXml(sourceViewRoot.getText());
	}
	
	private void setMetadataFromFormView() {
		// TODO
	}
	
	private void setMetadataFromXml(String xml) {
		
		try {
			XmlInputStream xin = new XmlInputStream();
			xin.setInput(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))), "UTF-8");
			
			TypeMapper mapper = new TypeMapper();
			
			if (metadata != null) {
				metadata.load(xin, mapper);
			}
			else {
				TypeInfo typeInfo = new TypeInfo(Main.getMetadataNamespace(), 
												 PACKAGE_TYPE, 
											     Main.getMetadataNamespace(), 
											     PACKAGE_TYPE, 
											     0, 
											     1, 
											     true);
				xin.peekTag();
				metadata = (Package)mapper.readObject(xin, typeInfo, Package.class);
			}
		}
		catch (PullParserException e) {
			e.printStackTrace();
		}
		catch (ConnectionException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private String getMetadataAsXmlInternal() {
		
		String xml = null;
		
		if (metadata != null) {
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				XmlOutputStream xout = new XmlOutputStream(baos, "    ");
		        
		        xout.setPrefix("env", Constants.SOAP_ENVELOPE_NS);
		        xout.setPrefix("xsd", Constants.SCHEMA_NS);
		        xout.setPrefix("xsi", Constants.SCHEMA_INSTANCE_NS);
		        xout.setPrefix("", Main.getMetadataNamespace());
		        
		        metadata.write(new QName(Main.getMetadataNamespace(), PACKAGE_TYPE), xout, new TypeMapper());
		        
		        xout.close();
		        
		        xml = new String(baos.toByteArray(), Charset.forName("UTF-8"));
			}
			catch (IOException e) {
				// TODO: Fix this
				e.printStackTrace();
			}
		}
		
		return xml;
	}
	
	private void handleModeToggleChanged() {
		ToggleButton selectedButton = (ToggleButton)modeGroup.getSelectedToggle();
		if (selectedButton != null) {
			setActiveMode(selectedButton.getText());
		}
	}
	
	private void setActiveMode(String mode) {

		if (mode.equals(SOURCE_VIEW)) {
			root = sourceViewRoot;
		}
		else {
			root = formViewRoot;
		}
	}
	
	private boolean isSourceViewMode() {
		return root == sourceViewRoot;
	}
	
	private void handleMetadataConnectionChanged() {
		if (application.metadataConnection().get() != null) {
			setDisablesForOrgTreeSelection();
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
	}
	
	private void handleDescribeButtonClicked(ActionEvent e) {
		
		orgTree.setDisable(true);
		describeButton.setDisable(true);
		
		final DescribeWorker describeWorker = new DescribeWorker(application.metadataConnection().get(), application.apiVersion().get());
		describeWorker.setOnSucceeded(es -> {
			orgMetadataLists.clear();
			orgMetadataDescription = describeWorker.getValue().getDescription();
			ObservableList<TreeItem<String>> describeAndListTreeItems = orgTree.getRoot().getChildren();
			describeAndListTreeItems.clear();
			for (String metadataTypeName : orgMetadataDescription.keySet()) {
				describeAndListTreeItems.add(new CheckBoxTreeItem<String>(metadataTypeName));
			}
			application.getLogController().log(describeWorker.getValue().getLogHandler());
			
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
		
		final ListWorker listWorker = new ListWorker(application.metadataConnection().get(), application.apiVersion().get(), selectedTypeName);
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
			application.getLogController().log(listWorker.getValue().getLogHandler());
			
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
	}
	
	private void setDisablesForOrgTreeSelection() {
		
		TreeItem<String> selectedItem = orgTree.getSelectionModel().getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		
		boolean connected = application.metadataConnection().get() != null;
		
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
	
	private void setDisablesForOperationCompletion() {
		cancelButton.setDisable(true);
		orgTree.setDisable(false);
		setDisablesForOrgTreeSelection();
	}
	
	private void handleCancelButtonClicked(ActionEvent e) {	
		cancelButton.setOnAction(null);
		
		// TODO: Fix this to wait for cancel to complete?
		
		setDisablesForOperationCancellation();
	}
	
	private void setDisablesForOperationCancellation() {
		cancelButton.setDisable(true);
		// TODO:
		// TODO:
		// TODO:
	}
}