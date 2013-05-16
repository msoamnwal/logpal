package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.w3c.dom.Document;

import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.appsforyourdomain.UserService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.appsforyourdomain.generic.GenericEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.google.gdata.util.ServiceException;

/**
 * A service to retrieve customer info such as customer id and domain. Uses OAuth2.
 */
public class CustomerInfoService implements AppConstants {	
    
    public CustomerInfo getCustomerInfo_current(GoogleOAuthParameters oauthParameters){
    	/**
		   * The abbreviated name of Apps for Your Domain recognized by Google.
		   * The service name is used while requesting an authentication token.
		   */
    	CustomerInfo info = new CustomerInfo();
    	try{    		
		  	String APPS_SERVICE = "apps";
		  	String ServiceURL = "https://apps-apis.google.com/a/feeds/customer/2.0/";
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	// Set up authentication.
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	// URL of service endpoint.
		  	URL feedUrl = new URL(ServiceURL+"customerId");
	
		    // Send the query request and receive the response
		    GDataRequest entry = service1.createEntryRequest(feedUrl);
		    entry.execute();
		    //Get the Response String as gData Entry doesn't work.
	        String body = ServiceUtility.streamToString(entry.getResponseStream());
	        
	        //Set values for   CustomerInfo from retrived XML.
	        // remove namespaces to simplify XML parsing
	        body = body.replaceAll("apps:property", "property");
	        Document xml = XML.getDocument(body);	        
	        info.customerId = XPath.selectText("//property[@name='customerId']/@value", xml);
	        info.domain = XPath.selectText("//property[@name='customerOrgUnitName']/@value", xml);
    	}
    	catch(Exception e){
    		Logger.info("*****::Error ::"+e);
    		return info;
    	}      
  		//return info;    	
    	return info;
    }
    public JsonGdataInfo getCustomerInfoByDomain(GoogleOAuthParameters oauthParameters, String domainName) throws IOException{
    	/**
		   * The abbreviated name of Apps for Your Domain recognized by Google.
		   * The service name is used while requesting an authentication token.
		   */
    	JsonGdataInfo info = new JsonGdataInfo();
    	info.isSuccessful = true;
    	try{    		
		  	String APPS_SERVICE = "apps";
		  	String ServiceURL = "https://www.googleapis.com/apps/identity/v1/CUSTOMER/";
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	// URL of service endpoint.
		  	URL feedUrl = new URL(ServiceURL+"lookup?name="+domainName);
	
		  	// Set up authentication.		    //
		    // Send the query request and receive the response
		    GDataRequest entry = service1.createEntryRequest(feedUrl);
		    entry.execute();		    
		    //Get the Response String as gData Entry doesn't work.
	        String body = ServiceUtility.streamToString(entry.getResponseStream());
	        
	        //build OBJECT from JSON response text.
	        info = SharedServices.getObject(body, JsonGdataInfo.class);
	        info.isSuccessful=true;
    	}    	
	  	catch(ServiceException se){
	  		Logger.info("*****::ServiceException ::"+se.getResponseBody());
	  		info = SharedServices.getObject(se.getResponseBody(), JsonTokenInfo.class);
	  		info.isSuccessful = false;
	  		info.errorMsg = info.error.message;	  		
	  	}    	
    	catch(Exception e){
    		Logger.info("*****::Error ::"+e);
    		info.isSuccessful = false;
    		info.errorMsg = e.getMessage();    		
    	}
      
  		//return info;    	
    	return info;
    }
   
}
