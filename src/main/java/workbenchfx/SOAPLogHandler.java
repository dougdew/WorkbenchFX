package workbenchfx;

import java.net.URL;

import workbenchfx.LogController.LogMessage;

import com.sforce.ws.MessageHandler;

public class SOAPLogHandler implements LogMessage, MessageHandler {
	
	String title;
	String url;
	String summary;
	String request;
	String response;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}

	public void handleRequest(URL endpoint, byte[] request) {
		url = endpoint.toString();
		this.request = new String(request);
	}

	public void handleResponse(URL endpoint, byte[] response) {
		this.response = new String(response);
	}
}
