package workbenchfx;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import com.sforce.soap.enterprise.GetUserInfoResult;

public class LogController {
	
	public interface LogMessage {
		String getTitle();
		String getUrl();
		String getSummary();
		String getRequest();
		String getResponse();
	}
	
	public static class LogRow {
		private StringProperty operation = new SimpleStringProperty(this, "operation");
		private StringProperty description = new SimpleStringProperty(this, "description");
		
		public LogRow(String operation, String value) {
			this.operation.set(operation);
			this.description.set(value);
		}
		
		public StringProperty operationProperty() {
			return operation;
		}
		
		public StringProperty descriptionProperty() {
			return description;
		}
	}
	
	private Main application;
	
	private AnchorPane root;
	private ToolBar toolBar;
	private Button clearButton;
	private TreeTableView<LogRow> treeTableView;
	TreeItem<LogRow> treeTableViewRoot;
	private TreeTableColumn<LogRow, String> operationColumn;
	private TreeTableColumn<LogRow, String> descriptionColumn;
	
	public LogController(Main application) {
		this.application = application;
		createGraph();
		application.userInfo().addListener((o, oldValue, newValue) -> handleUserInfoChanged(oldValue, newValue));
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void log(LogMessage message) {
		
		String title = message.getTitle() != null ? message.getTitle() : "";
		String summary = message.getSummary() != null ? message.getSummary() : "";
		
		TreeItem<LogRow> summaryRow = new TreeItem<>(new LogRow(title, summary));
		treeTableViewRoot.getChildren().add(summaryRow);
		
		if (message.getRequest() != null) {
			TreeItem<LogRow> requestRow = new TreeItem<>(new LogRow("    Request", "")); // TODO Add date/time
			summaryRow.getChildren().add(requestRow);
			TreeItem<LogRow> requestDetailsRow = new TreeItem<>(new LogRow("", prettyFormat(message.getRequest())));
			requestRow.getChildren().add(requestDetailsRow);
		}
		
		if (message.getResponse() != null) {
			TreeItem<LogRow> responseRow = new TreeItem<>(new LogRow("    Response", "")); // TODO Add date/time
			summaryRow.getChildren().add(responseRow);
			TreeItem<LogRow> responseDetailsRow = new TreeItem<>(new LogRow("", prettyFormat(message.getResponse())));
			responseRow.getChildren().add(responseDetailsRow);
		}
		
		clearButton.setDisable(false);
	}
	
	private void createGraph() {
		
		root = new AnchorPane();
		
		toolBar = new ToolBar();
		AnchorPane.setTopAnchor(toolBar, 0.0);
		AnchorPane.setLeftAnchor(toolBar, 0.0);
		AnchorPane.setRightAnchor(toolBar, 0.0);
		root.getChildren().add(toolBar);
		
		clearButton = new Button("Clear");
		clearButton.setDisable(true);
		clearButton.setOnAction(e -> handleClearButtonClicked(e));
		toolBar.getItems().add(clearButton);
		
		treeTableView = new TreeTableView<>();
		treeTableView.setShowRoot(false);
		treeTableView.setPlaceholder(new Label(""));
		AnchorPane.setTopAnchor(treeTableView, 38.0);
		AnchorPane.setBottomAnchor(treeTableView, 0.0);
		AnchorPane.setLeftAnchor(treeTableView, 0.0);
		AnchorPane.setRightAnchor(treeTableView, 0.0);
		root.getChildren().add(treeTableView);
		
		operationColumn = new TreeTableColumn<>("Operation");
		operationColumn.setSortable(false);
		operationColumn.setPrefWidth(150.0);
		operationColumn.setCellValueFactory(new TreeItemPropertyValueFactory<LogRow, String>("operation"));
		treeTableView.getColumns().add(operationColumn);
		
		descriptionColumn = new TreeTableColumn<>("Description");
		descriptionColumn.setSortable(false);
		descriptionColumn.setPrefWidth(600.0);
		descriptionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<LogRow, String>("description"));
		treeTableView.getColumns().add(descriptionColumn);
		
		treeTableViewRoot = new TreeItem<>(new LogRow("ROOT", "ROOT"));
		treeTableView.setRoot(treeTableViewRoot);
	}
	
	private void handleClearButtonClicked(ActionEvent e) {	
		treeTableViewRoot.getChildren().clear();
		clearButton.setDisable(true);
	}
	
	private void handleUserInfoChanged(GetUserInfoResult oldValue, GetUserInfoResult newValue) {
		treeTableViewRoot.getChildren().clear();
		clearButton.setDisable(true);
	}
	
	private String prettyFormat(String input) {
	    return prettyFormat(input, 2);
	}
	
	private String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
}
