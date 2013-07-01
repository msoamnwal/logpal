package model;

import play.Logger;

public enum UserStatus {
	//To check if user account is Active, AccessRequest or Blocked.
	Active("Active"),
	Deactivate("Deactivate"),
	Blocked("Blocked"),
	AccessRequest("AccessRequest"),
    None(""),
    Invalid("Invalid");
	private UserStatus(String name) {
		this.name = name;
		}
	private final String name;
	public String toString() {
		return name;
	}	
	public static UserStatus get(String status){
		Logger.info("*****.status !! :"+ status);
		UserStatus stat = UserStatus.Invalid;
		if(UserStatus.Active.toString().equalsIgnoreCase(status)){
			stat= UserStatus.Active;
		}
		if(UserStatus.Blocked.toString().equalsIgnoreCase(status)){
			stat= UserStatus.Blocked;
		}
		if(UserStatus.AccessRequest.toString().equalsIgnoreCase(status)){
			stat= UserStatus.AccessRequest;
		}
		if(UserStatus.None.toString().equalsIgnoreCase(status)){
			stat= UserStatus.None;
		}
		if(UserStatus.Invalid.toString().equalsIgnoreCase(status)){
			stat= UserStatus.Invalid;
		}
		if(UserStatus.Deactivate.toString().equalsIgnoreCase(status)){
			stat= UserStatus.Deactivate;
		}

		return stat;
	}
}
