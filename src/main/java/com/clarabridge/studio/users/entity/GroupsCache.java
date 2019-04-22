package com.clarabridge.studio.users.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupsCache {

	private Map<Long, Map<String, Long>> cache = new HashMap<>();

	public void addMasterAccountGroups(long masterAccountId, List<Group> groups) {
		Map<String, Long> groupMap = groups.stream()
				.collect(Collectors.toMap(Group::getGroupName, Group::getGroupId));

		cache.put(masterAccountId, groupMap);
	}

	public Long getGroupId(long masterAccountId, String groupName) {
		return cache.get(masterAccountId).get(groupName);
	}

}
