package com.clarabridge.studio.users.json;

import com.clarabridge.studio.users.entity.GroupAdditionPayload;
import com.clarabridge.studio.users.entity.GroupsCache;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.clarabridge.studio.users.CreateUsersUtil.*;
import static java.util.stream.Collectors.toList;

public class CsvToJsonUserConverter {

	public String getCreationPayload(String[] fields, boolean registered, boolean extended) {
		long masterAccountId = Long.parseLong(fields[DEFAULT_MA_ID_FIELD]);

		Map<Long, String> permissions = new HashMap<>();
		permissions.put(CX_DESIGNER_LICENSE, String.join(",",
				createPermissionJson(masterAccountId, "dashboard", "createDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "deleteDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "editDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "refreshDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "drillDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "shareEdit"),
				createPermissionJson(masterAccountId, "dashboard", "shareView"),

				createPermissionJson(masterAccountId, "group", "manageGroups"),
				createPermissionJson(masterAccountId, "user", "manageUsers"),

				createPermissionJson(masterAccountId, "masterAccount", "manageSettings"),
				createPermissionJson(masterAccountId, "masterAccount", "shareCreateUser"),
				createPermissionJson(masterAccountId, "masterAccount", "conductSecurityAudit"),

				createPermissionJson(masterAccountId, "contentProvider", "subscribeContentProvider"),
				createPermissionJson(masterAccountId, "contentProvider", "subscribeWidget")));

		permissions.put(CX_STUDIO_LICENSE, String.join(",",
				createPermissionJson(masterAccountId, "dashboard", "createDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "deleteDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "editDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "refreshDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "drillDashboard"),
				createPermissionJson(masterAccountId, "dashboard", "shareEdit"),
				createPermissionJson(masterAccountId, "dashboard", "shareView"),
				createPermissionJson(masterAccountId, "dashboard", "changeOwnership"),

				createPermissionJson(masterAccountId, "group", "manageGroups"),
				createPermissionJson(masterAccountId, "user", "manageUsers"),

				createPermissionJson(masterAccountId, "masterAccount", "shareCreateUser"),

				createPermissionJson(masterAccountId, "contentProvider", "subscribeContentProvider"),
				createPermissionJson(masterAccountId, "contentProvider", "subscribeWidget")));

		String licensePermissions = permissions.getOrDefault(Long.valueOf(fields[LICENSE_TYPE_ID_FIELD]), "");

		String extension = extended ? buildExtension(fields) : "";
		String jsonBase = "{"
				+ "\"firstName\":\"%s\","
				+ "\"lastName\":\"%s\","
				+ "\"userEmail\":\"%s\","
				+ "\"password\":\"%s\","
				+ "\"licenseTypeId\":%s,"
				+ "\"registered\":%s,"
				+ "\"defaultMasterAccountId\":%s,"
				+ "\"deleteFromAllMAsExceptDefault\":false,"
				+ "\"masterAccountPermissions\":[%s]"
				+ "%s"
				+ "}";

		return String.format(jsonBase,
				fields[FIRST_NAME_FIELD],
				fields[LAST_NAME_FIELD],
				fields[USER_EMAIL_FIELD],
				fields[PASSWORD_FIELD],
				fields[LICENSE_TYPE_ID_FIELD],
				String.valueOf(registered),
				fields[DEFAULT_MA_ID_FIELD],
				licensePermissions,
				StringUtils.isEmpty(extension) ? "" : "," + extension);
	}

	private String buildExtension(String[] fields) {
		String uniqueId = fields.length > UNIQUE_ID_FIELD ? fields[UNIQUE_ID_FIELD] : null;
		String customField = fields.length > CUSTOM_FIELD ? fields[CUSTOM_FIELD] : null;

		String extensionBase = (!StringUtils.isEmpty(uniqueId) ? "\"authUniqueId\": \"%s\"," : "")
				+ (!StringUtils.isEmpty(customField) ? "\"customField\": \"%s\"" : "");

		return String.format(extensionBase,
				uniqueId,
				customField);
	}

	private static String createPermissionJson(long masterAccountId, String group, String name) {
		return "{\"masterAccountId\":\"" + masterAccountId + "\","
				+ "\"group\":\"" + group + "\","
				+ "\"action\":\"" + name + "\"}";
	}

	public String getGroupsPayload(String[] fields, GroupsCache groupsCache) {
		GroupAdditionPayload payload = new GroupAdditionPayload();
		payload.setMasterAccountId(Long.parseLong(fields[DEFAULT_MA_ID_FIELD]));
		payload.setGroups(getGroupIds(fields, groupsCache));
		payload.setUsers(Collections.singletonList(fields[USER_EMAIL_FIELD]));

		return new Gson().toJson(payload);
	}

	private List<Long> getGroupIds(String[] fields, GroupsCache groupsCache) {
		if (fields.length <= GROUP_NAME_FIELD)
			return Collections.emptyList();

		long masterAccountId = Long.parseLong(fields[DEFAULT_MA_ID_FIELD]);
		String[] groupNames = fields[GROUP_NAME_FIELD].split("\\|");

		return Arrays.stream(groupNames)
				.map(groupName -> {
					Long groupId = groupsCache.getGroupId(masterAccountId, groupName);
					if (groupId == null)
						throw new IllegalArgumentException("Could not find ID for group " + groupName);

					return groupId;
				})
				.collect(toList());
	}

}
