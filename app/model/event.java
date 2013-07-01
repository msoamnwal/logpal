package model;

import com.google.api.client.util.Key;

public class event extends JsonTokenInfo{
	@Key public String eventType;
	@Key public String name;
	@Key public nameValuePair[] parameters;
}
