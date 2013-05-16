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
import java.util.ArrayList;
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

public class RetrieveAllTokenService extends CsvWriter {	
	private static final String CSV_HEADER_ROW = "isSuccessful, errorMsg, issueDomain, customerId, user, UserId, Service Name, clientId, scope";
	
    @Override
    protected String getCsvFilenamePrefix() {
        return "UserTokens-ALL";
    }

	public static InputStream getAllUserTokenCrdAsCSV(GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> domainUserTkns) throws IOException {
		RetrieveAllTokenService srv = new RetrieveAllTokenService();
		return srv.RetrieveAllUserTokenCrdAsCSV(oauthParameters, domainUserTkns);
	}
	public InputStream RetrieveAllUserTokenCrdAsCSV(GoogleOAuthParameters oauthParameters, List<JsonTokenInfo> domainUserTkns) throws IOException {
		//File csvFile = createTempCsvFile();		 
		StringWriter writer = new StringWriter();		
		PrintWriter out = new PrintWriter(new BufferedWriter(writer));
	    out.println(CSV_HEADER_ROW);		
		try{						
			for(JsonTokenInfo domainUserTkn : domainUserTkns){
                //get Customer-Resource-Id By Domain_name
				CustomerInfoService csc = new CustomerInfoService();
                JsonGdataInfo customerIdByDomain = csc.getCustomerInfoByDomain(oauthParameters, domainUserTkn.issueDomain);                
                //Get User-resource-Id from User-name                
                JsonGdataInfo userinfo = UserInfoService.getUserResource(oauthParameters, domainUserTkn.userName);
                //Get all the assigned Tokens.
                JsonTokenInfoList info = null;
                domainUserTkn.isSuccessful = true;
                try{
                	info = RetrieveAllUserTokenCrd(oauthParameters, customerIdByDomain.id, userinfo.id);                	
                }
			  	catch(ServiceException se){
			  		domainUserTkn = SharedServices.getObject(se.getResponseBody(), JsonTokenInfo.class);
			  		domainUserTkn.isSuccessful = false;
			  		if(domainUserTkn.error!=null && domainUserTkn.error.message!=null){
			  			domainUserTkn.errorMsg = domainUserTkn.error.message;
			  		}
			  	}
			  	catch(Exception e){
			  		domainUserTkn.isSuccessful = false;
			  		domainUserTkn.errorMsg = e.getMessage();			  		
			  	}
				if(info ==null || info.items==null || info.items.length==0){
					info = new JsonTokenInfoList();
					info.items = new JsonTokenInfo[1];
					info.items[0] = domainUserTkn;
				}				
				for(JsonTokenInfo tkn: info.items){
					tkn.userName = domainUserTkn.userName;
					tkn.userId = userinfo.id;
					tkn.customerId = customerIdByDomain.id;					
					// issueDomain in clientId Memeber
					tkn.clientId = tkn.issueDomain;
					tkn.issueDomain = domainUserTkn.issueDomain;

					tkn.isSuccessful = domainUserTkn.isSuccessful;
					tkn.displayText = tkn.displayText;
					tkn.errorMsg = domainUserTkn.errorMsg;
				}
		        try {
		        	writeTokenListToCsv(info, out);
		        } catch (Exception e) {
		        	Logger.info("*****::Record in CSV ::"+e);
		        }
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
	 	//Retrieving all token credentials for a user
	 	
	GET https://www.googleapis.com/apps/security/v1/customers/customer resource ID/users/user resource ID/tokens
	
	This example requests all token credentials for the user resource ID NNNN in the account using
	C03az79cb customer resource ID.
	
	GET https://www.googleapis.com/apps/security/v1/customers/C03az79cb/users/NNNN/tokens
   */	
    public JsonTokenInfoList RetrieveAllUserTokenCrd(GoogleOAuthParameters oauthParameters, String customerId, String userId) throws Exception {		
  		String APPS_SERVICE = "apps";
	  	String ServiceURL = "https://www.googleapis.com/apps/security/v1/customers/"+customerId+"/users/"+userId+"/tokens/";
	  	
	    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
	  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
	  	URL entryUrl = new URL(ServiceURL);		    		    
	    GDataRequest entry = service1.createEntryRequest(entryUrl);
	    entry.execute();
	    
        String body = ServiceUtility.streamToString(entry.getResponseStream());
        //build OBJECT from JSON response text.
        return SharedServices.getObject(body, JsonTokenInfoList.class);
	}
	
	private void writeTokenListToCsv(JsonTokenInfoList listToken, PrintWriter out) throws IOException {
        if(listToken != null && listToken.items!=null) {
            for (JsonTokenInfo tokenInfo : listToken.items) {
            	writeTokenToCsv(out, tokenInfo);
            }	
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
        // Service-Name/ApplicationName for the issued token.
        printColumn(out, getValidText(tokenInfo.displayText));
        // token_ClientId/ApplicationId for the Service
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
