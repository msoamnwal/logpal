package services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.Logger;
import play.data.Upload;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import java.util.ArrayList;
import java.util.Arrays;
import  java.util.List;

import model.JsonTokenInfo;

public class apiHandler {

	//apiQuery.get(0)-->Key
	//apiQuery.get(1)-->service
	//apiQuery.get(1)-->data
	public static String processRequest(Boolean isHeaderRequired, List<String> apiQuery, String recentAccessToken, String recentTokenSecret){
		try{
			if(apiQuery.size()<1){
				return "Invalid Service Request";
			}
			Boolean isProcessed =false;
			if(!isProcessed && "AllUserTokens".equalsIgnoreCase(apiQuery.get(2))){
				isProcessed= true ;
				if(apiQuery.size()<5){
					return "Invalid Request Data (Requires at least three data param).";
				}				
				GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(recentAccessToken, recentTokenSecret);				
	    		List<JsonTokenInfo> userIds = new ArrayList<JsonTokenInfo>();
				
				JsonTokenInfo usrDmn = new JsonTokenInfo();
				usrDmn.issueDomain = apiQuery.get(3);
				usrDmn.userName = apiQuery.get(4);
				userIds.add(usrDmn);
				//Domain Name				
				return RetrieveAllTokenService.getAllUserTokenCrdAsCSVText(isHeaderRequired, oauthParameters, userIds);
		    }			
			if(!isProcessed && "AllUserTokensBYClientId".equalsIgnoreCase(apiQuery.get(2))){
				isProcessed= true ;
				if(apiQuery.size()<6){
					return "Invalid Request Data (Requires at least three data param).";
				}				
				GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(recentAccessToken, recentTokenSecret);				
				RetrieveUserTokenService srv = new RetrieveUserTokenService();				
	    		List<JsonTokenInfo> userIds = new ArrayList<JsonTokenInfo>();
				
				JsonTokenInfo usrDmn = new JsonTokenInfo();
				usrDmn.issueDomain = apiQuery.get(3);
				usrDmn.userName = apiQuery.get(4);
				usrDmn.clientId = apiQuery.get(5);
				userIds.add(usrDmn);
				//Domain Name				
				return RetrieveUserTokenService.getUserTokenCrdForClientAsCSVText(isHeaderRequired, oauthParameters, userIds);
			}
			if(!isProcessed && "RevokeAssignedToken".equalsIgnoreCase(apiQuery.get(2))){
				isProcessed= true ;
				if(apiQuery.size()<6){
					return "Invalid Request Data (Requires at least three data param).";
				}				
				GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(recentAccessToken, recentTokenSecret);				
								
	    		List<JsonTokenInfo> userIds = new ArrayList<JsonTokenInfo>();				
				JsonTokenInfo usrDmn = new JsonTokenInfo();
				usrDmn.issueDomain = apiQuery.get(3);
				usrDmn.userName = apiQuery.get(4);
				usrDmn.clientId = apiQuery.get(5);
				userIds.add(usrDmn);
				//Domain Name				
				return RevokeUserTokenService.getRevokeUserTokenCrdForClientAsCsvText(isHeaderRequired, oauthParameters, userIds);
			}
		}
    	catch(Exception e){
    		Logger.info("Error :"+e);
    	}		
		return "";
	}
}
