package workbenchfx.editor.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

import com.sforce.soap.metadata.Metadata;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.TypeInfo;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;
import com.sforce.ws.parser.XmlOutputStream;
import com.sforce.ws.wsdl.Constants;

import workbenchfx.Main;

public class DefaultEditor implements Editor {
	
	private static String METADATA_NAME = "Metadata";
	private static String METADATA_STUBS_PACKAGE = "com.sforce.soap.metadata";
	
	private Metadata metadata;
	private TextArea root;
	private BooleanProperty dirtyProperty = new SimpleBooleanProperty();
	
	public DefaultEditor() {
		createGraph();
	}

	public Node getRoot() {
		return root;
	}
	
	public Metadata getMetadata() {
		if (dirtyProperty.get()) {
			setMetadataFromUI();
		}
		return metadata;
	}
	
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
		setUIFromMetadata();
		dirtyProperty.set(false);
	}
	
	public String getMetadataAsXml() {
		return root.getText();
	}
	
	public void setMetadataAsXml(String xml) {
		root.appendText(xml);
		root.setScrollTop(0.0);
		setMetadataFromUI();
	}
	
	public BooleanProperty dirty() {
		return dirtyProperty;
	}
	
	public void lock() {
		root.setDisable(true);
	}
	
	public void unlock() {
		root.setDisable(false);
	}
	
	public void setEditable(boolean editable) {
		root.setEditable(editable);
	}
	
	public boolean isEditable() {
		return root.isEditable();
	}
	
	private void createGraph() {
		
		root = new TextArea();
		root.setEditable(true);
		root.textProperty().addListener(e -> dirtyProperty.set(true));
	}
	
	private void setUIFromMetadata() {
		root.clear();
		if (metadata != null) {
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				XmlOutputStream xout = new XmlOutputStream(baos, "    ");
		        
		        xout.setPrefix("env", Constants.SOAP_ENVELOPE_NS);
		        xout.setPrefix("xsd", Constants.SCHEMA_NS);
		        xout.setPrefix("xsi", Constants.SCHEMA_INSTANCE_NS);
		        xout.setPrefix("", Main.getMetadataNamespace());
		        
		        metadata.write(new QName(Main.getMetadataNamespace(), METADATA_NAME), xout, new TypeMapper());
		        
		        xout.close();
		        
		        String xml = new String(baos.toByteArray(), Charset.forName("UTF-8"));
		        
		        root.appendText(xml);
		        root.setScrollTop(0.0);
			}
			catch (IOException e) {
				// TODO: Fix this
				e.printStackTrace();
			}
		}
		else {
			root.appendText("Null metadata");
		}
	}
	
	private void setMetadataFromUI() {
		
		try {
			String xml = root.getText();
			
			XmlInputStream xin = new XmlInputStream();
			xin.setInput(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))), "UTF-8");
			
			TypeMapper mapper = new TypeMapper();
			
			if (metadata != null) {
				metadata.load(xin, mapper);
			}
			else {
				TypeInfo typeInfo = new TypeInfo(Main.getMetadataNamespace(), 
												 METADATA_NAME, 
											     Main.getMetadataNamespace(), 
											     METADATA_NAME, 
											     0, 
											     1, 
											     true);
				xin.peekTag();
				String rootElementName = xin.getName();
				String rootElementJavaClassName = METADATA_STUBS_PACKAGE + "." + rootElementName;
				Class<?> rootElementJavaClass = Class.forName(rootElementJavaClassName);
				metadata = (Metadata)mapper.readObject(xin, typeInfo, rootElementJavaClass);
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
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
}