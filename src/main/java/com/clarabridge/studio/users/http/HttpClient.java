package com.clarabridge.studio.users.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class HttpClient {

	public String doRequest(HttpRequest httpRequest) {
		String line;
		int responseCode;
		StringBuilder jsonString = new StringBuilder();

		HttpURLConnection connection = null;
		try {
			URL url = new URL(httpRequest.getUrl());

			connection = (HttpURLConnection) url.openConnection();
			String authString = httpRequest.getLogin() + ":" + httpRequest.getPassword();
			byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod(httpRequest.getMethod().toString());
			connection.setRequestProperty("ma-id", String.valueOf(httpRequest.getMasterAccountId()));
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

			if (httpRequest.getPayload() != null) {
				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
				writer.write(httpRequest.getPayload());
				writer.close();
			}

			responseCode = connection.getResponseCode();
			if (connection.getHeaderField("error") != null) {
				throw new Exception(connection.getHeaderField("error"));
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		String result = jsonString.toString();
		if (responseCode / 100 != 2) {
			System.out.println("[ERROR] Response code was " + responseCode + " using endpoint: " + httpRequest.getUrl() + ". Result: " + result);
		}

		return result;
	}

}
