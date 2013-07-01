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

import model.auditActivities;
import model.auditActivity;
import model.nameValuePair;

import org.joda.time.DateTime;
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

/*
 Admin Audit API:
 -------------------
GET https://www.googleapis.com/apps/reporting/audit/v1/customerId/applicationId/?{parameters}
 
The customerId is the system's unique identifier for the Google Apps customer's account. 
This ID is used in all request and response operations. 
The applicationId determines which application activities are summarized in the the API reports. For this version of the API, the reports are for the control panel. The control panel's applicationId is 207535951991. The parameters are any parameters being applied to the query. 

  
Enable your Google Apps APIs
---------------------------------
Enable the Provisioning API from your Google Apps control panel to be able to make API calls to the Admin Audit API. 
To enable the API, log in to your admin account, and select the Domain settings tab. 
Select the User settings subtab, and then select the checkbox to enable the Provisioning API. Save your changes.

  
*/
public class RetriveAuditReportingService extends CsvWriter {	
    // use same header row as Google Audit Log CSV export
    private static final String CSV_HEADER_ROW = "isSuccessful,errorMsg,event_name,event_type, event_description,user,ip_address,timestamp";

    private static final int MAX_RESULTS_PER_PAGE = 100;
    private static final String CONTINUATION_TOKEN_PARAM_NAME = "continuationToken";


    @Override
    protected String getCsvFilenamePrefix() {
        return "audit-log";
    }

	//public auditActivities RetriveAuditReporting(GoogleOAuthParameters oauthParameters, String ServiceURL, String customerId, String applicationId, String reportCriteria) throws IOException {
    public auditActivities RetriveAuditReporting(GoogleOAuthParameters oauthParameters, String ServiceURL) throws IOException {
		auditActivities info = new auditActivities();		
	  	try{
	  		String APPS_SERVICE = "apps";	  		
  			//The above url is from old logpal code and will be deprecated by November 15, 2013. 
  			//ServiceURL = "https://www.googleapis.com/apps/reporting/audit/v1/"+customerId+"/"+applicationId+"?maxResults="+MAX_RESULTS_PER_PAGE+"&alt=json&"+reportCriteria;

		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));		    
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	URL enrtyUrl = new URL(ServiceURL);		  	
		    GDataRequest entry = service1.createEntryRequest(enrtyUrl);
		    entry.execute();
	        String body = ServiceUtility.streamToString(entry.getResponseStream());    
	        info = SharedServices.getObject(body, auditActivities.class);
	  	}
	  	catch(ServiceException se){
	  		se.printStackTrace();
	  		info = SharedServices.getObject(se.getResponseBody(), auditActivities.class);
	  		info.isSuccessful = false;
	  		if(info.error!=null && info.error.message!=null){
	  			info.displayText = info.error.message;
	  		}
	  	}
	  	catch(Exception e){
	  		e.printStackTrace();
	  		info.isSuccessful = false;
	  		info.displayText = e.getMessage();
	  	}      
	  	return info;
	}

	public InputStream RetriveAuditReportingAsCSV (GoogleOAuthParameters oauthParameters, String ServiceURL)  throws IOException {
		StringWriter writer = new StringWriter();		
		PrintWriter out = new PrintWriter(new BufferedWriter(writer));
	    out.println(CSV_HEADER_ROW);		
		try{						
			RetriveAuditReportingService srv = new RetriveAuditReportingService();
			ServiceURL += "&maxResults="+MAX_RESULTS_PER_PAGE;
			while(true){
				auditActivities info = srv.RetriveAuditReporting(oauthParameters, ServiceURL);
		        if(info.items!=null && info.items.length>0){
		        	//Write Content To CSV
		        	writeTokenListToCsv(info, out);
		        }
		        if(info.next!=null && !"".equalsIgnoreCase(info.next)){
		        	ServiceURL = info.next;
		        }else{
		        	break;
		        }
		  	}
	  	}
	  	catch(Exception e){
	  		Logger.info("Error :"+e);
	  	}
	    finally {
	        // close the stream to release the file handle and avoid a leak
	        out.close();
	    }
		return new ByteArrayInputStream(writer.toString().getBytes());
	}
	private void writeTokenListToCsv(auditActivities info, PrintWriter out) throws IOException {
        if(info != null && info.items!=null) {
            for (auditActivity tokenInfo : info.items) {
            	writeTokenToCsv(out, tokenInfo);
            }	
        }
	}
	
    private static String toRfc3339Time(long time) {   	
        return new com.google.api.client.util.DateTime(time, 0 /*UTC*/).toStringRfc3339();
    }
	private void writeTokenToCsv(PrintWriter out, auditActivity tokenInfo) {        
        // write columns for the row representing the event as follows:		
        //isSuccessful,errorMsg,event_name,event_type, event_description,user,ip_address,timestamp
        //items(id(time), actor(email), ipAddress, events)

        // token_Event    
        //beginQuotedColumnValue(out);        
        if(tokenInfo.events!=null && tokenInfo.events.length>0){
        	for (model.event  evt: tokenInfo.events) {
        		out.println();
                // isSuccessful
                printColumn(out, getValidText(tokenInfo.isSuccessful));
                // errorMsg
                printColumn(out, getValidText(tokenInfo.errorMsg));
        		printColumn(out, getValidText(evt.name));
        		printColumn(out, getValidText(evt.eventType));        		
        		Boolean isDelimitFieldWithinColumn = false;
        		beginQuotedColumnValue(out);
        		if(evt.parameters!=null){        			
        			for(nameValuePair nmVal: evt.parameters){
                        if (isDelimitFieldWithinColumn==true) {
                            delimitFieldWithinColumn(out);
                        }else{
                        	isDelimitFieldWithinColumn = true;
                        }
                        out.print(nmVal.name+"="+nmVal.value);
        			}
        		}
        		endQuotedColumnValue(out);
        		//Actor email/user
        		printColumn(out, getValidText(tokenInfo.actor.email));
        		// ipAddress
                printColumn(out, getValidText(tokenInfo.ipAddress));
                //Check Id tag for timestamp.
                if (tokenInfo.id != null ) {
                	if(tokenInfo.id.time!=null){
                		//Print Datetime
                		//long startTime = DateTime.now().minusMinutes(Integer.parseInt(tokenInfo.id.time)).getMillis();
                		//toRfc3339Time(startTime)
                		printColumn(out, getValidText(tokenInfo.id.time));
                	}
                }
        	}
            // finished with row
        }
    }
}
