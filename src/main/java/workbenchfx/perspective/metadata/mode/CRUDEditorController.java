package workbenchfx.perspective.metadata.mode;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.TypeInfo;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.XmlInputStream;

import workbenchfx.Main;
import workbenchfx.SOAPLogHandler;
import workbenchfx.editor.metadata.Editor;
import workbenchfx.editor.metadata.EditorFactory;

public class CRUDEditorController {
	
	private static class CreateWorkerResults {
		
		private SOAPLogHandler logHandler;
		private Metadata metadata;
		private boolean success;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}
		public Metadata getMetadata() {
			return metadata;
		}
		
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public boolean getSuccess() {
			return success;
		}
	}
	
	private static class CreateWorker extends Task<CreateWorkerResults> {
		
		private MetadataConnection connection;
		private String xml;
		
		public CreateWorker(MetadataConnection connection, String xml) {
			this.connection = connection;
			this.xml = xml;
		}
		
		@Override
		protected CreateWorkerResults call() throws Exception {
		
			CreateWorkerResults results = new CreateWorkerResults();
			
			try {
				XmlInputStream xin = new XmlInputStream();
				xin.setInput(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))), "UTF-8");
				
				TypeMapper mapper = new TypeMapper();
				TypeInfo typeInfo = new TypeInfo(Main.getMetadataNamespace(), 
											     "Metadata", 
											     Main.getMetadataNamespace(), 
											     "Metadata", 
											     0, 
											     1, 
											     true);
				xin.peekTag();
				Metadata metadata = (Metadata)mapper.readObject(xin, typeInfo, Metadata.class);
				results.setMetadata(metadata);
				
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Create");
				logHandler.setSummary(String.format("Type: %s, Full Name: %s", metadata.getClass().getSimpleName(), metadata.getFullName()));
				connection.getConfig().addMessageHandler(logHandler);
				results.setLogHandler(logHandler);
				
				SaveResult[] mdapiCreate = connection.createMetadata(new Metadata[]{metadata});
				if (mdapiCreate != null && mdapiCreate.length == 1) {
					results.setSuccess(mdapiCreate[0].isSuccess());
					// TODO: Add error reporting
				}
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				e.printStackTrace();
			}
			
			return results;
		}
	}
	
	private static class ReadWorkerResults {
		
		private SOAPLogHandler logHandler;
		private Metadata metadata;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}
		public Metadata getMetadata() {
			return metadata;
		}
	}
	
	private static class ReadWorker extends Task<ReadWorkerResults> {
		
		private MetadataConnection connection;
		private String type;
		private String fullName;
		
		public ReadWorker(MetadataConnection connection, String type, String fullName) {
			this.connection = connection;
			this.type = type;
			this.fullName = fullName;
		}
		
		@Override
		protected ReadWorkerResults call() throws Exception {
			
			ReadWorkerResults results = new ReadWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Read");
				logHandler.setSummary(String.format("Type: %s, Full Name: %s", type, fullName));
				connection.getConfig().addMessageHandler(logHandler);
				results.setLogHandler(logHandler);
				
				Metadata[] records = connection.readMetadata(type, new String[]{fullName}).getRecords();
				if (records != null && records.length == 1) {
					results.setMetadata(records[0]);
				}
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				e.printStackTrace();
			}
			
			return results;
		}
	}
	
	private static class UpdateWorkerResults {
		
		private SOAPLogHandler logHandler;
		boolean success;
		
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
	
	private static class UpdateWorker extends Task<UpdateWorkerResults> {
		
		private MetadataConnection connection;
		private Metadata metadata;
		
		public UpdateWorker(MetadataConnection connection, Metadata metadata) {
			this.connection = connection;
			this.metadata = metadata;
		}
		
		@Override
		protected UpdateWorkerResults call() throws Exception {
			
			UpdateWorkerResults results = new UpdateWorkerResults();
			
			try {
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Update");
				logHandler.setSummary(String.format("Type: %s, Full Name: %s", metadata.getClass().getSimpleName(), metadata.getFullName()));
				connection.getConfig().addMessageHandler(logHandler);
				results.setLogHandler(logHandler);
				
				SaveResult[] mdapiUpdate = connection.updateMetadata(new Metadata[]{metadata});
				if (mdapiUpdate != null && mdapiUpdate.length == 1) {
					results.setSuccess(mdapiUpdate[0].isSuccess());
				}
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				e.printStackTrace();
			}
			
			return results;
		}
	}
	
	private static class FileController {
		
		private String type;
		private String fullName;
		private Tab tab;
		private Editor editor;
		private Metadata metadata;
		private boolean newFlag;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		
		public Tab getTab() {
			return tab;
		}
		public void setTab(Tab tab) {
			this.tab = tab;
		}
		
		public Editor getEditor() {
			return editor;
		}
		public void setEditor(Editor editor) {
			this.editor = editor;
		}
		
		public Metadata getMetadata() {
			return metadata;
		}
		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}
		
		public boolean isNew() {
			return newFlag;
		}
		public void setNew(boolean newFlag) {
			this.newFlag = newFlag;
		}
	}
	
	private CRUDMode mode;
	
	private Map<String, FileController> fileControllersByName = new HashMap<String, FileController>();
	private int newFileCounter = 1;
	
	private Map<Tab, FileController> fileControllersByTab = new HashMap<Tab, FileController>();
	
	private AnchorPane root;
	private BorderPane toolBarPane;
	private ToolBar toolBar;
	private Button newButton;
	private Button createButton;
	private Button updateButton;
	private Button cancelButton;
	private TabPane tabPane;
	
	public CRUDEditorController(CRUDMode mode) {
		this.mode = mode;
		createGraph();
		mode.getPerspective().getApplication().metadataConnection().addListener((o, oldValue, newValue) -> handleMetadataConnectionChanged());
		mode.getPerspective().getApplication().userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}

	public Node getRoot() {
		return root;
	}
	
	public void edit(String type, String fullName) {
		
		if (fileControllersByName.get(fullName) != null) {
			tabPane.getSelectionModel().select(fileControllersByName.get(fullName).getTab());
			return;
		}
		
		final FileController fileController = new FileController();
		fileController.setType(type);
		fileController.setFullName(fullName);
		fileController.setNew(false);
		
		Tab tab = new Tab();
		tab.setText(fullName);
		tab.setOnSelectionChanged(e -> handleTabSelection());
		tab.setOnClosed(e -> {
			fileControllersByName.remove(fullName);
			fileControllersByTab.remove(tab);
			handleTabSelection();
		});
		fileController.setTab(tab);
		
		final Editor editor = EditorFactory.createEditor(type, mode.getPerspective().getApplication());
		editor.dirty().addListener((o, oldValue, newValue) -> setDisablesForTabSelection());
		tab.setContent(editor.getRoot());
		fileController.setEditor(editor);
		
		final ReadWorker readWorker = new ReadWorker(mode.getPerspective().getApplication().metadataConnection().get(), type, fullName);
		readWorker.setOnSucceeded(e -> {
			Metadata m = readWorker.getValue().getMetadata();
			fileController.setMetadata(m);
			editor.setMetadata(m);
			mode.getPerspective().getLogController().log(readWorker.getValue().getLogHandler());
			
			setDisablesForOperationCompletion();
		});	
		
		fileControllersByName.put(fullName, fileController);
		fileControllersByTab.put(tab, fileController);
		
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		
		handleTabSelection();
		
		cancelButton.setOnAction(ec -> {
			readWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(readWorker).start();
	}
	
	public void close(String type, String fullName) {	
		
		FileController fileController = fileControllersByName.get(fullName);
		if (fileController == null) {
			return;
		}
		
		Tab tab = fileController.getTab();
		tabPane.getTabs().remove(tab);
		fileControllersByName.remove(fullName);
		fileControllersByTab.remove(tab);
		
		handleTabSelection();
		
		// TODO: Special handling for zero remaining file controllers and tabs
	}
	
	public void closeAll() {
		tabPane.getTabs().clear();
		fileControllersByName.clear();
		fileControllersByTab.clear();
		
		handleTabSelection();
		
		// TODO: Special handling for zero remaining file controllers and tabs
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
		
		newButton = new Button("New");
		newButton.setOnAction(e -> handleNewButtonClicked(e));
		toolBar.getItems().add(newButton);
		
		toolBar.getItems().add(new Separator());
		
		createButton = new Button("Create");
		createButton.setDisable(true);
		createButton.setOnAction(e -> handleCreateButtonClicked(e));
		toolBar.getItems().add(createButton);
		
		updateButton = new Button("Update");
		updateButton.setDisable(true);
		updateButton.setOnAction(e -> handleUpdateButtonClicked(e));
		toolBar.getItems().add(updateButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.setDisable(true);
		cancelButton.setOnAction(e -> handleCancelButtonClicked(e));
		toolBar.getItems().add(cancelButton);
		
		tabPane = new TabPane();
		tabPane.setSide(Side.BOTTOM);
		AnchorPane.setTopAnchor(tabPane, 38.0);
		AnchorPane.setBottomAnchor(tabPane, 0.0);
		AnchorPane.setLeftAnchor(tabPane, 0.0);
		AnchorPane.setRightAnchor(tabPane, 0.0);
		root.getChildren().add(tabPane);
	}
	
	private void handleMetadataConnectionChanged() {
		
		setDisablesForTabSelection();
	}
	
	private void handleUserInfoChanged(GetUserInfoResult oldValue, GetUserInfoResult newValue) {
		closeAll();
	}
	
	private void handleNewButtonClicked(ActionEvent e) {
		
		String newFileName = "New " + newFileCounter;
		newFileCounter++;
		
		final FileController fileController = new FileController();
		fileController.setNew(true);
		
		Tab tab = new Tab();
		tab.setText(newFileName);
		tab.setOnSelectionChanged(es -> handleTabSelection());
		tab.setOnClosed(ec -> {
			fileControllersByName.remove(newFileName);
			fileControllersByTab.remove(tab);
			handleTabSelection();
		});
		fileController.setTab(tab);
		
		final Editor editor = EditorFactory.createEditor(null, mode.getPerspective().getApplication());
		editor.dirty().addListener((o, oldValue, newValue) -> setDisablesForTabSelection());
		tab.setContent(editor.getRoot());
		fileController.setEditor(editor);
		
		fileControllersByName.put(newFileName, fileController);
		fileControllersByTab.put(tab, fileController);
		
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		
		handleTabSelection();
	}
	
	private void handleCreateButtonClicked(ActionEvent e) {
		
		if (mode.getPerspective().getApplication().metadataConnection().get() == null) {
			return;
		}
		
		createButton.setDisable(true);
		updateButton.setDisable(true);
		
		Tab tab = tabPane.getSelectionModel().getSelectedItem();
		FileController fileController = fileControllersByTab.get(tab);
		Editor editor = fileController.getEditor();
		
		if (!fileController.isNew() || !editor.dirty().get()) {
			return;
		}
		
		editor.lock();
		String xml = editor.getMetadataAsXml();
		
		final CreateWorker createWorker = new CreateWorker(mode.getPerspective().getApplication().metadataConnection().get(), xml);
		createWorker.setOnSucceeded(es -> {
			mode.getPerspective().getLogController().log(createWorker.getValue().getLogHandler());
			boolean created = createWorker.getValue().getSuccess();
			if (created) {
				fileControllersByName.remove(fileController.getFullName());
				Metadata metadata = createWorker.getValue().getMetadata();
				String fullName = metadata.getFullName();
				fileControllersByName.put(fullName, fileController);
				fileController.setNew(false);
				editor.setMetadata(metadata);
				tab.setText(fullName);
				tab.setOnClosed(ec -> {
					fileControllersByName.remove(fullName);
					fileControllersByTab.remove(tab);
					handleTabSelection();
				});
			}
			editor.unlock();
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			createWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(createWorker).start();
	}
	
	private void handleUpdateButtonClicked(ActionEvent e) {
		
		if (mode.getPerspective().getApplication().metadataConnection().get() == null) {
			return;
		}
		
		createButton.setDisable(true);
		updateButton.setDisable(true);
		
		Tab tab = tabPane.getSelectionModel().getSelectedItem();
		FileController fileController = fileControllersByTab.get(tab);
		Editor editor = fileController.getEditor();
		
		if (!editor.dirty().get()) {
			return;
		}
		
		editor.lock();
		Metadata m = editor.getMetadata();
		
		final UpdateWorker updateWorker = new UpdateWorker(mode.getPerspective().getApplication().metadataConnection().get(), m);
		updateWorker.setOnSucceeded(es -> {
			mode.getPerspective().getLogController().log(updateWorker.getValue().getLogHandler());
			cancelButton.setDisable(true);
			boolean updated = updateWorker.getValue().getSuccess();
			if (updated) {
				editor.dirty().set(false);
			}
			editor.unlock();
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			updateWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(updateWorker).start();
	}
	
	private void handleCancelButtonClicked(ActionEvent e) {
		
		cancelButton.setOnAction(null);
		
		// TODO: Fix this to wait for cancel to complete?
		
		setDisablesForOperationCancellation();
	}
	
	private void setDisablesForTabSelection() {
		
		if (tabPane.getTabs().size() == 0) {
			updateButton.setDisable(true);
			createButton.setDisable(true);
		}
		else {
			boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
			
			Tab tab = tabPane.getSelectionModel().getSelectedItem();
			FileController fileController = fileControllersByTab.get(tab);
			Editor editor = fileController.getEditor();
			if (fileController.isNew()) {
				if (editor.dirty().get() && connected) {
					createButton.setDisable(false);
				}
				else {
					createButton.setDisable(true);
				}
				updateButton.setDisable(true);
			}
			else {
				if (editor.dirty().get() && connected) {
					updateButton.setDisable(false);
				}
				else {
					updateButton.setDisable(true);
				}
				createButton.setDisable(true);
			}
		}
	}
	
	private void handleTabSelection() {
		setToolBarForTabSelection();
		setDisablesForTabSelection();
	}
	
	private void setToolBarForTabSelection() {
		// TODO
	}
	
	private void setDisablesForOperationCompletion() {
		cancelButton.setDisable(true);
		setDisablesForTabSelection();
	}
	
	private void setDisablesForOperationCancellation() {
		cancelButton.setDisable(true);
		// TODO:
	}
}
