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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.opencsv.CSVReader;

import static java.util.stream.Collectors.joining;

public class CreateUsersUtil {

	private static final int FIRST_NAME_FIELD = 0;
	private static final int LAST_NAME_FIELD = 1;
	private static final int USER_EMAIL_FIELD = 2;
	private static final int PASSWORD_FIELD = 3;
	private static final int LICENSE_TYPE_ID_FIELD = 4;
	private static final int DEFAULT_MA_ID_FIELD = 5;

	private static final long CX_DESIGNER_LICENSE = 1L;
	private static final long CX_STUDIO_LICENSE = 2L;
	private static final long CX_STUDIO_BASIC_LICENSE = 4L;

	private static final int REQUIRED_FIELDS_NUMBER = 6;

	private static AtomicInteger lineNum = new AtomicInteger(1);

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("2 arguments are required: <config path>, <csv path>");
		}

		String configPath = args[0];
		String csvPath = args[1];

		Properties prop = new Properties();
		InputStream input = null;
		CSVReader reader = null;
		try {
			File file = new File(configPath);
			input = new FileInputStream(file);
			prop.load(input);

			reader = new CSVReader(new FileReader(csvPath), ',', '"', 0);
			List<String[]> lines = reader.readAll();
			String baseUrl = prop.getProperty("url");
			String login = prop.getProperty("login");
			String password = prop.getProperty("password");

			//registered = !force.flag
			final boolean registered = prop.getProperty("force.flag") == null
					|| Integer.valueOf(prop.getProperty("force.flag")) == 0;

			int threadNum = prop.getProperty("threads") != null
					? Integer.valueOf(prop.getProperty("threads")) : 1;

			ExecutorService executor = Executors.newFixedThreadPool(threadNum);
			for (int i = 0; i < threadNum; i++) {
				executor.execute(() -> {
					int currLine = lineNum.getAndIncrement();
					while (currLine < lines.size()) {
						try {
							String[] fields = lines.get(currLine);
							if (fields.length != REQUIRED_FIELDS_NUMBER) {
								throw new RuntimeException(
										"number of fields for '" + Arrays.toString(lines.get(currLine))
												+ "' is less than " + REQUIRED_FIELDS_NUMBER);
							}
							if (fields[PASSWORD_FIELD] == null || "".equals(fields[PASSWORD_FIELD])) {
								fields[PASSWORD_FIELD] = fields[LAST_NAME_FIELD] + "-whitbread";
							}
							String url = String.format(baseUrl, fields[USER_EMAIL_FIELD]);
							String payload = getJsonObj(fields, registered);
							callApi(url, login, password, payload, fields[DEFAULT_MA_ID_FIELD]);
						} catch (Exception e) {
							System.out.println("!!! User '" + Arrays.toString(lines.get(currLine)) + "' was not created.\r\n"
								+ "\tCause: " + e.getMessage());
						}
						currLine = lineNum.getAndIncrement();
					}
				});
			}
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				System.out.println("executer terminated");
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
		long masterAccountId = Long.parseLong(fields[DEFAULT_MA_ID_FIELD]);

		Map<Long, String> permissions = new HashMap<>();
		permissions.put(CX_DESIGNER_LICENSE, Stream.of(
				createPermissionJson(masterAccountId,"dashboard", "createDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "deleteDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "editDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "refreshDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "drillDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "shareEdit"),
				createPermissionJson(masterAccountId,"dashboard", "shareView"),

				createPermissionJson(masterAccountId,"group", "manageGroups"),
				createPermissionJson(masterAccountId,"user", "manageUsers"),

				createPermissionJson(masterAccountId,"masterAccount", "manageSettings"),
				createPermissionJson(masterAccountId,"masterAccount", "shareCreateUser"),
				createPermissionJson(masterAccountId,"masterAccount", "conductSecurityAudit"),

				createPermissionJson(masterAccountId,"contentProvider", "subscribeContentProvider"),
				createPermissionJson(masterAccountId,"contentProvider", "subscribeWidget"))
				.collect(joining(",")));

		permissions.put(CX_STUDIO_LICENSE, Stream.of(
				createPermissionJson(masterAccountId,"dashboard", "createDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "deleteDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "editDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "refreshDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "drillDashboard"),
				createPermissionJson(masterAccountId,"dashboard", "shareEdit"),
				createPermissionJson(masterAccountId,"dashboard", "shareView"),
				createPermissionJson(masterAccountId,"dashboard", "changeOwnership"),

				createPermissionJson(masterAccountId,"group", "manageGroups"),
				createPermissionJson(masterAccountId,"user", "manageUsers"),

				createPermissionJson(masterAccountId,"masterAccount", "shareCreateUser"),

				createPermissionJson(masterAccountId,"contentProvider", "subscribeContentProvider"),
				createPermissionJson(masterAccountId,"contentProvider", "subscribeWidget"))
				.collect(joining(",")));

		String licensePermissions = permissions.getOrDefault(Long.valueOf(fields[LICENSE_TYPE_ID_FIELD]), "");

		String baseStr = "{"
				+ "\"firstName\":\"%s\","
				+ "\"lastName\":\"%s\","
				+ "\"userEmail\":\"%s\","
				+ "\"password\":\"%s\","
				+ "\"licenseTypeId\":%s,"
				+ "\"registered\":%s,"
				+ "\"defaultMasterAccountId\":%s,"
				+ "\"deleteFromAllMAsExceptDefault\":false,"
				+ "\"masterAccountPermissions\":[%s]"
				+ "}";
		return String.format(baseStr,
				fields[FIRST_NAME_FIELD], fields[LAST_NAME_FIELD], fields[USER_EMAIL_FIELD], fields[PASSWORD_FIELD],
				fields[LICENSE_TYPE_ID_FIELD], String.valueOf(registered), fields[DEFAULT_MA_ID_FIELD],
				licensePermissions);
	}

	private static String createPermissionJson(long masterAccountId, String group, String name) {
		return "{\"masterAccountId\":\"" + masterAccountId + "\","
				+ "\"group\":\"" + group + "\","
				+ "\"action\":\"" + name + "\"}";
	}

	private static String callApi(String urlString, String login, String password, String payload, String maId) {
		String line;
		StringBuilder jsonString = new StringBuilder();

		System.out.println("> " + urlString);
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
