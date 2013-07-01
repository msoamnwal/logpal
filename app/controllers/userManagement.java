package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.User;
import model.UserRoles;
import model.UserStatus;
import model.mainPageInfoModel;
import LogpalUtils.CryptoUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gdata.data.dublincore.Date;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import services.ServiceUtility;

public class userManagement extends Controller {
    // session keys for customer info
    private static final String CUSTOMER_ID_SESSION_KEY = "customerId";
    private static final String DOMAIN_SESSION_KEY = "domain";
    private static final String USER_RESOURCE_ID = "userResourceId";
    
    public static void index(String defaultSelection, List<String>userName, List<String> isReports, List<String>isTokens, List<String>isIPAccess,
    		List<String>isMasterQueue, List<String>isUserAccess, List<String>isActivate, List<String> isBlock, List<String> isDeActivate)
    				throws EntityNotFoundException, IOException {
    	List<User>usrForUI = new ArrayList<User>();
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.UserAccess.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);
        }
       	mainPageInfoModel mainSettings = mainPageInfoModel.get(
    			session.get("accessToken"), 
    			session.get("accessTokenSecret"));
        
        String PageHeader = "";
        if(UserStatus.Invalid.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "Users With Invalid Status";
        }
        if(UserStatus.AccessRequest.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "Users For Activation Request";
        }
        if(UserStatus.Active.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "Users Activated";
        }
        if(UserStatus.Blocked.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "Users Blocked To Use Application";
        }
        if(UserStatus.Deactivate.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "Users Deactivated To Use The Application";
        }
        if(UserStatus.None.toString().equalsIgnoreCase(UserStatus.get(defaultSelection).toString())){
        	PageHeader = "View All Users Of Application";
        }
        //redirect(request.getBase() + "/userManagement?defaultSelection="+defaultSelection);
        //Save the user preferences.        
        List<Entity>usrForUpdate = new ArrayList<Entity>();
        if(userName!=null && userName.size()>0){        	
        	for(Integer nUser=0; nUser<userName.size(); nUser++){
        		//Get the new status for User.
        		String zStatus = defaultSelection;
        		if("checked".equalsIgnoreCase(isActivate.get(nUser))){        			
        			zStatus = UserStatus.Active.toString();
        		}        		
        		if("checked".equalsIgnoreCase(isBlock.get(nUser))){
        			zStatus = UserStatus.Blocked.toString();
        		}        		        		
        		if("checked".equalsIgnoreCase(isDeActivate.get(nUser))){
        			zStatus = UserStatus.Deactivate.toString();
        		}        		
        		String zPermission = "";
        		String zPermissionVal = isReports.get(nUser);
        		if("checked".equalsIgnoreCase(zPermissionVal)){
        			zPermission = "Reports";
        		}
        		zPermissionVal = isTokens.get(nUser);
        		if("checked".equalsIgnoreCase(zPermissionVal)){
        			zPermission = zPermission + "," + "Tokens";
        		}
        		zPermissionVal = isIPAccess.get(nUser);
        		if("checked".equalsIgnoreCase(zPermissionVal)){
        			zPermission = zPermission + "," + "IPAccess";
        		}
        		zPermissionVal = isUserAccess.get(nUser);
        		if("checked".equalsIgnoreCase(zPermissionVal)){
        			zPermission = zPermission + "," + "UserAccess";
        		}
        		zPermissionVal = isMasterQueue.get(nUser);
        		if("checked".equalsIgnoreCase(zPermissionVal)){
        			zPermission = zPermission + "," + "MasterQueue";
        		}        		
        		//create new user Entity obj for update. 
        		User aUser = new User(userName.get(nUser));
        		//Set Active, Inactive, blocked/ none        		
        		if(zStatus!=null){
        			aUser.setProperty(User.Status, UserStatus.get(zStatus).toString());
        		}
        		aUser.setProperty(User.Permissions, zPermission);
        		Date createDateTime = new Date();
        		aUser.setProperty("LastUpdated", createDateTime.toString());//new CryptoUtils().encrypt(
        		usrForUpdate.add(aUser.getEntity());        		
        	}
        	if(usrForUpdate.size()>0){
        		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
        		DataStoreSvc.put(usrForUpdate);
        	}
        	redirect(request.getBase() + "/userManagement?defaultSelection="+defaultSelection);
        }else{
	       	usrForUI =  User.getUsers("", UserStatus.get(defaultSelection));
	        List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);
	        if(mainSettings.isSuperAdmin){
	        	
	        }else{
		        if(matchedUsers.size()==0 || (UserStatus.Active!= matchedUsers.get(0).getStatus())){
		        	//User visiting App for first time.
		        	redirect(Http.Request.current().getBase() + "/requestAppAccess");
		        }else{
		       	 //Validate User Permission/Role allowed.	            	 
		       	 if(matchedUsers.get(0).getRole()==null){
		       		redirect(Http.Request.current().getBase() + "/accessPermission");
		       	 }
		        }
	        }
	        String role = matchedUsers.get(0).getRole();	       	
	        render(usrForUI, defaultSelection, PageHeader, mainSettings, role);
        }
    }
    
}