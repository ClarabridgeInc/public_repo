package com.clarabridge.studio.users;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.opencsv.CSVReader;

public class CreateUsersUtil {

	private static String PROPERTIES_FILE = "studio_users.properties";
	
	private static AtomicInteger lineNum = new AtomicInteger(1);
	public static void main(String[] args) {
		Properties prop = new Properties();
		InputStream input = null;
		CSVReader reader = null;
		try {
		    File file = new File(PROPERTIES_FILE);
			input = new FileInputStream(file);
			prop.load(input);
			
		    reader = new CSVReader(new FileReader(prop.getProperty("csv.file")), ',' , '"' , 0);
		    List<String[]> lines = reader.readAll();   
		    String baseUrl = prop.getProperty("url");
		    String login = prop.getProperty("login");
		    String password = prop.getProperty("password");
		    //registered = !force.flag
		    final boolean registered = prop.getProperty("force.flag") != null ? Integer.valueOf(prop.getProperty("force.flag")) == 0 : true;
		    int threadNum = prop.getProperty("threads") != null ? Integer.valueOf(prop.getProperty("threads")) : 1;
	        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
	        for (int i = 0; i < threadNum; i++)  {
                executor.execute(new Runnable() {
                    public void run() {
                        int currLine = lineNum.getAndIncrement();
                        while (currLine < lines.size()) {
                            try {
                                String[] fields = lines.get(currLine);
                                if (fields.length < 6) {
                                    throw new RuntimeException("number of fields for '" + Arrays.toString(lines.get(currLine)) + "' is less than 6");
                                }
                                if (fields[3] == null || "".equals(fields[3])) {
                                    fields[3] = fields[1] + "-whitbread";
                                }
                                String url = String.format(baseUrl, fields[2]);
                                String payload = getJsonObj(fields, registered);
                                callApi(url, login, password, payload, fields[5]);
                            } catch (Exception e) {
                                System.err.println("User '" + Arrays.toString(lines.get(currLine)) + "' was not created, cause: "  + e.getMessage());
                                //e.printStackTrace();
                            }
                            currLine = lineNum.getAndIncrement();
                        }
                    }
                });
            }
	        executor.shutdown();
	        try {
	            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	        } catch (InterruptedException e) {
	            System.err.println("executer terminated");
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
			if (reader != null) {
			    try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
		}
		
		
	}
	
	private static String getJsonObj(String[] fields, boolean registered) {
	    String[] permissions = new String[] {
	            "{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"createDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"deleteDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"editDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"refreshDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"drillDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"shareEdit\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"shareView\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"changeOwnership\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"group\",\"action\":\"manageGroups\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"user\",\"action\":\"manageUsers\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"masterAccount\",\"action\":\"manageSettings\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"masterAccount\",\"action\":\"shareCreateUser\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"masterAccount\",\"action\":\"conductSecurityAudit\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"contentProvider\",\"action\":\"subscribeContentProvider\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"contentProvider\",\"action\":\"subscribeWidget\"}",
	            "{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"createDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"deleteDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"editDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"refreshDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"drillDashboard\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"shareEdit\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"shareView\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"dashboard\",\"action\":\"changeOwnership\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"group\",\"action\":\"manageGroups\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"user\",\"action\":\"manageUsers\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"masterAccount\",\"action\":\"shareCreateUser\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"contentProvider\",\"action\":\"subscribeContentProvider\"},{\"masterAccountId\":\"" + fields[5] + "\",\"group\":\"contentProvider\",\"action\":\"subscribeWidget\"}",
	            "",
	    };
		String baseStr = "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"userEmail\":\"%s\",\"password\":\"%s\",\"licenseTypeId\":%s,\"registered\":" + registered + ",\"defaultMasterAccountId\":%s,\"deleteFromAllMAsExceptDefault\":false,\"masterAccountPermissions\":[%s]}";
		return String.format(baseStr, fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], permissions[Integer.valueOf(fields[4]) - 1]);
		
	}
	
	private static String callApi(String urlString, String login, String password, String payload, String maId) {
		String line;
	    StringBuffer jsonString = new StringBuffer();
	    System.out.println("creating user : ma=" + maId + ", payload="  + payload );
	    HttpURLConnection connection = null;
	    try {
	        URL url = new URL(urlString);

	        connection = (HttpURLConnection) url.openConnection();
			String authString = login + ":" + password;
			byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("ma-id", maId);
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        writer.write(payload);
	        writer.close();
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
	    return jsonString.toString();
	}

}
