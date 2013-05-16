package model;

import services.JsonGdataInfo;
import services.JsonTokenInfo;

import com.google.api.client.util.Key;

public class auditActivity extends JsonTokenInfo{
	@Key public activityId id;
	@Key public activityActor actor;
	@Key public String ownerDomain;
	@Key public String ipAddress;
	@Key public event[] events;
}
