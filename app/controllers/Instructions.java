package controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jobs.MasterQueue;


import LogpalUtils.CryptoUtils;
import LogpalUtils.GaeFileSystem;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.api.client.util.Key;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import model.*;
import play.Logger;
import play.data.Upload;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import services.ActivityReportService;
import services.CsvReader;
import services.EmailClientsReportService;
import services.RetrieveAllTokenService;
import services.RetrieveUserTokenService;
import services.RetriveAuditReportingService;
import services.RevokeUserTokenService;
import services.ServiceUtility;
import services.UserInfoService;

import play.mvc.results.*;


public class Instructions extends Controller {
    // session keys for customer info
    private static final String CUSTOMER_ID_SESSION_KEY = "customerId";
    private static final String DOMAIN_SESSION_KEY = "domain";
    private static final String USER_RESOURCE_ID = "userResourceId";

    /******************************************************************************
		Start MasterQueue APIs Section
     *****************************************************************************/
    /*
     * 
     * @throws IOException
     */
    public static void startMasterQueue() throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);
        }    	
    	MasterQueue.startMasterQueue();
    	redirect(request.getBase() + "/controlPanel");
    }
    /****
     *     
     * @throws IOException
     */
    public static void stopMasterQueue() throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	MasterQueue.stopMasterQueue();
    	redirect(request.getBase() + "/controlPanel");
    }
    /***
     * 
     * @throws IOException
     */
    public static void processMasterQueue() throws IOException{    	
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	MasterQueue.processMasterQueue();
    	redirect(request.getBase() + "/controlPanel");
    }
    /***
     * 
     * @throws IOException
     */
    public static void downloadMasterQueue() throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);
        }
    	String masterFile = MasterQueueConfig.getMasterQueueFile(); 
    	//masterEt.getProperty(MasterQueueConfig.prop_FileName)+"";
		GaeFileSystem gaeFS = new GaeFileSystem();

    	// Get the object of DataInputStream		
		if(masterFile!=null){
	    	DataInputStream csvFile = new DataInputStream(new ByteArrayInputStream(gaeFS.Read(masterFile)));//
			if(csvFile!=null){
				renderBinary(csvFile, "MasterQueue.csv", true);
			}
		}
    }
    /***
     * 
     * @throws IOException
     */
    public static void resetMasterQueue() throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	String masterFile = MasterQueueConfig.createMasterQuFile();    	
    	MasterQueueConfig.setMasterDefault(masterFile);
    	redirect(request.getBase() + "/controlPanel");
    }
    /***
     * 
     * @param dailyApiLimit
     * @param BatchInterval
     * @throws IOException
     */
    public static void UpdateMasterQueueConf(String dailyApiLimit, String BatchInterval) throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }
    	//String dailyApiLimit, String BatchInterval, MasterQueueFileKey, ToAddress, FromAddress, ReplyToAddress
    	MasterQueueConfig.set(Integer.parseInt(dailyApiLimit), Integer.parseInt(BatchInterval));
    	redirect(request.getBase() + "/controlPanel");
    }    
    /******************************************************************************
      			End MasterQueue APIs Section
      				-------------
      				
      			START Token Management APIs Section      			
     *****************************************************************************/
    
    /************************************************
     * 
     * @param requestAllUserCrd
     * @param requestAllUserCrdCHK
     * @return
     * @throws IOException
     */    
    public static String Upload_getAllUserCrd(Upload requestAllUserCrd, Boolean requestAllUserCrdCHK) throws IOException {
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	if(requestAllUserCrd!=null){
    		if(requestAllUserCrdCHK!=null && requestAllUserCrdCHK==true){
    			return MigrateCsvFor_masterQueue(requestAllUserCrd, RetrieveAllTokenService.getServiceName());
    		}else{
    			return Upload_getAllUserCrdImmediate(requestAllUserCrd);
    		}
    	}
    	return "Done";
    }
    
    /***
     * 
     * @param requestAllUserCrd
     * @return
     * @throws IOException
     */
    public static String Upload_getAllUserCrdImmediate(Upload requestAllUserCrd) throws IOException {    	
    	//Security Check
    	try{
            String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.MasterQueue.toString());
            if(reDirectUrl != null){
            	redirect(reDirectUrl);        	
            }    	
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
				GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
						new CryptoUtils().decrypt(session.get("accessToken")), 
						new CryptoUtils().decrypt(session.get("accessTokenSecret")));				
				RetrieveAllTokenService srv = new RetrieveAllTokenService();				
				InputStream csvFile = srv.getAllUserTokenCrdAsCSV(oauthParameters, userIds);
				renderBinary(csvFile, srv.getFileName(), true); 	            
	        }
   		}
    	catch(Exception e){
    		Logger.info("ERROR : "+e);
    	}
    	return "Upload DONE";
    }
    
    /***
     * 
     * @param requestAllUserForClientCrd
     * @param requestAllUserForClientCrdCHK
     * @return
     * @throws IOException
     */
    public static String Upload_getAllUserCrdForClient(Upload requestAllUserForClientCrd, Boolean requestAllUserForClientCrdCHK) throws IOException{
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	if(requestAllUserForClientCrd!=null){
    		if(requestAllUserForClientCrdCHK!=null && requestAllUserForClientCrdCHK==true){
    			return MigrateCsvFor_masterQueue(requestAllUserForClientCrd, RetrieveUserTokenService.getServiceName());
    		}else{
    			return Upload_getAllUserCrdForClientImmediate(requestAllUserForClientCrd);
    		}
    	}
    	return "Done";
    }

    /***
     * 
     * @param requestAllUserForClientCrd
     * @return
     * @throws IOException
     */
    public static String Upload_getAllUserCrdForClientImmediate(Upload requestAllUserForClientCrd) throws IOException {    	
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	try{    		
	    	if(requestAllUserForClientCrd!=null){
	    		BufferedReader br= new BufferedReader(new InputStreamReader(requestAllUserForClientCrd.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);
	    		GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
	    				new CryptoUtils().decrypt(session.get("accessToken")), 
	    				new CryptoUtils().decrypt(session.get("accessTokenSecret")));
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
    		Logger.info("Error :"+e);
    	}
    	return "Upload DONE";
    }
    
    /***
     * 
     * @param revokeUserCrd
     * @param revokeUserCrdCHK
     * @return
     * @throws IOException
     */
    public static String Upload_revokeUserCrd(Upload revokeUserCrd, Boolean revokeUserCrdCHK) throws IOException{
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }    	
    	if(revokeUserCrd!=null){
    		if(revokeUserCrdCHK!=null && revokeUserCrdCHK==true){
    			return MigrateCsvFor_masterQueue(revokeUserCrd, RevokeUserTokenService.getServiceName());
    		}else{
    			return Upload_revokeUserCrdImmediate(revokeUserCrd);
    		}
    	}
    	return "Done";
    }
    
    /***
     * 
     * @param revokeUserCrd
     * @return
     */
    public static String Upload_revokeUserCrdImmediate(Upload revokeUserCrd) {
    	
    	try{
            String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
            if(reDirectUrl != null){
            	redirect(reDirectUrl);        	
            }    		
	    	if(revokeUserCrd!=null){
	    		BufferedReader br= new BufferedReader(new InputStreamReader(revokeUserCrd.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);
	    		GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
	    				new CryptoUtils().decrypt(session.get("accessToken")), 
	    				new CryptoUtils().decrypt(session.get("accessTokenSecret")));
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
				InputStream csvFile = RevokeUserTokenService.getRevokeUserTokenCrdForClientAsCSV(oauthParameters, tokens);
				renderBinary(csvFile, srv.getFileName(), true); 
	        }
   		}
    	catch(Exception e){
    		Logger.info("Error :"+e.getMessage());
    	}
    	return "Upload DONE";
    }
    /***
     * 
     * @param masterQueueUpload
     * @param ServiceName
     * @return
     * @throws IOException
     */
    public static String MigrateCsvFor_masterQueue(Upload masterQueueUpload, String ServiceName)  throws IOException{
    	return QueueTo_masterQueue(masterQueueUpload, "", "", ServiceName, true);
    }
    /***
     * 
     * @param masterQueueUpload
     * @param position
     * @param NextRecordId
     * @return
     * @throws IOException
     */
    public static String Upload_masterQueue(Upload masterQueueUpload, String position, String NextRecordId)  throws IOException{
    	return QueueTo_masterQueue(masterQueueUpload, position, NextRecordId, "", false);
    }
    /***
     * 
     * @param masterQueueUpload
     * @param position
     * @param NextRecordId
     * @param ServiceName
     * @param isMigrate
     * @return
     * @throws IOException
     */
    public static String QueueTo_masterQueue(Upload masterQueueUpload, String position, String NextRecordId, String ServiceName, Boolean isMigrate)  throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Tokens.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }
        //Logger.info("***** Upload_masterQueue IN::"+position+", "+masterQueueUpload);
    	try{    		
    		if(position==null || position==""){
    			position = "end";
    		}
	    	if(masterQueueUpload!=null){
	    		BufferedReader br= new BufferedReader(new InputStreamReader(masterQueueUpload.asStream()));
	    		CsvReader userIdReader = new CsvReader(br);	    		
	    		List<List<String>> records =  new ArrayList<List<String>>();
	    		while (userIdReader.readRecord()) {
	    			//Create key for record.
	    			String recordId = userIdReader.getRawRecord().replaceAll(",", "_").replaceAll("\\s", "");
	    			List<String> recordDelim = new ArrayList<String>();
	    			//recordDelim.add(0, e), is not implemented for lib, so little long route to create Array.
	    			if(isMigrate){
	    				//Add First row of CSV
	    				recordId = ServiceName+"_"+recordId;
	    				recordDelim.add(recordId);
	    				//Add flag to delete record after process.
	    				recordDelim.add("TRUE");
	    				//Add Service-API Name
	    				recordDelim.add(ServiceName);
	    			}else{
	    				//remove true/false/delete from key.
	    				recordId = recordId.substring(recordId.indexOf("_")+1);
	    				//Add First row of CSV
	    				recordDelim.add(recordId);
	    			}
	    			recordDelim.addAll(Arrays.asList(userIdReader.getValues()));
	    			records.add(recordDelim);
	    		}
	    		if(records.size()>0){
	    			//Logger.info("***** Insert to master List:" +records.size());
	    			MasterQueue.insertQueueList(position, records, null);
	    			//Logger.info("*****DONE Insert to master List:" +records.size());
	    		}
	    		redirect(request.getBase() + "/controlPanel");	    		
	        }
   		}
    	catch(Exception e){
    		Logger.info("Error :"+e);
    		e.printStackTrace();
    	}
    	//Logger.info("***** Upload_masterQueue Out :");
    	return "Done";
    }    
    /******************************************************************************
		End Token Management APIs Section
			--------------------
			
		START Report Management APIs Section      			
     *****************************************************************************/
    
    /***
     * 
     * @param ActivityReportType
     * @param startTime
     * @param endTime
     * @param userEmail
     * @param ActivityEventAdmin
     * @param ActivityEventAdminProp
     * @param ActivityEventDoc
     * @return
     */
    public static String RetriveAuditReporting(String ActivityReportType, String startTime, String endTime, String userEmail, String ActivityEventAdmin, String ActivityEventAdminProp, String ActivityEventDoc) {    	
    	try{    		    	
            String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Reports.toString());
            if(reDirectUrl != null){
            	redirect(reDirectUrl);        	
            }    		
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
					new CryptoUtils().decrypt(session.get("accessToken")), 
					new CryptoUtils().decrypt(session.get("accessTokenSecret")));
			RetriveAuditReportingService adtSvr = new RetriveAuditReportingService();
			String queryType = userEmail;
			if(queryType==null || "".equalsIgnoreCase(queryType) || "null".equalsIgnoreCase(queryType)){
				queryType = "all";
			}
	  		String ServiceURL = "https://www.googleapis.com/admin/reports/v1/activity/users/"+queryType+"/applications/"+ActivityReportType+"?alt=json&";
	  		
			if(startTime!=null && !"".equalsIgnoreCase(startTime)){
				ServiceURL += ("&startTime="+startTime);
			}
			if(endTime!=null && !"".equalsIgnoreCase(endTime)){
				ServiceURL += ("&endTime="+endTime);
			}
			String eventName = "";
			if("admin".equalsIgnoreCase(ActivityReportType)){
				if(ActivityEventAdmin!=null && !"".equalsIgnoreCase(ActivityEventAdmin) && ActivityEventAdminProp!=null && !"".equalsIgnoreCase(ActivityEventAdminProp)){
					eventName = ActivityEventAdminProp;
				}
			}
			if("docs".equalsIgnoreCase(ActivityReportType)){
				if(ActivityEventDoc!=null && !"".equalsIgnoreCase(ActivityEventDoc)){
					eventName = ActivityEventDoc;
				}
			}
			if(!"".equalsIgnoreCase(eventName)){
				ServiceURL += ("&eventName="+eventName);
			}			
			//Logger.info("***** "+ActivityReportType+" ServiceURL :"+ServiceURL);				  		
	  		
    		InputStream csvFile = adtSvr.RetriveAuditReportingAsCSV(oauthParameters, ServiceURL);
    		if(csvFile!=null){
    			renderBinary(csvFile, adtSvr.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("Error :"+e);
    		e.printStackTrace();
    	}        	
    	return "Upload DONE";
    }
    
    /***
     * 
     * @param month
     * @return
     */
    public static String RetriveActivityReporting(String month) {
    	try{    		    		
            String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Reports.toString());
            if(reDirectUrl != null){
            	redirect(reDirectUrl);        	
            }    		
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
					new CryptoUtils().decrypt(session.get("accessToken")), 
					new CryptoUtils().decrypt(session.get("accessTokenSecret")));
			ActivityReportService actSrv = new ActivityReportService();
   	        InputStream csvFile = actSrv.createReport(oauthParameters, month, 
   	        		new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)));
    		if(csvFile!=null){
    			renderBinary(csvFile, actSrv.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("Error :"+e);
    	}
    	return "Upload DONE";
    }
    
    /***
     * 
     * @param month
     * @return
     */
    public static String RetriveEmailClientReporting(String month) {    	
    	try{
            String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.Reports.toString());
            if(reDirectUrl != null){
            	redirect(reDirectUrl);        	
            }    		
			GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(
					new CryptoUtils().decrypt(session.get("accessToken")), 
					new CryptoUtils().decrypt(session.get("accessTokenSecret")));
			EmailClientsReportService emailSrv = new EmailClientsReportService();
			InputStream csvFile = emailSrv.createReport(oauthParameters, month, 
					new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)));
    		if(csvFile!=null){
    			renderBinary(csvFile, emailSrv.getFileName(), true);
    		}
   		}
    	catch(Exception e){
    		Logger.info("Error :"+e);
    	}
    	return "Upload DONE";
    }
    
    /******************************************************************************
		End Report Management APIs Section
				-------------

		START EmailConf APIs Section
     *****************************************************************************/
    /***
     * 
     * @param ToAddress
     * @param FromAddress
     * @param ReplyToAddress
     * @param EmailSubject
     * @param EmailBody
     * @throws IOException
     */
    public static void UpdateConfForEmail(String ToAddress, String FromAddress, String ReplyToAddress, String EmailSubject, String EmailBody) throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.EmailConf.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);
        }
    	//String dailyApiLimit, String BatchInterval, MasterQueueFileKey, ToAddress, FromAddress, ReplyToAddress
    	MasterQueueConfig.set(ToAddress, FromAddress, ReplyToAddress, EmailSubject, EmailBody);
    	redirect(request.getBase() + "/controlPanel");
    }
    /******************************************************************************
		End EmailConf Management APIs Section
				-------------

		START WhitelistedIP APIs Section
     *****************************************************************************/
    /***
     * 
     * @param WhitelistedIPs
     * @param WhitelistedDomains
     * @throws IOException
     */
    public static void UpdateWhitelistedIP(String WhitelistedIPs, String WhitelistedDomains) throws IOException{
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheckByRole(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)), UserRoles.IPAccess.toString());
        if(reDirectUrl != null){
        	redirect(reDirectUrl);
        }
    	//String dailyApiLimit, String BatchInterval, MasterQueueFileKey, ToAddress, FromAddress, ReplyToAddress
    	MasterQueueConfig.set(WhitelistedIPs, WhitelistedDomains);
    	redirect(request.getBase() + "/controlPanel");
    }
    /******************************************************************************
		End WhitelistedIP Management APIs Section
     *****************************************************************************/
    /***
     * 
     * @throws EntityNotFoundException
     * @throws IOException
     */
    public static void index() throws EntityNotFoundException, IOException {
    	//Security Check
        String reDirectUrl = ServiceUtility.secuiryCheck(session.get("accessToken"), session.get("accessTokenSecret"), new CryptoUtils().decrypt(session.get(DOMAIN_SESSION_KEY)));
        if(reDirectUrl != null){
        	redirect(reDirectUrl);        	
        }
        //Else render the  page    	
    	mainPageInfoModel mainSettings = mainPageInfoModel.get(
    			session.get("accessToken"), 
    			session.get("accessTokenSecret"));
        List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);	             
        if(matchedUsers.size()==0 || (UserStatus.Active!= matchedUsers.get(0).getStatus())){
        	//User visiting App for first time.
        	redirect(Http.Request.current().getBase() + "/requestAppAccess");
        }else{
       	 //Validate User Permission/Role allowed.	            	 
       	 if(matchedUsers.get(0).getRole()==null){
       		redirect(Http.Request.current().getBase() + "/accessPermission");
       	 }
        }
        String role = matchedUsers.get(0).getRole();
    	render(mainSettings, role);
    }
}