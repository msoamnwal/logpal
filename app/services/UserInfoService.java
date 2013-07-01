package services;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.Document;

import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;

import model.*;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.ServiceException;
/**
 * A service to retrieve user info such as user id . Uses OAuth2.
 */
public class UserInfoService implements AppConstants {

	public UserDataForAuthDetail getUserDetailByToken(GoogleOAuthParameters oauthParameters, String accessToken) throws IOException {
		//
	   	/**
			   * The abbreviated name of Apps for Your Domain recognized by Google.
			   * The service name is used while requesting an authentication token.
			*/
		UserDataForAuthDetail userInfo = new UserDataForAuthDetail();
		userInfo.isSuccessful = true;
    	try{    		
		  	String APPS_SERVICE = "apps";
		  	//https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+accessToken;
		  	String ServiceURL = "https://www.googleapis.com/userinfo/email?alt=json";
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());

		  	URL feedUrl = new URL(ServiceURL);
		  	// Set up authentication.
		    // Send the query request and receive the response
		    GDataRequest entry = service1.createEntryRequest(feedUrl);
		    entry.execute();
	        String body = ServiceUtility.streamToString(entry.getResponseStream());
	        //Logger.info("***** :<>: "+body);
	        userInfo = SharedServices.getObject(body, UserDataForAuthDetail.class);
	        userInfo.isSuccessful=true;
    	}
	  	catch(ServiceException se){
	  		Logger.info("ServiceException :"+se.getResponseBody());
	  		userInfo = SharedServices.getObject(se.getResponseBody(), UserDataForAuthDetail.class);
	  		userInfo.isSuccessful = false;
	  		userInfo.errorMsg = userInfo.error.message;	  		
	  	}    	
    	catch(Exception e){
    		Logger.info("Error :"+e);
    		userInfo.isSuccessful = false;
    		userInfo.errorMsg = e.getMessage();    		
    	}
    	return userInfo;
	}
		
    public JsonGdataInfo getUserResourceId(GoogleOAuthParameters oauthParameters, String userName) throws IOException {
	   	/**
		   * The abbreviated name of Apps for Your Domain recognized by Google.
		   * The service name is used while requesting an authentication token.
		   * eg: gapplog.admin1@wilikihanatest.com
		*/
		JsonGdataInfo userInfo = new JsonGdataInfo();
		userInfo.isSuccessful = true;
    	try{
		  	String APPS_SERVICE = "apps";
		  	String ServiceURL = "https://www.googleapis.com/apps/identity/v1/USER/";
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());

		  	URL feedUrl = new URL(ServiceURL+"lookup?name="+userName);
		  	// Set up authentication.
		    // Send the query request and receive the response
		    GDataRequest entry = service1.createEntryRequest(feedUrl);
		    entry.execute();
	        String body = ServiceUtility.streamToString(entry.getResponseStream());
	       // Logger.info("***** :< !! >: "+body);
	        userInfo = SharedServices.getObject(body, JsonGdataInfo.class);
	        userInfo.isSuccessful=true;
    	}
	  	catch(ServiceException se){
	  		Logger.info("ServiceException:"+se.getResponseBody());
	  		userInfo = SharedServices.getObject(se.getResponseBody(), JsonTokenInfo.class);
	  		userInfo.isSuccessful = false;
	  		userInfo.errorMsg = userInfo.error.message;	  		
	  	}    	
    	catch(Exception e){
    		Logger.info("Error :"+e);
    		userInfo.isSuccessful = false;
    		userInfo.errorMsg = e.getMessage();    		
    	}
    	return userInfo;
    }
    
    public static JsonGdataInfo getUserResource(GoogleOAuthParameters oauthParameters, String userName) throws IOException {
        //get User-Resource-Id By Domain_name
        UserInfoService usrInfoSvr = new UserInfoService();
        return usrInfoSvr.getUserResourceId(oauthParameters, userName);
    }
    /*
    public String getUserInfo_Old(GoogleOAuthParameters oauthParameters) throws IOException {       	
       	 //Get UserInfo by:
       	 //https://apps-apis.google.com/a/feeds/user/2.0/domain/userName OR Email

        	CustomerInfo info = new CustomerInfo();
        	try{    		
    		  	String APPS_SERVICE = "apps";
    		  	String ServiceURL = "https://apps-apis.google.com/a/feeds/user/2.0/";
    		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
    			Logger.info("*****::1");	    	
    		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
    		  	Logger.info("*****::3");
    		  	// URL of service endpoint.
    		  	//gapplog.admin1%40wilikihanatest.com
    		  	URL feedUrl = new URL(ServiceURL+"wilikihanatest.com/gapplog.admin1@wilikihanatest.com");
    	
    		  	// Set up authentication.
    		    //service1.setUserCredentials("Gapplog.admin1@wilikihantest.com", "Appirio1234567");
    		    Logger.info("service1.getFeed");
    		    // Send the query request and receive the response
    		    String customerId = "";
    		    GDataRequest entry = service1.createEntryRequest(feedUrl);
    		    entry.execute();
    		    Logger.info("*****::6"+entry.getResponseContentType());
    	        String body = ServiceUtility.streamToString(entry.getResponseStream());
    	        Logger.info("*****::7"+body);
    	        // remove namespaces to simplify XML parsing
    	        body = body.replaceAll("apps:property", "property");
    	        Logger.info("*****::8");
    	        Document xml = XML.getDocument(body);
    	        Logger.info("*****::9");
    	        
    	        info.customerId = XPath.selectText("//id/@value", xml);
    	        Logger.info("UserId = "+info.customerId);
    	        //info.domain = XPath.selectText("//property[@name='customerOrgUnitName']/@value", xml);
    	        
        	}
        	catch(Exception e){
        		Logger.info("*****::Error ::"+e);
        		//return info;
        	}      
      		//return info;    	
        	//return info;
        	return         			"";
        }    
	*/
}
