package controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jobs.FileCleanupJob;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import play.Logger;
import play.data.Upload;
import play.mvc.Controller;
import play.mvc.Http.Request;
import services.ActivityReportService;
import services.CsvReader;
import services.CustomerInfo;
import services.EmailClientsReportService;
import services.JsonTokenInfo;
import services.RetrieveAllTokenService;
import services.RetrieveUserTokenService;
import services.RetriveAuditReportingService;
import services.RevokeUserTokenService;
import services.ServiceUtility;
import services.UserInfo;
import services.UserInfoService;

import play.mvc.results.*;


public class Instructions extends Controller {
    // session keys for customer info
    private static final String CUSTOMER_ID_SESSION_KEY = "customerId";
    private static final String DOMAIN_SESSION_KEY = "domain";
    private static final String USER_RESOURCE_ID ="userResourceId";

    public static String Upload_getAllUserCrd(Upload requestAllUserCrd) {    	
    	Logger.info("***** Upload_getAllUserCrd IN");
    	try{    		    		
	    	if(requestAllUserCrd!=null){    		
	    		BufferedReader br= new BufferedReader(new InputStreamReader(requestAllUserCrd.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);
	    		List<JsonTokenInfo> userIds = new ArrayList<JsonTokenInfo>();
				while (userIdReader.readRecord()) {
					JsonTokenInfo usrDmn = new JsonTokenInfo();
					usrDmn.issueDomain = userIdReader.getValues()[0];
					usrDmn.userName = userIdReader.getValues()[1];
					userIds.add(usrDmn);
				}				
				GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));				
				RetrieveAllTokenService srv = new RetrieveAllTokenService();				
				InputStream csvFile = srv.getAllUserTokenCrdAsCSV(oauthParameters, userIds);
				renderBinary(csvFile, srv.getFileName(), true); 	            
	        }
   		}
    	catch(Exception e){
    		Logger.info("***** ERROR : Upload_getAllUserCrd :"+e);
    	}
    	return "Upload DONE";
    }
    
    public static String Upload_getAllUserCrdForClient(Upload requestAllUserForClientCrd) {    	
    	try{    		    		
	    	if(requestAllUserForClientCrd!=null){
	    		BufferedReader br= new BufferedReader(new InputStreamReader(requestAllUserForClientCrd.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);
	    		GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));
	    		List<JsonTokenInfo> tokens = new ArrayList<JsonTokenInfo>();
				while (userIdReader.readRecord()) {
					String domainName = userIdReader.getValues()[0];
					String userName = userIdReader.getValues()[1];
					String project_ClientId = userIdReader.getValues()[2];
					JsonTokenInfo token = new JsonTokenInfo();
					token.issueDomain = domainName;
					token.userName = userName;
					token.clientId = project_ClientId;
					tokens.add(token);
				}
				//JsonTokenInfo token = tokenServices1.RetrieveUserTokenCrdForClient(oauthParameters, session.get(CUSTOMER_ID_SESSION_KEY), userId, domainName);
				RetrieveUserTokenService srv = new RetrieveUserTokenService();				
				InputStream csvFile = srv.getUserTokenCrdForClientAsCSV(oauthParameters, tokens);
				renderBinary(csvFile, srv.getFileName(), true);
	        }
   		}
    	catch(Exception e){
    		Logger.info("***** Upload_getAllUserCrd :"+e);
    	}
    	return "Upload DONE";
    }
    
    public static String Upload_revokeUserCrd(Upload revokeUserCrd) {
    	Logger.info("***** Upload_revokeUserCrd IN"+revokeUserCrd);
    	try{    		
	    	if(revokeUserCrd!=null){
	    		BufferedReader br= new BufferedReader(new InputStreamReader(revokeUserCrd.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);
	    		GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));
	    		List<JsonTokenInfo> tokens = new ArrayList<JsonTokenInfo>();
				while (userIdReader.readRecord()) {
					String domainName = userIdReader.getValues()[0];
					String userName = userIdReader.getValues()[1];
					String project_ClientId = userIdReader.getValues()[2];
					JsonTokenInfo token = new JsonTokenInfo();
					token.issueDomain = domainName;
					token.userName = userName;
					token.clientId = project_ClientId;
					tokens.add(token);				
				}
				RevokeUserTokenService srv = new RevokeUserTokenService();				
				InputStream csvFile = srv.RevokeUserTokenStatusAsCSV(oauthParameters, tokens);
				renderBinary(csvFile, srv.getFileName(), true); 
	        }
   		}
    	catch(Exception e){
    		Logger.info("***** Upload_revokeUserCrd :"+e.getMessage());
    	}
    	return "Upload DONE";
    }
        
    public static String RetriveAuditReporting(String reportCriteria) {    	
    	try{    		    		
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));
			RetriveAuditReportingService adtSvr = new RetriveAuditReportingService();
    		InputStream csvFile = adtSvr.RetriveAuditReportingAsCSV(oauthParameters, session.get(CUSTOMER_ID_SESSION_KEY), reportCriteria);
    		if(csvFile!=null){    			
    			renderBinary(csvFile, adtSvr.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("***** RetriveAuditReporting :"+e);
    	}        	
    	return "Upload DONE";
    }
    public static String RetriveActivityReporting(String month) {
    	try{    		    		
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));
			ActivityReportService actSrv = new ActivityReportService();
   	        InputStream csvFile = actSrv.createReport(oauthParameters, month, session.get(DOMAIN_SESSION_KEY));
    		if(csvFile!=null){
    			renderBinary(csvFile, actSrv.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("***** RetriveAuditReporting :"+e);
    	}
    	return "Upload DONE";
    }
    
    public static String RetriveEmailClientReporting(String month) {
    	try{
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(session.get("accessToken"), session.get("accessTokenSecret"));
			EmailClientsReportService emailSrv = new EmailClientsReportService();
			InputStream csvFile = emailSrv.createReport(oauthParameters, month, session.get(DOMAIN_SESSION_KEY));
    		if(csvFile!=null){
    			renderBinary(csvFile, emailSrv.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("***** RetriveAuditReporting :"+e);
    	}
    	return "Upload DONE";
    }    
    public static void index() { 
        //get Customer-Resource-Id For Current User/Logged-in.
    	CustomerInfo customerInfo = new CustomerInfo();
    	customerInfo.customerId = session.get(CUSTOMER_ID_SESSION_KEY);
    	customerInfo.domain = session.get(DOMAIN_SESSION_KEY);
        //get Customer-Resource-Id By Domain_name
        //JsonGdataInfo customerIdByDomain = csc.getCustomerInfoByDomain(oauthParameters, session.get(DOMAIN_SESSION_KEY));
        Logger.info("DONE customerIdByDomain>>"+customerInfo.customerId+", "+customerInfo.domain);
        
        //get User-Resource-Id By Domain_name        
        UserInfo userinfo = new UserInfo();
        userinfo.userId = session.get(USER_RESOURCE_ID);
        Logger.info("DONE userinfo.userId>>"+userinfo.userId);
        render();
    }

}