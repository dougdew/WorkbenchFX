package workbenchfx.perspective.metadata.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.metadata.AsyncRequestState;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Package;
import com.sforce.soap.metadata.RetrieveMessage;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.ws.bind.TypeInfo;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

import workbenchfx.Main;
import workbenchfx.SOAPLogHandler;
import workbenchfx.editor.metadata.Editor;
import workbenchfx.editor.metadata.EditorFactory;

public class RetrieveEditorController {
	
	private static class RetrieveWorkerResults {
		
		private SOAPLogHandler logHandler;
		private Package pkg;
		private AsyncResult asyncResult;
		private RetrieveResult retrieveResult;
		private Map<String, SortedMap<String, FileProperties>> fileProperties;
		private TreeItem<String> fileNames;
		private Map<String, String> files;
		
		public void setLogHandler(SOAPLogHandler logHandler) {
			this.logHandler = logHandler;
		}
		public SOAPLogHandler getLogHandler() {
			return logHandler;
		}
		
		public void setPackage(Package pkg) {
			this.pkg = pkg;
		}
		public Package getPackage() {
			return pkg;
		}
		
		public void setAsyncResult(AsyncResult asyncResult) {
			this.asyncResult = asyncResult;
		}
		public AsyncResult getAsyncResult() {
			return asyncResult;
		}

		public void setRetrieveResult(RetrieveResult retrieveResult) {
			this.retrieveResult = retrieveResult;
		}
		public RetrieveResult getRetrieveResult() {
			return retrieveResult;
		}
		
		public void setFileProperties(Map<String, SortedMap<String, FileProperties>> fileProperties) {
			this.fileProperties = fileProperties;
		}
		public Map<String, SortedMap<String, FileProperties>> getFileProperties() {
			return fileProperties;
		}
		
		public void setFileNames(TreeItem<String> fileNames) {
			this.fileNames = fileNames;
		}
		public TreeItem<String> getFileNames() {
			return fileNames;
		}
		
		public void setFiles(Map<String, String> files) {
			this.files = files;
		}
		public Map<String, String> getFiles() {
			return files;
		}
	}
	
	private static class RetrieveWorker extends Task<RetrieveWorkerResults> {
		
		private static final int MAX_POLL_REQUESTS = 50;
		
		private MetadataConnection connection;
		private double apiVersion;
		private String xml;
		private String retrieveName;
		
		public RetrieveWorker(MetadataConnection connection, double apiVersion, String xml, String retrieveName) {
			this.connection = connection;
			this.apiVersion = apiVersion;
			this.xml = xml;
			this.retrieveName = retrieveName;
		}
		
		@Override
		protected RetrieveWorkerResults call() throws Exception {
			
			RetrieveWorkerResults workerResults = new RetrieveWorkerResults();
			
			try {
				Package pkg = readPackageFromXml();
				workerResults.setPackage(pkg);
				
				// Need a better log handler for this. Need a log handler
				// that does not include the zip contents in the log entry.
				SOAPLogHandler logHandler = new SOAPLogHandler();
				logHandler.setTitle("Retrieve");
				// Full name is null sometimes.
				logHandler.setSummary(String.format("Name: %s", retrieveName));
				connection.getConfig().addMessageHandler(logHandler);
				workerResults.setLogHandler(logHandler);
				
				RetrieveRequest retrieveRequest = new RetrieveRequest();
				retrieveRequest.setApiVersion(apiVersion);
				// TODO: Handle server package
				retrieveRequest.setUnpackaged(pkg);
				
				System.out.println("1");
				
				AsyncResult asyncResult = connection.retrieve(retrieveRequest);
				
				// This is the retrieve request xml that we want to report to the user.
				// We have to capture this so that we can set it in the log handler
				// later. In between now and then the log handler will be used for a
				// bunch of async status checks that won't be interesting to report
				// to the user.
				String requestLog = logHandler.getRequest();
				
				asyncResult = waitForCompletionOfRetrieve(asyncResult);
				workerResults.setAsyncResult(asyncResult);
				
				System.out.println("3");
				
				if (asyncResult.getState() == AsyncRequestState.Completed) {
					
					System.out.println("4");
					
					RetrieveResult retrieveResult = connection.checkRetrieveStatus(asyncResult.getId());
					workerResults.setRetrieveResult(retrieveResult);
					
					// Set the retrieve request log xml. This is the xml that we want to provide
					// to the user to go along with the response xml for the checkRetrieveStatus
					// call.
					logHandler.setRequest(requestLog);
					
					System.out.println("4a");
					
					Map<String, SortedMap<String, FileProperties>> filePropertiesMap = createFilePropertiesMap(retrieveResult.getFileProperties());
					workerResults.setFileProperties(filePropertiesMap);
					
					TreeItem<String> fileNamesTree = createFileNamesTree(retrieveName, filePropertiesMap);
					workerResults.setFileNames(fileNamesTree);
					
					Map<String, String> filesMap = createFilesMap(retrieveResult.getZipFile());
					workerResults.setFiles(filesMap);
				}
				
				System.out.println("7");
				
				connection.getConfig().clearMessageHandlers();
			}
			catch (ConnectionException e) {
				
			}
			
			return workerResults;
		}
		
