package com.clarabridge.studio.users;

import com.clarabridge.studio.users.entity.Group;
import com.clarabridge.studio.users.entity.GroupsCache;
import com.clarabridge.studio.users.http.HttpClient;
import com.clarabridge.studio.users.http.HttpMethod;
import com.clarabridge.studio.users.http.HttpRequest;
import com.clarabridge.studio.users.http.Urls;
import com.clarabridge.studio.users.json.CsvToJsonUserConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class CreateUsersUtil {

	public static final int FIRST_NAME_FIELD = 0;
	public static final int LAST_NAME_FIELD = 1;
	public static final int USER_EMAIL_FIELD = 2;
	public static final int PASSWORD_FIELD = 3;
	public static final int LICENSE_TYPE_ID_FIELD = 4;
	public static final int DEFAULT_MA_ID_FIELD = 5;
	public static final int GROUP_NAME_FIELD = 6;
	public static final int UNIQUE_ID_FIELD = 7;
	public static final int CUSTOM_FIELD = 8;

	public static final long CX_DESIGNER_LICENSE = 1L;
	public static final long CX_STUDIO_LICENSE = 2L;
	public static final long CX_STUDIO_BASIC_LICENSE = 4L;

	private static final int MINIMUM_REQUIRED_FIELDS_NUMBER = 6;

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
			String baseUrl = prop.getProperty("url.base");
			String login = prop.getProperty("login");
			String password = prop.getProperty("password");
			boolean extended = Boolean.parseBoolean(prop.getProperty("extended", "false"));
			System.out.println("[i] Extended upload: " + extended);

			//registered = !force.flag
			final boolean registered = prop.getProperty("force.flag") == null
					|| Integer.valueOf(prop.getProperty("force.flag")) == 0;

			int threadNum = prop.getProperty("threads") != null
					? Integer.valueOf(prop.getProperty("threads")) : 1;

			System.out.println(">>> Getting necessary metadata");
			List<Long> masterAccountIds = getMasterAccountIds(lines);
			GroupsCache groupsCache = getGroups(Urls.groupsUrl(baseUrl), login, password, masterAccountIds);

			System.out.println(">>> Starting user upload using " + threadNum + " thread(s)");
			HttpClient client = new HttpClient();
			ExecutorService executor = Executors.newFixedThreadPool(threadNum);
			for (int i = 0; i < threadNum; i++) {
				executor.execute(() -> {
					int currLine = lineNum.getAndIncrement();
					while (currLine < lines.size()) {
						try {
							String[] fields = lines.get(currLine);

							if (fields.length < MINIMUM_REQUIRED_FIELDS_NUMBER) {
								throw new RuntimeException("Expected at least " + MINIMUM_REQUIRED_FIELDS_NUMBER + " columns filled at this line, but got " + fields.length);
							}
							if (fields[PASSWORD_FIELD] == null || "".equals(fields[PASSWORD_FIELD])) {
								fields[PASSWORD_FIELD] = fields[LAST_NAME_FIELD] + "-whitbread";
							}

							String userCreationUrl = String.format(Urls.userUploadUrl(baseUrl, fields[USER_EMAIL_FIELD]), fields[USER_EMAIL_FIELD]);
							System.out.println("[i] Creating user " + fields[USER_EMAIL_FIELD]);
							String userCreationPayload = new CsvToJsonUserConverter().getCreationPayload(fields, registered, extended);
							HttpRequest userCreationRequest = new HttpRequest();
							userCreationRequest.setMethod(HttpMethod.POST);
							userCreationRequest.setUrl(userCreationUrl);
							userCreationRequest.setAuthentication(login, password);
							userCreationRequest.setPayload(userCreationPayload);
							userCreationRequest.setMasterAccountId(Long.parseLong(fields[DEFAULT_MA_ID_FIELD]));
							client.doRequest(userCreationRequest);

							if (extended) {
								System.out.println("[i] Adding user " + fields[USER_EMAIL_FIELD] + " to specified groups");
								String groupAdditionPayload = new CsvToJsonUserConverter().getGroupsPayload(fields, groupsCache);

								HttpRequest groupsRequest = new HttpRequest();
								groupsRequest.setMethod(HttpMethod.PUT);
								groupsRequest.setUrl(Urls.addToGroupUrl(baseUrl));
								groupsRequest.setAuthentication(login, password);
								groupsRequest.setPayload(groupAdditionPayload);
								groupsRequest.setMasterAccountId(Long.parseLong(fields[DEFAULT_MA_ID_FIELD]));
								client.doRequest(groupsRequest);
							}
						} catch (Exception e) {
							System.out.println("[e] !!! User '" + Arrays.toString(lines.get(currLine)) + "' was not created.\r\n"
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
				System.out.println("[w] Executor terminated");
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

	private static List<Long> getMasterAccountIds(List<String[]> lines) {
		return lines.stream()
				.skip(1)
				.map(entry -> entry[DEFAULT_MA_ID_FIELD])
				.map(Long::valueOf)
				.distinct()
				.collect(toList());
	}

	private static GroupsCache getGroups(String groupsUrl, String login, String password, List<Long> masterAccountIds) {
		System.out.println("[i] Getting groups for master accounts " + masterAccountIds);

		HttpClient httpClient = new HttpClient();
		GroupsCache groupsCache = new GroupsCache();
		Gson gson = new Gson();

		masterAccountIds.forEach(masterAccountId -> {
			HttpRequest request = new HttpRequest();
			request.setMethod(HttpMethod.GET);
			request.setAuthentication(login, password);
			request.setMasterAccountId(masterAccountId);
			request.setUrl(groupsUrl);

			List<Group> masterAccountGroups = gson.fromJson(
					httpClient.doRequest(request),
					new TypeToken<List<Group>>() {}.getType());
			groupsCache.addMasterAccountGroups(masterAccountId, masterAccountGroups);
			System.out.println("[i] Found " + masterAccountGroups.size() + " groups for master account " + masterAccountId);
		});

		return groupsCache;
	}

}
