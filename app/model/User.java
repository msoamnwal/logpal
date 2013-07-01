package model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import play.Logger;

import LogpalUtils.CryptoUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gdata.data.dublincore.Date;

public class User{
	private static final String entity ="UserLogin"; 
	public static final String UserName = "UserName";
	public static final String UserEmail = "UserEmail";
	public static final String UserDomain = "UserDomain";
	//To check if user account is Active, AccessRequest or Blocked.
	public static final String Status = "Status";
	//User Authorized for these functionalities.
	//"Reports,Tokens,MasterQueue, EmailConf, IPAccess,UserAccess", 
	public static final String Permissions = "Permissions";
	
	private Entity etUser ;
	public User(String zEmail){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key userKey = KeyFactory.createKey(entity, zEmail);
		try {
			etUser = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			etUser = new Entity(entity, zEmail);
		}
	}
	public User(Entity etyUser ){
		etUser = etyUser;
	}
	public Entity getEntity(){
		return etUser;
	}
	public static User set(String zUserName, String zUserEmail, String zDomain, UserStatus zStatus, UserRoles zPermissions){
		User objUser = new User(zUserEmail);
		objUser.create(zUserName, zUserEmail, zDomain, zStatus, zPermissions);
		return objUser;
	}
	public static User set(Entity etUser, String zUserName, String zUserEmail, String zDomain, UserStatus zStatus, UserRoles zPermissions){
		User objUser ;
		if(etUser==null){
			objUser = set(etUser, zUserName, zUserEmail, zDomain, zStatus, zPermissions);
		}else{
			objUser = new User(etUser);
			objUser.create(zUserName, zUserEmail, zDomain, zStatus, zPermissions);
		}
		return objUser;
	}
	public void create(String zUserName, String zUserEmail, String zDomain, UserStatus zStatus, UserRoles zPermissions){
		etUser.setProperty(UserName, zUserName);
		etUser.setProperty(UserEmail, zUserEmail);
		etUser.setProperty(UserDomain, zDomain);
		etUser.setProperty(Status, zStatus.toString());
		etUser.setProperty(Permissions, zPermissions.toString());
		Date createDateTime = new Date();
		etUser.setProperty("LastUpdated", createDateTime.toString());//new CryptoUtils().encrypt(
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
		DataStoreSvc.put(etUser);				
		Logger.info("***** @@@ :"+ zUserEmail + " >> " + etUser);
	}
	public void setProperty(String field, String value){
		etUser.setProperty(field, value);
	}
	public String getUserName(){
		if(etUser==null){
			return "";
		}
		return getStringValueFromEntity(UserName);		
	}
	public String getUserEmail(){
		if(etUser==null){
			return "";
		}
		return getStringValueFromEntity(UserEmail);		
	}
	public String getUserDomain(){
		if(etUser==null){
			return "";
		}
		return getStringValueFromEntity(UserDomain);		
	}
	public UserStatus getStatus(){
		if(etUser==null){
			return UserStatus.None;
		}
		Logger.info("*****.getStatus() !! :"+ etUser.getProperty(Status));
		return UserStatus.get(getStringValueFromEntity(Status, false));		
	}
	//For UI-checkbox.
	public String getStatusUI(String status){
		if(etUser==null){
			return "unchecked";
		}
		Logger.info("*****.getStatusUI :: "+status+"   >> "+UserStatus.get(getStringValueFromEntity(Status, false)).toString());
		if( UserStatus.get(getStringValueFromEntity(Status, false)).toString().equalsIgnoreCase(status)){
			Logger.info("*****.getStatusUI :: "+"checked");
			return "checked";
		}
		return "unchecked";
	}
	public String getRoleUI(String status){
		String role = getRole();
		if(role.contains(status)){
			return "checked";
		}
		return "unchecked";
	}
	public String getRole(){
		if(etUser==null || etUser.toString().trim()==""){
			return UserStatus.None.toString();
		}
		return getStringValueFromEntity(Permissions, false);		
	}
	private String getStringValueFromEntity(String propName, boolean isDecrypt){
		Object value = etUser.getProperty(propName);
		if(value==null){
			value = "";
		}
		if(isDecrypt){
			return new CryptoUtils().decrypt(value+"");
		}else{
			return value+"";
		}
	}
	private String getStringValueFromEntity(String propName){
		return getStringValueFromEntity(propName, false);
	}
	private static void buildQuery(Query q, Query.FilterOperator Operator, String fieldName, String fieldValue, boolean isEncrypt){
		if(fieldName!=null && fieldName.trim()!="" && fieldValue!=null && fieldValue.trim()!=""){
			Logger.info("*****Size "+fieldName+" :"+ fieldValue );
			/*if(isEncrypt){
				q.addFilter(fieldName, Operator, new CryptoUtils().encrypt(fieldValue));
			}else{*/
				q.addFilter(fieldName, Operator, fieldValue);
			//}
		}
	}
	public static List<User> getUsers(String zUserEmail, UserStatus zStatus){//UserStatus zStatus, UserStatus zPermissions
		// Get the Datastore Service
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		ArrayList<User>users = new ArrayList<User>();
		// The Query interface assembles a query
		Query q = new Query(entity);
		//buildQuery(q, Query.FilterOperator.EQUAL, UserName, zUserName);
		//buildQuery(q, Query.FilterOperator.EQUAL, UserEmail, "\""+zUserEmail+"\"", false);
		buildQuery(q, Query.FilterOperator.EQUAL, UserEmail, zUserEmail, false);
		//buildQuery(q, Query.FilterOperator.EQUAL, UserDomain, zDomain);
		buildQuery(q, Query.FilterOperator.EQUAL, Status, zStatus.toString(),false);
		
		// PreparedQuery contains the methods for fetching query results
		// from the datastore
		Logger.info("*****Query :"+ q);
		PreparedQuery pq =  datastore.prepare(q);

		for(Entity result: pq.asIterable()){	
			Logger.info("*****CHECK  !! :"+result);
			users.add(new User(result));
			//String firstName = (String) result.getProperty("firstName");
						
			
		}
		Logger.info("*****Size !! :"+ users.size());
		return users;
	}
	/*
	//Method to get Key for Entity
	public static Key getKey(){
		return  KeyFactory.createKey("RecentAdminAccess", 1);		
	}
	//Method to retrive Entity
	public static  Entity getEntity() {		
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();		
		try {
			return DataStoreSvc.get(getKey());
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return null;
	}*/
}
