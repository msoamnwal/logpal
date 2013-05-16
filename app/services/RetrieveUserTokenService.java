package services;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.ServiceException;

public class RetrieveUserTokenService extends CsvWriter {
	private static final String CSV_HEADER_ROW = "isSuccessful, errorMsg, issueDomain, customerId, user, UserId, clientId, scope";
	
    @Override
    protected String getCsvFilenamePrefix() {
        return "UserTokens-ByClient";
    }

	public static InputStream getUserTokenCrdForClientAsCSV(GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> domainUserTkns) throws IOException {
		RetrieveUserTokenService srv = new RetrieveUserTokenService();
		return srv.RetrieveUserTokenCrdForClientAsCSV(oauthParameters, domainUserTkns);
	}
	
	public InputStream RetrieveUserTokenCrdForClientAsCSV(GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> domainUserTkns) throws IOException {
		StringWriter writer = new StringWriter();		
		PrintWriter out = new PrintWriter(new BufferedWriter(writer));
	    out.println(CSV_HEADER_ROW);
	    List<JsonTokenInfo> newTkn = new ArrayList<JsonTokenInfo>();
		try{						
			for(JsonTokenInfo domainUserTkn : domainUserTkns){
                //get Customer-Resource-Id By Domain_name
				CustomerInfoService csc = new CustomerInfoService();
                JsonGdataInfo customerIdByDomain = csc.getCustomerInfoByDomain(oauthParameters, domainUserTkn.issueDomain);
                JsonTokenInfo info = domainUserTkn;               
                if(customerIdByDomain.isSuccessful==false){
                	info.isSuccessful = false;
                	info.errorMsg = customerIdByDomain.errorMsg;
                }else{                	
	                //Get User-resource-Id from User-name      
                	JsonGdataInfo userinfo = UserInfoService.getUserResource(oauthParameters, domainUserTkn.userName);
	                if(userinfo.isSuccessful==false){
	                	info.isSuccessful = false;
	                	info.errorMsg = customerIdByDomain.errorMsg;	                	
	                }else{	   
		                //Get all the assigned Tokens.
	                	info.isSuccessful =true;
	                	info.userId = userinfo.id;
	                	info.customerId = customerIdByDomain.id;		                //Get all the assigned Tokens.
		                try{                	
		                	info = RetrieveUserTokenCrdForClient(oauthParameters, customerIdByDomain.id, userinfo.id, domainUserTkn.clientId);
		                	info.isSuccessful= true;
		                }
		                catch(Exception e){
		                	info.isSuccessful= false;
		                	info.errorMsg = e.toString();
		                }
		                info.userName = domainUserTkn.userName;
						info.userId = userinfo.id;
						info.customerId = customerIdByDomain.id;
						info.issueDomain = domainUserTkn.issueDomain;
		                info.clientId = domainUserTkn.clientId;
					}
                }
				if(info !=null){
					newTkn.add(info);
				}
			}
	        try {
	        	writeTokenListToCsv(newTkn, out);
	        } catch (Exception e) {
	        	Logger.info("*****::Record in CSV ::"+e);
	        }			
	  	}
	  	catch(Exception e){
	  		Logger.info("*****::Error ::"+e);
	  	}
	    finally {
	        // close the stream to release the file handle and avoid a leak
	        out.close();
	    }
		return new ByteArrayInputStream(writer.toString().getBytes());
	}	

  	/**
 		//Retrieving a user's token credential issued for a specific client for a user.

		https://www.googleapis.com/apps/security/v1/customers/customer resource ID/users/user resource ID/tokens/issue domain
		--Sample
		https://www.googleapis.com/apps/security/v1/customers/C03az79cb/users/NNNN/tokens/www.example.com
  	 */    
	public JsonTokenInfo RetrieveUserTokenCrdForClient(GoogleOAuthParameters oauthParameters, String customerId, String userId, String domainName) throws IOException {
		JsonTokenInfo info = new JsonTokenInfo();
	  	try{    		
	  		String APPS_SERVICE = "apps";
		  	String ServiceURL = "https://www.googleapis.com/apps/security/v1/customers/"+customerId+"/users/"+userId+"/tokens/"+domainName;
		  	
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	URL feedUrl = new URL(ServiceURL);		    		    
		    GDataRequest entry = service1.createEntryRequest(feedUrl);
		    entry.execute();
		    
	        String body = ServiceUtility.streamToString(entry.getResponseStream());	        
	        Logger.info("*****::body ::"+body);
	        //build OBJECT from JSON response text.
	        info = SharedServices.getObject(body, JsonTokenInfo.class);
	  	}
	  	catch(ServiceException se){
	  		Logger.info("*****::ServiceException ::"+se.getResponseBody());
	  		info = SharedServices.getObject(se.getResponseBody(), JsonTokenInfo.class);
	  		info.isSuccessful = false;
	  		if(info.error!=null && info.error.message!=null){
	  			info.displayText = info.error.message;
	  		}
	  		Logger.info("*****::ServiceException 1 ::"+info.error.message);
	  	}
	  	catch(Exception e){
	  		info.isSuccessful = false;
	  		info.displayText = e.getMessage();
	  		Logger.info("*****::Error ::"+e);
	  	}      
    
	  	return info;
	}  

	private void writeTokenListToCsv(List<JsonTokenInfo> tokens, PrintWriter out) throws IOException {	    
	    try {
	        if(tokens!=null) {
	        	for(JsonTokenInfo token : tokens){
	        		writeTokenToCsv(out, token);
	        	}
	        }
	    } finally {
	        // close the stream to release the file handle and avoid a leak
	        out.close();
	    }
	}	
	private static String getValidText(Object obj){
		if(obj!=null){
			return obj.toString();
		}
		return "";
	}
	private void writeTokenToCsv(PrintWriter out, JsonTokenInfo tokenInfo) {        
        // write columns for the row representing the event as follows:
		
        // isSuccessful
	       printColumn(out, getValidText(tokenInfo.isSuccessful));
	        
	        // errorMsg
	        printColumn(out, getValidText(tokenInfo.errorMsg));

			// token_issueDomain/CustomerId
	        printColumn(out, getValidText(tokenInfo.issueDomain));        
	        printColumn(out, getValidText(tokenInfo.customerId));

	        // token_userId/UserName
	        printColumn(out, getValidText(tokenInfo.userName));
	        //Print Quoted UserId	        
	        printColumn(out, getValidText(tokenInfo.userId));        	
	        // token_ClientId/ProjectId
	        printColumn(out, getValidText(tokenInfo.clientId));

	        // token_scopes    
	        beginQuotedColumnValue(out);        
	        if(tokenInfo.scopes!=null && tokenInfo.scopes.length>0){
	            for (int i = 0; i < tokenInfo.scopes.length; i++) {	                
	                if (i > 0) {
	                    delimitFieldWithinColumn(out);
	                }
	                out.print(tokenInfo.scopes[i]);
	            }
	        }
	        endQuotedColumnValue(out);

	        // finished with row
	        out.println();
    }		
}
