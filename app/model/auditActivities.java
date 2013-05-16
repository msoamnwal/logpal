package model;
import services.JsonGdataInfo;
import services.JsonTokenInfo;

import com.google.api.client.util.Key;

public class auditActivities extends JsonTokenInfo{
	@Key public auditActivity[] items;
	@Key public String next;
}


