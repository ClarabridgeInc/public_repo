package com.clarabridge.studio.users.http;

public class Urls {

	public static String userUploadUrl(String baseUrl, String email) {
		return baseUrl + "/users/" + email;
	}

	public static String groupsUrl(String baseUrl) {
		return baseUrl + "/groups";
	}

	public static String addToGroupUrl(String baseUrl) {
		return baseUrl + "/groups/add/users";
	}

}
