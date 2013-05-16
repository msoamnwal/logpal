package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.auditActivities;

import jobs.FileCleanupJob;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import services.ActivityReportService;

import services.CustomerInfo;
import services.CustomerInfoService;
import services.JsonGdataInfo;
import services.JsonTokenInfo;
import services.JsonTokenInfoList;
import services.RetrieveAllTokenService;
import services.RetrieveUserTokenService;
import services.RevokeUserTokenService;
import services.ServiceUtility;
import services.UserInfo;
import services.UserInfoService;
import services.EmailClientsReportService;
import services.SharedServices;


import com.google.gdata.client.GoogleService;
////3-LG
import com.google.gdata.client.docs.*;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.appsforyourdomain.UserService;
import com.google.gdata.client.appsforyourdomain.audit.AuditService;
import com.google.gdata.client.authn.oauth.*;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.google.gdata.data.appsforyourdomain.provisioning.UserFeed;
import com.google.gdata.util.ServiceException;

import play.mvc.results.*;
/**
 * The main controller of this reports app.
 */
public class lg3 extends Controller {

    // report types
    private static final String AUDIT_LOG_REPORT_TYPE = "auditlog";
    private static final String ACTIVITY_LOG_REPORT_TYPE = "activitylog";
    private static final String EMAIL_CLIENTS_LOG_REPORT_TYPE = "emailclientslog";

    // session keys for request parameters
    private static final String REPORT_TYPE_SESSION_KEY = "report_type";
    private static final String INTERVAL_SESSION_KEY = "interval";
    private static final String MONTH_SESSION_KEY = "month";
    private static final String ADMIN_SESSION_KEY = "admin";
    private static final String EVENT_SESSION_KEY = "event";

    // session keys for customer info
    private static final String CUSTOMER_ID_SESSION_KEY = "customerId";
    private static final String DOMAIN_SESSION_KEY = "domain";
    private static final String USER_RESOURCE_ID ="userResourceId";

    // Play config props
    private static final String GOOGLE_API_CLIENT_ID_PROP = "google.api.client.id";
    private static final String GOOGLE_API_CLIENT_SECRET_PROP = "google.api.client.secret";

    // OAuth2 constants
    //Allowed-scopes
    private static final String CUSTOMER_ID_OAUTH2_SCOPE = "https://apps-apis.google.com/a/feeds/policies/";
    private static final String ADMIN_AUDIT_API_OAUTH2_SCOPE = "https://www.googleapis.com/auth/apps/reporting/audit.readonly";
    private static final String REPORTING_API_OAUTH2_SCOPE = "https://www.google.com/hosted/services/v1.0/reports/ReportingData";
    //to get all User--Added
    private static final String USERS_API_OAUTH2_SCOPE = "https://apps-apis.google.com/a/feeds/user/";
    private static final String LO3_API_OAUTH2_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    
    private static final String OAUTH2_OFFLINE_ACCESS_TYPE = "offline";
    private static final String OAUTH2_FORCE_APPROVAL_PROMPT = "force";
    
    private static final String USERID_CUSTOMERID_OAUTH2_SECURITY = "https://www.googleapis.com/auth/apps.security";

    /**
     * The main entry point of the Reports controller which routes report
     * requests to an appropriate report service.
     *
     * @param report_type
     *          one of: "<i>auditlog</i>", "<i>activitylog</i>", or "<i>emailclientslog</i>"
     *          (defaults to "auditlog")
     * @param interval
     *          filter for <i>auditlog</i>: interval in minutes back from now
     *          (defaults to the maximum log retention which is 180 days back)
     * @param month
     *          filter for <i>auditlog</i> and <i>emailclientslog</i>: "this" or "last"
     *          (defaults to the maximum log retention which is 2 calendar months)
     * @param admin
     *          filter for <i>activitylog</i>: admin email
     *          (defaults to all)
     * @param event
     *          filter for <i>auditlog</i>: event name as documented at:
     *          <a href="https://developers.google.com/google-apps/admin-audit/event_names">
     *          https://developers.google.com/google-apps/admin-audit/event_names
     *          </a> - names can be provided in lower-case
     *          (defaults to all)
     *
     * @throws Exception
     */    
 
    public static Result index() {    	
  	  return new Ok();
    }    
    
