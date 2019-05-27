package com.clarabridge.studio.users.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.Random;

public class UserGeneratorConfig {
	public int count;
	public long masterAccountId;
	public int licenseId;
	public String password;
	public String emailDomain;
	
	public String[] firstNames;
	public String[] lastNames;
	public String[] groups;
	public String[] customValues;
	public String randomSeed;
	
	public static UserGeneratorConfig fromProperties(Properties props) {
		UserGeneratorConfig config = new UserGeneratorConfig();
		config.count = Integer.parseInt(props.getProperty("count"));
		config.masterAccountId = Long.parseLong(props.getProperty("masterAccountId"));
		config.licenseId = Integer.parseInt(props.getProperty("licenseId"));
		config.password = props.getProperty("password");
		config.emailDomain = props.getProperty("emailDomain");
		
		config.firstNames = getArrayProperty(props, "firstNames");
		config.lastNames = getArrayProperty(props, "lastNames");
		config.groups = getArrayProperty(props, "groups");
		config.customValues = getArrayProperty(props, "customValues");
		
		config.randomSeed = props.getProperty("randomSeed");
		return config;
	}
	
	private static String[] getArrayProperty(Properties props, String name) {
		String[] values = props.getProperty(name).split(",");
		for (int i = 0; i < values.length; i++)
			values[i] = values[i].trim();
		return values;
	}
	
	private UserGeneratorConfig() {}
	
	public Random getRandomizer() {
		if (StringUtils.isEmpty(randomSeed))
			return new Random();
		else return new Random(randomSeed.hashCode());
	}

	@Override
	public String toString() {
		return String.format("GeneratorConfig:\n"
				+ "Count=%d\n"
				+ "Master Account=%d\n"
				+ "License=%d\n"
				+ "Names count=(%d first, %d last)\n"
				+ "Groups count=%d\n"
				+ "Custom values count=%d\n"
				+ "Seed=%s", 
				count, masterAccountId, licenseId, firstNames.length, lastNames.length, 
				groups.length, customValues.length, randomSeed);
	}
}
