package services;

import com.google.api.client.util.Key;

public class JsonTokenInfo extends JsonGdataInfo{
	@Key public String issueDomain; //This is the domain Name
	@Key public String displayText; 
	@Key public Boolean anonymous; 
	@Key public Boolean nativeApp; 
	@Key public String userId;	//This is the user-resource-id for a user-name.
	@Key public String[] scopes; //scope for granted applications.
	//Processing Elements
	@Key public String userName;
	@Key public String customerId;//customer Id is specific to domain, gAPI can manage a primary and other domains.
	@Key public String clientId;	 //This is the Access given to a project "[ProjectId].app.googleusercontent.com", user need to click on allow access for it. 
}
