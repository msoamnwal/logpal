package model;

public enum UserRoles {
	//"Reports,Tokens,MasterQueue, EmailConf, IPAccess,UserAccess", 
	Reports("Reports"), 
	Tokens("Tokens"), 
	MasterQueue("MasterQueue"), 
	EmailConf("EmailConf"), 
	IPAccess("IPAccess"), 
	UserAccess("UserAccess"), 
	None(""),
	Invalid("Invalid");	
	
	private UserRoles(String name) {
		this.name = name;
		}
	private final String name;
	public String toString() {
		return name;
	}
	public static UserRoles get(String status){
		UserRoles rol = UserRoles.Invalid;
		if(status!=null){
			if(status.contains(UserRoles.Reports.toString())){
				rol = UserRoles.Reports;
			}
			if(status.contains(UserRoles.Tokens.toString())){
				rol = UserRoles.Tokens;
			}
			if(status.contains(UserRoles.MasterQueue.toString())){
				rol = UserRoles.MasterQueue;
			}
			if(status.contains(UserRoles.EmailConf.toString())){
				rol = UserRoles.EmailConf;
			}
			if(status.contains(UserRoles.IPAccess.toString())){
				rol = UserRoles.IPAccess;
			}
			if(status.contains(UserRoles.UserAccess.toString())){
				rol = UserRoles.UserAccess;
			}
		}
		if(UserRoles.None.toString().equalsIgnoreCase(status)){
			rol = UserRoles.None;
		}		
		return rol;
	}
}
