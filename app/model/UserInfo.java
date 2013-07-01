package model;

import com.google.api.client.util.Key;

import play.db.jpa.Model;
import javax.persistence.Entity;

public class UserInfo extends JsonTokenInfo{
	@Key public String userId;
	@Key public String email;
	@Key public Boolean isVerified;
}
