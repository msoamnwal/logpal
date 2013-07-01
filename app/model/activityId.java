package model;


import com.google.api.client.util.Key;

public class activityId extends JsonTokenInfo{
	@Key public String time;
	@Key public String uniqQualifier;
	@Key public String applicationId;
	@Key public String customerId;
}
