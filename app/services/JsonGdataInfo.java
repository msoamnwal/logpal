package services;

import com.google.api.client.util.Key;

public class JsonGdataInfo{
	@Key public String kind; 
	@Key public String id; 
	@Key public String type; 
	@Key public String name;
	@Key public JsonErrorGdataInfo error;

	@Key public Boolean isSuccessful;
	@Key public String errorMsg;
}

