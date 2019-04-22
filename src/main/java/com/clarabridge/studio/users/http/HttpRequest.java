package com.clarabridge.studio.users.http;

public class HttpRequest {

	private HttpMethod method;
	private String url;
	private String login;
	private String password;
	private String payload;
	private long masterAccountId;

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAuthentication(String login, String password) {
		this.setLogin(login);
		this.setPassword(password);
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public long getMasterAccountId() {
		return masterAccountId;
	}

	public void setMasterAccountId(long masterAccountId) {
		this.masterAccountId = masterAccountId;
	}

}
