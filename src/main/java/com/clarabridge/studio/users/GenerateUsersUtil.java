package com.clarabridge.studio.users;

import com.clarabridge.studio.users.entity.UserGeneratorConfig;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

public class GenerateUsersUtil {

	private static final String[] HEADERS = new String[] {
			"First Name", "Last Name", "Email Address", "Password", "License type", "Master Account", 
			"Group Name", "Unique ID", "Custom Field"
	};
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("2 arguments are required: <config path>, <csv path>");
		}

		Properties props = getProperties(args[0]);
		
		String csvPath = args[1];
		try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath))) {
			writer.writeNext(HEADERS);
			writer.writeAll(generateUsers(props));
		};
		
	}
	
	private static List<String[]> generateUsers(Properties props) {
		UserGeneratorConfig config = UserGeneratorConfig.fromProperties(props);
		System.out.println(">>> Generating users:\n" + config.toString());
		
		List<String[]> result = new ArrayList<>(config.count);
		Set<String> emails = new HashSet<String>();
		Random rand = config.getRandomizer();
		for (int i = 0; i < config.count; i++) {
			String first = randomOrDefault(config.firstNames, rand, "First");
			String last = randomOrDefault(config.lastNames, rand, "Last");
			String email = first + "." + last + "@" + config.emailDomain;
			if (emails.contains(email))
				email = first + "." + last + i + "@" + config.emailDomain;
			emails.add(email);
			String password = config.password;
			String license = "" + config.licenseId;
			String maId = "" + config.masterAccountId;
			String group = randomOrDefault(config.groups, rand, "");
			String UID = "";
			String customValue = randomOrDefault(config.customValues, rand, "");
			result.add(new String[] {
					first, last, email, password, license, maId, group, UID, customValue
			});
		}
		return result;
	}
	
	private static String randomOrDefault(String[] values, Random random, String defaultValue) {
		if (values.length == 0)
			return defaultValue;
		else return values[random.nextInt(values.length)];
	}

	private static Properties getProperties(String path) {
		try (InputStream input = new FileInputStream(path)) {

			Properties prop = new Properties();
			prop.load(input);

			return prop;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
