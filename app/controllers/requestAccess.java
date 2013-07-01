package controllers;

import java.io.IOException;
import java.util.List;

import model.User;
import model.UserRoles;
import model.UserStatus;
import model.mainPageInfoModel;
import model.pageModel;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import services.ServiceUtility;

import LogpalUtils.CryptoUtils;

import com.google.appengine.api.datastore.EntityNotFoundException;

public class requestAccess extends Controller {
	private static final String DOMAIN_SESSION_KEY = "domain";
	public static void makeRequestAcess() throws EntityNotFoundException, IOException {
		String reDirectUrl = ServiceUtility.secuiryCheckForRequestAccess(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)));
		if(reDirectUrl != null){
			redirect(reDirectUrl);
		}
		 mainPageInfoModel mainSettings = mainPageInfoModel.get(
				 session.get("accessToken"), session.get("accessTokenSecret"));
		 //Logger.info("requestAccess :"+mainSettings.isSuperAdmin);
	     if(mainSettings.isSuperAdmin){
	     }else{
	    	 //Logger.info("requestAccess NO :"+mainSettings.isSuperAdmin);
             List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);                
             //User visiting App for first time, and making request for access.
             User.set(mainSettings.loggedInUsrInfo.data.email, mainSettings.loggedInUsrInfo.data.email, 
            		 mainSettings.loggedInUsrInfo.data.email, UserStatus.AccessRequest, UserRoles.None);            	                 	
             redirect(Http.Request.current().getBase() + "/requestAppAccess");
	     }
	    //Logger.info("requestAccess Test :");
		render();
	}
	public static void msgBlocked() throws EntityNotFoundException, IOException {
		render();
	}	
	public static void msgDeactivated() throws EntityNotFoundException, IOException {
		render();
	}	
	public static void msgInprogress() throws EntityNotFoundException, IOException {
		render();
	}	
	
	public static void index() throws EntityNotFoundException, IOException {
		String reDirectUrl = ServiceUtility.secuiryCheckForRequestAccess(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)));
		if(reDirectUrl != null){
			redirect(reDirectUrl);
		}
		 mainPageInfoModel mainSettings = mainPageInfoModel.get(
				 session.get("accessToken"), session.get("accessTokenSecret"));
	     if(mainSettings.isSuperAdmin){
	    	 //Logger.info("requestAccess index  :"+mainSettings.isSuperAdmin);
	     }else	{
	    	 //Logger.info("requestAccess NO :"+mainSettings.isSuperAdmin);
             List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);
        	 //User already visited App.
             if(matchedUsers.size()>0){
            	 if(UserStatus.Active== matchedUsers.get(0).getStatus()){                
            		 redirect(Http.Request.current().getBase() + "/controlPanel");
	             }
            	 if(UserStatus.Blocked== matchedUsers.get(0).getStatus()){
            		 redirect(Http.Request.current().getBase() + "/requestAccess/msgBlocked");            		 
	             }	            
            	 if(UserStatus.Deactivate== matchedUsers.get(0).getStatus()){
            		 redirect(Http.Request.current().getBase() + "/requestAccess/msgDeactivated");            		 
	             }
            	 if(UserStatus.AccessRequest== matchedUsers.get(0).getStatus()){
            		 redirect(Http.Request.current().getBase() + "/requestAccess/msgInprogress");            		 
	             }
             }
	     }
		render();	
	}
}