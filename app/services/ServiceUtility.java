package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.Logger;
import play.Play;
import play.mvc.Http;

import LogpalUtils.CryptoUtils;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

//Start Email Import
import com.google.appengine.api.mail.MailService.Message;

import java.util.List;

import model.MasterQueueConfig;
import model.User;
import model.UserRoles;
import model.UserStatus;
import model.mainPageInfoModel;

//End Email Import

public class ServiceUtility {
    // Play config props
    private static final String GOOGLE_API_CLIENT_ID_PROP = "google.api.client.id";
    private static final String GOOGLE_API_CLIENT_SECRET_PROP = "google.api.client.secret";

	 public static String streamToString(InputStream in) throws IOException {
	  	  StringBuilder out = new StringBuilder();	  	  
	  	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
	  	  for(String line = br.readLine(); line != null; line = br.readLine()) 
	  	    out.append(line);
	  	  br.close();
	  	  return out.toString();
	 }
	 public static String byteToString(byte[] in) throws IOException {
		 return streamToString(new ByteArrayInputStream(in));		 		 
	  }

	 public static InputStream stringToStream(String in) throws IOException {
		 return new ByteArrayInputStream(in.getBytes("UTF-8"));
		 
	  }
	 public static byte[] getInputStreamAsBytes(InputStream inputStream) throws IOException {
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    int l;
		    byte[] buf = new byte[1024];//gae can upload in small chunks
		    while ((l = inputStream.read(buf)) > 0) {
		      outputStream.write(buf, 0, l);
		    }
		    outputStream.flush();
		    byte[] op = outputStream.toByteArray();
		    outputStream.close();
		    return op;
	 }	 
	 public static GoogleOAuthParameters getOAuthParameters(String accessToken, String accessTokenSecret){
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	    oauthParameters.setOAuthConsumerKey(Play.configuration.getProperty(GOOGLE_API_CLIENT_ID_PROP));
	    oauthParameters.setOAuthConsumerSecret(Play.configuration.getProperty(GOOGLE_API_CLIENT_SECRET_PROP));

        oauthParameters.setOAuthToken(accessToken);
        oauthParameters.setOAuthTokenSecret(accessTokenSecret);

		return oauthParameters;
	 }	 

	 public static void sendEmail(String fromAddress,  List<String> ToAddresses, String subject, String attachmentName, String htmlBody, byte[] attachmentData){
		 try{
			 MailService mailService = MailServiceFactory.getMailService();
			 
		     Message message = new Message();
	         message.setSubject(subject);
	         message.setSender(fromAddress);
	         message.setTo(ToAddresses);
	         message.setTextBody(htmlBody);
	         MailService.Attachment csvAttachment =
	             new MailService.Attachment(attachmentName, attachmentData);
	         message.setAttachments(csvAttachment);
	         mailService.send(message);
		 }
		 catch(Exception e){
			 Logger.info("Error : " + e);
		 }		 
	 }
	 public static String secuiryCheckForRequestAccess(String accessToken, String accessTokenSecret, String userDomain) throws IOException{		 		 
		 mainPageInfoModel mainSettings = mainPageInfoModel.get(
				 accessToken, 
				 accessTokenSecret);
		 return secuiryCheck(accessToken, accessTokenSecret, userDomain, mainSettings);
	 }
	 public static String secuiryCheckByRole(String accessToken, String accessTokenSecret, String userDomain, String roleName) throws IOException{
		 mainPageInfoModel mainSettings = mainPageInfoModel.get(
				 accessToken, 
				 accessTokenSecret);		 
	     if(!mainSettings.isSuperAdmin){	   
	    	 String retUrl = secuiryCheck(accessToken, accessTokenSecret, userDomain, mainSettings);	    	 
	    	 if(retUrl==null){
	             List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);	             
	             if(matchedUsers.size()==0 || (UserStatus.Active!= matchedUsers.get(0).getStatus())){
	            	 //User visiting App for first time.
	            	 return Http.Request.current().getBase() + "/requestAppAccess";
	             }else{
	            	 //Validate User Permission/Role allowed.	            	 
	            	 if(matchedUsers.get(0).getRole()==null || !matchedUsers.get(0).getRole().contains(roleName)){
	            		 return Http.Request.current().getBase() + "/accessPermission";
	            	 }
	             }
	    	 }
	     }
	     return null;
	 }
	 public static String secuiryCheck(String accessToken, String accessTokenSecret, String userDomain) throws IOException{
		 mainPageInfoModel mainSettings = mainPageInfoModel.get(
				 accessToken, 
				 accessTokenSecret);
		 //Logger.info("*****mainSettings.isSuperAdmin 11 :"+ mainSettings.isSuperAdmin);
	     if(!mainSettings.isSuperAdmin){	   
	    	 String retUrl = secuiryCheck(accessToken, accessTokenSecret, userDomain, mainSettings);
	    	 //Logger.info("*****mainSettings.retUrl 11 :"+ retUrl);
	    	 if(retUrl==null){
	             List<User>matchedUsers = User.getUsers(mainSettings.loggedInUsrInfo.data.email, UserStatus.None);
	             //Logger.info("*****mainSettings.matchedUsers 11 :"+ matchedUsers);
	             if(matchedUsers.size()==0 || (UserStatus.Active!= matchedUsers.get(0).getStatus())){
	            	 //User visiting App for first time.
	            	 return Http.Request.current().getBase() + "/requestAppAccess";
	             }
	    	 }
	     }
	     return null;
	 }
	 private static String secuiryCheck(String accessToken, String accessTokenSecret, String userDomain, mainPageInfoModel mainSettings) throws IOException{
	     String whiteIps = (String)mainSettings.defaultSetting.getProperty(MasterQueueConfig.prop_WhitelistedIPs);
	     String whiteDomains = (String)mainSettings.defaultSetting.getProperty(MasterQueueConfig.prop_WhitelistedDomains);
	     if(!mainSettings.isSuperAdmin){	   
		     if(whiteIps!=null && !whiteIps.contains(Http.Request.current().remoteAddress)){
		    	 return Http.Request.current().getBase() + "/accessDeniedIP";
		     }
		     if(userDomain==null || "".equalsIgnoreCase(userDomain) ){
		    	 return Http.Request.current().getBase() + "/accessDeniedAPI";
		     }
		     if(whiteDomains!=null && !whiteDomains.contains(userDomain)){
		    	 return Http.Request.current().getBase() + "/accessDeniedDomain";
		     }
	     }
	     return null;
		 
	 }
}
