package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import play.Play;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

//Start Email Import
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
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
	 public static InputStream stringToStream(String in) throws IOException {
		 return new ByteArrayInputStream(in.getBytes("UTF-8"));
		 
	  }
	 public static GoogleOAuthParameters getOAuthParameters(String accessToken, String accessTokenSecret){
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	    oauthParameters.setOAuthConsumerKey(Play.configuration.getProperty(GOOGLE_API_CLIENT_ID_PROP));
	    oauthParameters.setOAuthConsumerSecret(Play.configuration.getProperty(GOOGLE_API_CLIENT_SECRET_PROP));

        oauthParameters.setOAuthToken(accessToken);
        oauthParameters.setOAuthTokenSecret(accessTokenSecret);

		return oauthParameters;
	 }	 

	 public static void sendEmail(String htmlBody, byte[] attachmentData){
		 try{
			 Properties props = new Properties();
		     Session session = Session.getDefaultInstance(props);		 
		     Message msg = new MimeMessage(session);
		     msg.setFrom(new InternetAddress("admin@example.com", "Example.com Admin"));
		     msg.addRecipient(Message.RecipientType.TO,
			                 new InternetAddress("user@example.com", "Mr. User"));
		     msg.setSubject("Your Example.com account has been activated");
		         
		         
	         Multipart mp = new MimeMultipart();	         
	         MimeBodyPart htmlPart = new MimeBodyPart();	         
	         htmlPart.setContent(htmlBody, "text/html");
	         mp.addBodyPart(htmlPart);
	
	         MimeBodyPart attachment = new MimeBodyPart();
	         attachment.setFileName("manual.pdf");
	         attachment.setContent(attachmentData, "application/pdf");
	         mp.addBodyPart(attachment);
	
	         msg.setContent(mp);	 
		 }
		 catch(Exception e){
		 }
	 }
}
