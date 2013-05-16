package model;

import services.JsonGdataInfo;
import services.JsonTokenInfo;

import com.google.api.client.util.Key;

public class event extends JsonTokenInfo{
	@Key public String eventType;
	@Key public String name;
	@Key public nameValuePair[] parameters;
}
