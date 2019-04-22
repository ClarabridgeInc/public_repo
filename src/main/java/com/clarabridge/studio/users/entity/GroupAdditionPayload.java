package com.clarabridge.studio.users.entity;

import java.util.List;

public class GroupAdditionPayload {

	private long masterAccountId;
	private List<String> users;
	private List<Long> groups;

	public long getMasterAccountId() {
		return masterAccountId;
	}

	public void setMasterAccountId(long masterAccountId) {
		this.masterAccountId = masterAccountId;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<Long> getGroups() {
		return groups;
	}

	public void setGroups(List<Long> groups) {
		this.groups = groups;
	}

}
