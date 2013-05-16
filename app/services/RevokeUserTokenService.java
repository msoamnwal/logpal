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
import java.util.List;

import org.w3c.dom.Document;

import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.ServiceException;

public class RevokeUserTokenService extends CsvWriter {	
    @Override
    protected String getCsvFilenamePrefix() {
        return "RevokeToken-User";
    }
    
    private static final String CSV_HEADER_ROW = "isSuccessful, errorMsg, issueDomain, customerId, user, UserId, clientId";

	/*
	 	//Revoking a user's token credential issued for a specific client.

		//DELETE https://www.googleapis.com/apps/security/v1/customers/customer resource ID/users/user resource ID/tokens/issue domain

		This example revokes token credentials issued to www.example.com for the NNNN user, and the	account's customerId resource ID is C03az79cb.
		DELETE https://www.googleapis.com/apps/security/v1/customers/C03az79cb/users/NNNN/tokens/www.example.com
		A successful response returns a 200 HTTP status code.
	 */
	public JsonTokenInfo RevokeUserTokenCrdForClient(GoogleOAuthParameters oauthParameters, String customerId, String userId, String domainName) throws IOException {
		JsonTokenInfo info = new JsonTokenInfo();
		info.isSuccessful = true;
	  	try{    		
	  		String APPS_SERVICE = "apps";
		  	String ServiceURL = "https://www.googleapis.com/apps/security/v1/customers/"+customerId+"/users/"+userId+"/tokens/"+domainName;
		  	
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	URL feedUrl = new URL(ServiceURL);
		    GDataRequest entry = service1.createDeleteRequest(feedUrl);
		    entry.execute();		  
	  	}
	  	catch(ServiceException se){
	  		info = SharedServices.getObject(se.getResponseBody(), JsonTokenInfo.class);
	  		info.isSuccessful = false;
	  		if(info.error!=null && info.error.message!=null){
	  			info.displayText = info.error.message;
	  		}
	  	}
	  	catch(Exception e){
	  		info.isSuccessful = false;
	  		info.displayText = e.getMessage();
	  	}      
	  	return info;
	}
	public static InputStream getRevokeUserTokenCrdForClientAsCSV (
			GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> tokens) throws IOException {
		RevokeUserTokenService srv = new RevokeUserTokenService();
		return srv.RevokeUserTokenStatusAsCSV(oauthParameters, tokens);
		
	}	
	public InputStream RevokeUserTokenStatusAsCSV(GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> domainUserTkns) throws IOException {
		StringWriter writer = new StringWriter();		
		PrintWriter out = new PrintWriter(new BufferedWriter(writer));		
		try{						
			for(JsonTokenInfo domainUserTkn : domainUserTkns){
                //get Customer-Resource-Id By Domain_name
				CustomerInfoService csc = new CustomerInfoService();
                JsonGdataInfo customerIdByDomain = csc.getCustomerInfoByDomain(oauthParameters, domainUserTkn.issueDomain);
                if(customerIdByDomain.isSuccessful==false){
                	domainUserTkn.isSuccessful = false;
                	domainUserTkn.errorMsg = customerIdByDomain.errorMsg;
                }else{
	                //Get User-resource-Id from User-name                
                	JsonGdataInfo userinfo = UserInfoService.getUserResource(oauthParameters, domainUserTkn.userName);
	                if(userinfo.isSuccessful==false){
	                	domainUserTkn.isSuccessful = false;
	                	domainUserTkn.errorMsg = customerIdByDomain.errorMsg;	                	
	                }else{	                
		                //Get all the assigned Tokens.
		                domainUserTkn.isSuccessful =true;
		                domainUserTkn.userId = userinfo.id;
		                domainUserTkn.customerId = customerIdByDomain.id;		                
		                try{
		                	JsonTokenInfo requestStatus =  RevokeUserTokenCrdForClient(oauthParameters, customerIdByDomain.id, userinfo.id, domainUserTkn.clientId);
		                	if(requestStatus.isSuccessful==false){
		                		domainUserTkn.isSuccessful = false;
		                		domainUserTkn.errorMsg = requestStatus.errorMsg;
		                	}
		                }catch(Exception e){
		                	domainUserTkn.isSuccessful =false;
		                	domainUserTkn.errorMsg = e.toString();
		                }                
					}
                }
			}
			writeTokenListToCsv( domainUserTkns, out);	
		}
	  	catch(Exception e){
	  		Logger.info("*****::Error ::"+e);
	  	}		
		return new ByteArrayInputStream(writer.toString().getBytes());
	}	
	private void writeTokenListToCsv(List<JsonTokenInfo> tokens, PrintWriter out) throws IOException {		
	    out.println(CSV_HEADER_ROW);
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

        // finished with row
        out.println();
        
    }
}