		private Package readPackageFromXml() throws PullParserException, ConnectionException, IOException {
			
			XmlInputStream xin = new XmlInputStream();
			xin.setInput(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))), "UTF-8");
			
			TypeMapper mapper = new TypeMapper();
			TypeInfo typeInfo = new TypeInfo(Main.getMetadataNamespace(),
					                         "Package",
					                         Main.getMetadataNamespace(),
					                         "Package",
					                         0,
					                         1,
					                         true);
			
			xin.peekTag();
			
			return (Package)mapper.readObject(xin, typeInfo, Package.class);
		}
		
		private AsyncResult waitForCompletionOfRetrieve(AsyncResult asyncResult) throws ConnectionException, InterruptedException {
			
			int poll = 0;
			long waitTimeMilliSecs = 1000;
			while (!asyncResult.isDone()) {
				System.out.println("2");
				Thread.sleep(waitTimeMilliSecs);
				waitTimeMilliSecs *= 2;
				poll++;
				if (poll > MAX_POLL_REQUESTS) {
					// Time out
					break;
				}
				asyncResult = connection.checkStatus(new String[]{asyncResult.getId()})[0];
			}
			
			return asyncResult;
		}
		
		private Map<String, SortedMap<String, FileProperties>> createFilePropertiesMap(FileProperties[] filePropertiesList) {
			
			Map<String, SortedMap<String, FileProperties>> filePropertiesMap = new TreeMap<>();
			for (FileProperties fp : filePropertiesList) {
				SortedMap<String, FileProperties> filePropertiesForType = filePropertiesMap.get(fp.getType());
				if (filePropertiesForType == null) {
					filePropertiesForType = new TreeMap<>();
					filePropertiesMap.put(fp.getType(), filePropertiesForType);
				}
				filePropertiesForType.put(fp.getFileName(), fp);
			}
			
			return filePropertiesMap;
		}
		
		private TreeItem<String> createFileNamesTree(String rootName, Map<String, SortedMap<String, FileProperties>> filePropertiesMap) {
			
			TreeItem<String> root = new TreeItem<>(rootName);
			
			for (String type : filePropertiesMap.keySet()) {
				TreeItem<String> typeItem = new TreeItem<>(type);
				root.getChildren().add(typeItem);
				SortedMap<String, FileProperties> filePropertiesMapForType = filePropertiesMap.get(type);
				for (String fileName : filePropertiesMapForType.keySet()) {
					typeItem.getChildren().add(new TreeItem<String>(fileName));
				}
			}
			
			return root;
		}
		
		private Map<String, String> createFilesMap(byte[] zip) throws IOException {
			
			Map<String, String> filesMap = new HashMap<>();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(zip);
			ZipInputStream zis = new ZipInputStream(bais);
			
			System.out.println("5");
			
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				System.out.println("6");
				String fileName = ze.getName();
				System.out.println(fileName);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = zis.read(buffer)) > 0) {
					baos.write(buffer, 0, bytesRead);
				}
				baos.close();
				// Need to check for non-xml files before doing this
				String xml = new String(baos.toByteArray(), Charset.forName("UTF-8"));
				filesMap.put(fileName, xml);
				ze = zis.getNextEntry();
			}
			
			return filesMap;
		}
	}
	
	private static class FileController {
	
		private String fileName;
		private String retrieveName;
		private Tab tab;
		private Editor editor;
		private boolean newFlag;
		
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
		public String getRetrieveName() {
			return retrieveName;
		}
		public void setRetrieveName(String retrieveName) {
			this.retrieveName = retrieveName;
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
		
		public boolean isNew() {
			return newFlag;
		}
		public void setNew(boolean newFlag) {
			this.newFlag = newFlag;
		}
	}
	
	private RetrieveMode mode;
	
	private Map<String, FileController> fileControllersByName = new HashMap<String, FileController>();
	private int newFileCounter = 1;
	
	private Map<Tab, FileController> fileControllersByTab = new HashMap<Tab, FileController>();
	
	private Map<String, RetrieveWorkerResults> retrieves = new HashMap<String, RetrieveWorkerResults>();
	
	private AnchorPane root;
	private ToolBar toolBar;
	private Button newButton;
	private Button retrieveButton;
	private Button cancelButton;
	private TabPane tabPane;
	
	public RetrieveEditorController(RetrieveMode mode) {
		this.mode = mode;
		createGraph();
		mode.getPerspective().getApplication().metadataConnection().addListener((o, oldValue, newValue) -> handleMetadataConnectionChanged());
		mode.getPerspective().getApplication().userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void show(String retrieveName, String fileName) {
		
		String qualifiedName = createQualifiedName(retrieveName, fileName);
		
		if (fileControllersByName.get(qualifiedName) != null) {
			tabPane.getSelectionModel().select(fileControllersByName.get(qualifiedName).getTab());
			return;
		}
		
		RetrieveWorkerResults workerResults = retrieves.get(retrieveName);
		if (workerResults == null) {
			return;
		}
		
		Map<String, String> files = workerResults.getFiles();
		String file = files.get(fileName);
		if (file == null) {
			return;
		}
		
		final FileController fileController = new FileController();
		fileController.setRetrieveName(retrieveName);
		fileController.setFileName(fileName);
		fileController.setNew(false);
		
		Tab tab = new Tab();
		tab.setText(fileName);
		tab.setOnSelectionChanged(e -> setDisablesForTabSelection());
		tab.setOnClosed(e -> {
			fileControllersByName.remove(qualifiedName);
			fileControllersByTab.remove(tab);
			setDisablesForTabSelection();
		});
		fileController.setTab(tab);
		
		final Editor editor = EditorFactory.createEditor(null);
		editor.setMetadataAsXml(file);
		editor.setEditable(false);
		// TODO Disable editing
		tab.setContent(editor.getRoot());
		fileController.setEditor(editor);
		
		fileControllersByName.put(qualifiedName, fileController);
		fileControllersByTab.put(tab, fileController);
		
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		
		setDisablesForTabSelection();
	}
	
	public void close(String retrieveName, String fileName) {	
		
		String qualifiedName = createQualifiedName(retrieveName, fileName);
		
		FileController fileController = fileControllersByName.get(qualifiedName);
		if (fileController == null) {
			return;
		}
		
		tabPane.getTabs().remove(fileController.getTab());
		fileControllersByName.remove(qualifiedName);
		fileControllersByTab.remove(fileController.getTab());
		
		setDisablesForTabSelection();
		
		// TODO: Special handling for zero remaining file controllers and tabs
	}
	
	public void closeAll() {
		tabPane.getTabs().clear();
		fileControllersByName.clear();
		fileControllersByTab.clear();
		
		setDisablesForTabSelection();
		
		// TODO: Special handling for zero remaining file controllers and tabs
	}

	private void createGraph() {
		
		root = new AnchorPane();
		
		toolBar = new ToolBar();
		AnchorPane.setTopAnchor(toolBar, 0.0);
		AnchorPane.setLeftAnchor(toolBar, 0.0);
		AnchorPane.setRightAnchor(toolBar, 0.0);
		root.getChildren().add(toolBar);
		
		newButton = new Button("New");
		newButton.setOnAction(e -> handleNewButtonClicked(e));
		toolBar.getItems().add(newButton);
		
		toolBar.getItems().add(new Separator());
		
		retrieveButton = new Button("Retrieve");
		retrieveButton.setDisable(true);
		retrieveButton.setOnAction(e -> handleRetrieveButtonClicked(e));
		toolBar.getItems().add(retrieveButton);
		
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
		
	}
	
	private void handleNewButtonClicked(ActionEvent e) {
		
		String newFileName = "New " + newFileCounter;
		newFileCounter++;
		
		String qualifiedName = createQualifiedName(null, newFileName);
		
		final FileController fileController = new FileController();
		fileController.setFileName(newFileName);
		fileController.setNew(true);
		
		Tab tab = new Tab();
		tab.setText(newFileName);
		tab.setOnSelectionChanged(es -> setDisablesForTabSelection());
		tab.setOnClosed(ec -> {
			fileControllersByName.remove(qualifiedName);
			fileControllersByTab.remove(tab);
			setDisablesForTabSelection();
		});
		fileController.setTab(tab);
		
		// This should be creating an editor for package manifests
		final Editor editor = EditorFactory.createEditor(null);
		editor.dirty().addListener((o, oldValue, newValue) -> setDisablesForTabSelection());
		tab.setContent(editor.getRoot());
		fileController.setEditor(editor);
		
		fileControllersByName.put(qualifiedName, fileController);
		fileControllersByTab.put(tab, fileController);
		
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		
		setDisablesForTabSelection();
	}
	
	private void handleRetrieveButtonClicked(ActionEvent e) {
		
		if (mode.getPerspective().getApplication().metadataConnection().get() == null) {
			return;
		}
		
		retrieveButton.setDisable(true);
		
		FileController fileController = fileControllersByTab.get(tabPane.getSelectionModel().getSelectedItem());
		Editor editor = fileController.getEditor();
		editor.lock();
		String xml = editor.getMetadataAsXml();
		
		String retrieveName = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		final RetrieveWorker retrieveWorker = new RetrieveWorker(mode.getPerspective().getApplication().metadataConnection().get(),
				                                                 mode.getPerspective().getApplication().apiVersion().get(),
				                                                 xml,
				                                                 retrieveName);
		retrieveWorker.setOnSucceeded(es -> {
			mode.getPerspective().getLogController().log(retrieveWorker.getValue().getLogHandler());
			
			if (fileController.isNew()) {
				String oldQualifiedName = createQualifiedName(fileController.getRetrieveName(), fileController.getFileName());
				fileControllersByName.remove(oldQualifiedName);
				String manifestName = "package.xml";
				fileController.setFileName(manifestName);
				final String newQualifiedName = createQualifiedName(retrieveName, manifestName);
				fileControllersByName.put(newQualifiedName, fileController);
				fileController.setNew(false);
				
				Tab tab = fileController.getTab();
				tab.setText(manifestName);
				tab.setOnClosed(ec -> {
					fileControllersByName.remove(newQualifiedName);
					fileControllersByTab.remove(tab);
					setDisablesForTabSelection();
				});
			}
			
			retrieves.put(retrieveName, retrieveWorker.getValue());
			mode.getNavigatorController().addRetrieve(retrieveName, retrieveWorker.getValue().getFileProperties(), retrieveWorker.getValue().getFileNames());
			
			editor.unlock();
			setDisablesForOperationCompletion();
		});
		
		cancelButton.setOnAction(ec -> {
			retrieveWorker.cancel();
			handleCancelButtonClicked(ec);
		});
		cancelButton.setDisable(false);
		
		new Thread(retrieveWorker).start();
	}
	
	private void handleCancelButtonClicked(ActionEvent e) {
		
	}
	
	private void setDisablesForTabSelection() {
		
		if (tabPane.getTabs().size() == 0) {
			retrieveButton.setDisable(true);
		}
		else {
			boolean connected = mode.getPerspective().getApplication().metadataConnection().get() != null;
			
			Tab tab = tabPane.getSelectionModel().getSelectedItem();
			FileController fileController = fileControllersByTab.get(tab);
			Editor editor = fileController.getEditor();
			if (fileController.isNew()) {
				if (editor.dirty().get() && connected) {
					retrieveButton.setDisable(false);
				}
				else {
					retrieveButton.setDisable(true);
				}
				// TODO
			}
			else {
				if (editor.dirty().get() && connected) {
					retrieveButton.setDisable(false);
				}
				else {
					retrieveButton.setDisable(true);
				}
				// TODO
			}
		}
	}
	
	private void setDisablesForOperationCompletion() {
		cancelButton.setDisable(true);
		setDisablesForTabSelection();
	}
	
	private String createQualifiedName(String retrieveName, String fileName) {
		if (retrieveName != null) {
			return retrieveName + ":" + fileName;
		}
		else {
			return ":" + fileName;
		}
	}
}
