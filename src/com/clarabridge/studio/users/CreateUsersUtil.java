package com.clarabridge.studio.users;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class CreateUsersUtil {

	private static String PROPERTIES_FILE_PATH = "/studio_users.properties";
	
	public static void main(String[] args) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE_PATH);
			prop.load(input);
			
			Path csvPath = Paths.get(prop.getProperty("csv.file"));
		    List<String> lines = Files.readAllLines(csvPath);
		    
		    String url = prop.getProperty("url");
		    String login = prop.getProperty("login");
		    String password = prop.getProperty("password");
		    for (String line : lines) {
		    	try {
		    		String payload = getJsonObj(line);
		    		callApi(url, login, password, payload);
		    	} catch (Exception e) {
		    		System.err.println("User '" + line + "' was not created");
		    		e.printStackTrace();
		    	}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	private static String getJsonObj(String line) {
		//escape the double quotes in json string
		return "{\"firstName\":\"test1\",\"lastName\":\"test2\",\"userEmail\":\"a1@epam.com\",\"password\":\"12345678\",\"licenseTypeId\":2,\"registered\":true,\"defaultMasterAccountId\":1,\"deleteFromAllMAsExceptDefault\":false,\"masterAccountPermissions\":[{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"drillToFeedbackInView\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"refreshDashboard\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"editDashboard\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"deleteDashboard\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"createDashboard\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"drillDashboard\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"shareEdit\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"shareView\"},{\"masterAccountId\":\"1\",\"group\":\"dashboard\",\"action\":\"changeOwnership\"}]}";
	}
	
	private static String callApi(String urlString, String login, String password, String payload) {
		String line;
	    StringBuffer jsonString = new StringBuffer();
	    try {

	        URL url = new URL(urlString);

	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			String authString = login + ":" + password;
			byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        writer.write(payload);
	        writer.close();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        while ((line = br.readLine()) != null) {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	    } catch (Exception e) {
	            throw new RuntimeException(e.getMessage());
	    }
	    return jsonString.toString();
	}

}
