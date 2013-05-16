package model;
import services.JsonGdataInfo;
import services.JsonTokenInfo;

import com.google.api.client.util.Key;

public class nameValuePair extends JsonTokenInfo{
	@Key public String name;
	@Key public String value;
}