    /*
    // report handlers    
    private static File handleAuditReport(Credential oAuth2Creds, Integer interval, String admin, String event) throws Exception {
        AuditReportService service = new AuditReportService();
        service.credential = oAuth2Creds;
        service.customerId = session.get(CUSTOMER_ID_SESSION_KEY);
        return service.createReport(interval, admin, event);
    }

    private static File handleActivityReport(Credential oAuth2Creds, String month) throws Exception {
        ActivityReportService service = new ActivityReportService();        
        service.credential = oAuth2Creds;
        service.domain = session.get(DOMAIN_SESSION_KEY);
        return service.createReport(month);
    }

    private static File handleEmailClientsReport(Credential oAuth2Creds, String month) throws Exception {
        EmailClientsReportService service = new EmailClientsReportService();
        service.credential = oAuth2Creds;
        service.domain = session.get(DOMAIN_SESSION_KEY);
        return service.createReport(month);
    }
    */
    static public void LOstep1(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {    	
        // Create an instance of GoogleOAuthParameters
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	    oauthParameters.setOAuthConsumerKey(Play.configuration.getProperty(GOOGLE_API_CLIENT_ID_PROP));
	    oauthParameters.setOAuthConsumerSecret(Play.configuration.getProperty(GOOGLE_API_CLIENT_SECRET_PROP));
	    oauthParameters.setOAuthType(OAuthParameters.OAuthType.THREE_LEGGED_OAUTH);
        // Set the scope. In general, we want to limit the scope as much as 
        // possible. For this example, we just ask for access to all feeds.
	    oauthParameters.setScope(
                CUSTOMER_ID_OAUTH2_SCOPE+' '+
                ADMIN_AUDIT_API_OAUTH2_SCOPE+' '+
                REPORTING_API_OAUTH2_SCOPE+' '+                
                USERS_API_OAUTH2_SCOPE+' '+
                LO3_API_OAUTH2_SCOPE+' '+
	    		REPORTING_API_OAUTH2_SCOPE+' '+
	    		USERID_CUSTOMERID_OAUTH2_SECURITY);

        // This sets the callback URL. This is where we want the user to be 
        // sent after they have granted us access. Sometimes, developers 
        // generate different URLs based on the environment. You should set 
        // this value to "http://localhost:8888/step2" if you are running 
        // the development server locally.
        oauthParameters
            .setOAuthCallback(request.getBase() + "/step2");
        GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(
            new OAuthHmacSha1Signer());
        try {
            // Remember that your request token is still unauthorized. We 
            // need to first get a unique token for the user to promote.
            oauthHelper.getUnauthorizedRequestToken(oauthParameters);
            // Generate the authorization URL
            String approvalPageUrl = oauthHelper
                .createUserAuthorizationUrl(oauthParameters);
            // Store the token secret in the session. We use this later after 
            // the user grants access. Note that this method isn't foolproof
            //  or even close. This assumes the user won't sign out of their 
            // browser or the sessions are swept between the time the user 
            // is redirected and the callback is invoked. 
            session.put("oauthTokenSecret", oauthParameters.getOAuthTokenSecret());
            redirect(approvalPageUrl);

          } catch (OAuthException e) {
        	  Logger.info("*****Error "+e);
              renderHtml("OAuth Error::"+e.getMessage());
              // We probably want to do something about this error
         }
    }

    static public void LOstep2(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Create an instance of GoogleOAuthParameters
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
	    oauthParameters.setOAuthConsumerKey(Play.configuration.getProperty(GOOGLE_API_CLIENT_ID_PROP));
	    oauthParameters.setOAuthConsumerSecret(Play.configuration.getProperty(GOOGLE_API_CLIENT_SECRET_PROP));

        GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(
          new OAuthHmacSha1Signer());

        // Remember the token secret that we stashed? Let's get it back
        // now. We need to add it to oauthParameters
        String oauthTokenSecret = session.get("oauthTokenSecret");
        oauthParameters.setOAuthTokenSecret(oauthTokenSecret);

        // The query string should contain the oauth token, so we can just
        // pass the query string to our helper object to correctly
        // parse and add the parameters to our instance of oauthParameters
        oauthHelper.getOAuthParametersFromCallback(request.querystring,
          oauthParameters);

        try {

            // Now that we have all the OAuth parameters we need, we can
            // generate an access token and access token secret. These
            // are the values we want to keep around, as they are 
            // valid for all API calls in the future until a user revokes
            // our access.
            String accessToken = oauthHelper.getAccessToken(oauthParameters);
            String accessTokenSecret = oauthParameters.getOAuthTokenSecret();
            String render = "<br/>accessTokenSecret : "+accessTokenSecret+"<br/>accessToken : "+accessToken;
            Logger.info(render);
            session.put("accessToken", accessToken);
            session.put("accessTokenSecret", accessTokenSecret);
            // In a real application, we want to redirect the user to a new
            // servlet that makes API calls. For the safe of clarity and simplicity,
            // we'll just reuse this servlet for making API calls.
            oauthParameters = new GoogleOAuthParameters();
    	    oauthParameters.setOAuthConsumerKey(Play.configuration.getProperty(GOOGLE_API_CLIENT_ID_PROP));
    	    oauthParameters.setOAuthConsumerSecret(Play.configuration.getProperty(GOOGLE_API_CLIENT_SECRET_PROP));

            // This is interesting: we set the OAuth token and the token secret
            // to the values extracted by oauthHelper earlier. These values are
            // already in scope in this example code, but they can be populated
            // from reading from the datastore or some other persistence mechanism.
            oauthParameters.setOAuthToken(accessToken);
            oauthParameters.setOAuthTokenSecret(accessTokenSecret);
            try{
            	
            	CustomerInfoService csc = new CustomerInfoService();
            	CustomerInfo customerInfo = csc.getCustomerInfo_current(oauthParameters);
                //get Customer-Resource-Id For Current User/Logged-in.
                session.put(CUSTOMER_ID_SESSION_KEY, customerInfo.customerId);            
                session.put(DOMAIN_SESSION_KEY, customerInfo.domain);


                redirect(request.getBase() + "/controlPanel");                
            }catch(Exception e){
            	Logger.info("*****::Error"+e);
            }
            Logger.info("*****::logged IN");
            
        } catch (OAuthException e) {
            // Something went wrong. Usually, you'll end up here if we have invalid
            // oauth tokens
        } /*catch (ServiceException e) {
            // Handle this exception
        }*/
    }    

}
