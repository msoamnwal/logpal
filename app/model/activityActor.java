package model;

import services.JsonGdataInfo;
import services.JsonTokenInfo;

import com.google.api.client.util.Key;
public class activityActor extends JsonTokenInfo{
	@Key public String callerType;
	@Key public String email;
}
