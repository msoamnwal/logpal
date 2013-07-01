package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.Logger;
import play.Play;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.vfs.VirtualFile;


import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.InvalidEntryException;
import com.google.gdata.util.ServiceException;

/**
 * An abstract (generic) reporting service based on the Google Apps Reporting API.
 * Uses OAuth2.
 */
public abstract class GenericReportingApiService extends CsvWriter {

    private static final String THIS_MONTH_INDICATOR = "this";
    private static final String LAST_MONTH_INDICATOR = "last";

    private static final String ROOT_URL = "https://www.google.com/";
    private static final String SERVICE_PATH = "hosted/services/v1.0/reports/";
    private static final com.google.gdata.util.ContentType CONTENT_TYPE = com.google.gdata.util.ContentType.TEXT_XML;


    //private static final String REQUEST_CONTENT_TYPE = "application/atom+xml; charset=UTF-8";
    private static final String REQUEST_TEMPLATE = "app/templates/reportingApiRequest.xml";

    private static final String REQUEST_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    public String domain;
    String APPS_SERVICE = "apps";

    //This function correct the CSV format returned from API. 
    public InputStream createReport(GoogleOAuthParameters oauthParameters, String month, String Domain) throws Exception {
		StringWriter writer = new StringWriter();		
		PrintWriter out = new PrintWriter(new BufferedWriter(writer));
	    try{
	    	String  csvData = createReportAsText(oauthParameters, month, Domain);    	
	    	/*
	    	 	Need to reformat the received CSV to place </br> at end of each record/line.
	    	 
	    	 	As specified at :https://developers.google.com/google-apps/reporting/#activity_report
	    	
				Note: The data in this report has been reformatted to ensure that this document prints correctly. 
				In this sample, an indented line signifies the continuation of the previous line. 
				In the reports you receive through the API, there will not be carriage returns between indented lines and the lines above them.
				
				date,num_accounts,count_1_day_actives,count_7_day_actives,count_14_day_actives,
				        count_30_day_actives,count_30_day_idle,count_60_day_idle,count_90_day_idle
				
				20060801,46,34,38,41,44,2,1,0
				20060802,75,61,64,69,73,2,1,0
				20060803,78,65,68,73,76,2,1,1
				20060804,82,68,72,77,80,2,1,1
	    	*/
			BufferedReader br= new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvData.getBytes())));
			
			CsvReader userIdReader = new CsvReader(br);
			Boolean isHeaderCreated = false;
			while (userIdReader.readRecord()) {
				if(userIdReader.getColumnCount()>2){
					Boolean isHeaderEndFound = false;
					//Create the Header and First column of data record.
					String firstColValForRecord = "";
					//Check if, new Header and Data chunk Start token found.					
					if(userIdReader.getValues()[0].equalsIgnoreCase(getFirstCoumnNameForCSV()))
					{
						Integer numberOfColumns =0;
						//Parse the header and Data chunk.						
						String[] tokensCvs = userIdReader.getValues();
						for(Integer pointerCurrentTokenNumber =0; pointerCurrentTokenNumber<tokensCvs.length; pointerCurrentTokenNumber++){
							String csvTokenText = tokensCvs[pointerCurrentTokenNumber];
							if(!isHeaderEndFound){
								//Parse the header and first column data with header.
								if(csvTokenText.startsWith(getLastCoumnNameForCSV())){
									isHeaderEndFound = true;
									//count header column.
									//numberOfColumns ++; Masked the increment and lastheader and fist dataRow in CSV is merged. 
									//get the first Column value.
									if(csvTokenText.length()>getLastCoumnNameForCSV().length()){
										firstColValForRecord = csvTokenText.substring(getLastCoumnNameForCSV().length());
									}
									if(isHeaderCreated == false){
										isHeaderCreated = true;
										printColumn(out, getValidText(getLastCoumnNameForCSV()));
										out.println();
									}
									printColumn(out, getValidText(firstColValForRecord));
								}else{	
									//count header column.
									numberOfColumns ++;
									if(isHeaderCreated == false){
										//print header column.
										printColumn(out, getValidText(csvTokenText));
									}
								}
							}else{
								//find row termination column								
								if((pointerCurrentTokenNumber % numberOfColumns) ==0){
									if(csvTokenText.length()<(getSecoundCoumnDataLength()+1)){
										printColumn(out, getValidText(csvTokenText));
										out.println();
									}else{
										//Separate first and last merged data column.										
										printColumn(out, getValidText(csvTokenText.substring(0, csvTokenText.length()-getSecoundCoumnDataLength())));
										out.println();
										//Print first column of next row.
										printColumn(out, getValidText(csvTokenText.substring(csvTokenText.length()-getSecoundCoumnDataLength())));
									}									
								}else{
									//getSecoundCoumnNameForCSV
									//Add the data-columns
									printColumn(out, getValidText(csvTokenText));
								}
							}
						}
					}
				}
			}
	    }
	    finally {
	        // close the stream to release the file handle and avoid a leak
	        out.close();
	    }
	    return new ByteArrayInputStream(writer.toString().getBytes());    	
    }    
    
    public String createReportAsText(GoogleOAuthParameters oauthParameters, String month, String Domain) throws Exception {
        String csvData;
        this.domain = Domain;
        if (THIS_MONTH_INDICATOR.equalsIgnoreCase(month)
            || LAST_MONTH_INDICATOR.equalsIgnoreCase(month)) {
            // caller only wants one month's data (last OR this month's)
            csvData = getReportCsvData(oauthParameters, month);
        } else {
            // caller wants all available data (last AND this month's)
            String csvDataLastMonth = getReportCsvData(oauthParameters, LAST_MONTH_INDICATOR);
            String csvDataThisMonth = getReportCsvData(oauthParameters, THIS_MONTH_INDICATOR);

            // strip header row from this month's data, then append to end of last month's to get report of all data,
            // in ascending chronological order
            /*
            int newlineIndex = csvDataThisMonth.indexOf('\n');
            if ((newlineIndex == -1) || (newlineIndex == csvDataThisMonth.length() - 1)) {
                csvDataThisMonth = ""; // no data for this month yet
            } else {
                csvDataThisMonth = csvDataThisMonth.substring(newlineIndex+1);
            }*/
            csvData = csvDataLastMonth +"\n"+ csvDataThisMonth;
        }
        return csvData;
    }    

    private String getReportCsvData(GoogleOAuthParameters oauthParameters, String month) throws IOException, OAuthException, ServiceException {
    	String body = "";
    	try{
		    GoogleService service1 = new GoogleService(APPS_SERVICE, Play.configuration.getProperty("application.name"));
		  	service1.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		  	URL enrtyUrl = new URL(ROOT_URL + SERVICE_PATH + "ReportingData"); 
		  	String requestPayload = buildRequestPayload(month);		  	
		    GDataRequest entry = service1.createRequest(RequestType.BATCH, enrtyUrl, CONTENT_TYPE);
		    OutputStream rs = entry.getRequestStream();
		    rs.write(requestPayload.getBytes());
		    rs.close();
		    entry.execute();
		    body = ServiceUtility.streamToString(entry.getResponseStream());
    	}
    	catch(InvalidEntryException e){
    		Logger.info("Error occured for month:"+month+", error-Message: "+ e.getMessage());    		
    		//<?xml version="1.0" encoding="UTF-8"?>
    		//<hs:rest xmlns:hs="google:accounts:rest:protocol"><hs:status>Failure(2001)</hs:status><hs:reason>ReportNotAvailableForGivenDate(1059)</hs:reason><hs:extendedMessage>No extended message available for this error.</hs:extendedMessage><hs:result></hs:result><hs:type></hs:type></hs:rest>
    		//
    		// catch the error message : "ReportNotAvailableForGivenDate"
    	}
	    return body;
    }

    private String buildRequestPayload(String month) {
        // assume want this month's data unless month value specified is the LAST_MONTH_INDICATOR
        // (Google Reporting API returns data for one month only in one request, back to the last month)
        DateTime date = DateTime.now(); // specifying today will get this month's data
        if (LAST_MONTH_INDICATOR.equalsIgnoreCase(month)) {
            // specifying last day of previous month will get last month's data
            date = date.minusMonths(1).dayOfMonth().withMaximumValue();
        }
        // set properties for request payload
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("domain", domain);
        props.put("date", formatDateForRequest(date));
        props.put("reportName", getReportName());

        // use properties to render request payload using a template of the payload
        return renderTemplateFromPropertyMap(REQUEST_TEMPLATE, props);
    }

    protected abstract String getReportName();
    
    protected abstract String getLastCoumnNameForCSV();
    protected abstract String getFirstCoumnNameForCSV();
    protected Integer getSecoundCoumnDataLength(){
    	return "20130430".length();//Length of a date.
    }
    

    private static String formatDateForRequest(DateTime date) {
        DateTimeFormatter format = DateTimeFormat.forPattern(REQUEST_DATE_FORMAT_PATTERN);
        // use UTC since Google Reporting API does        
        //format.getZone().setDefault(DateTimeZone.UTC);
        //format.getZone().convertLocalToUTC(arg0, arg1)
        return format.withZone(DateTimeZone.UTC).print(date);
    }

    private static String renderTemplateFromPropertyMap(
        String templateVirtualPath, Map<String, Object> props) {

        VirtualFile templateFile = VirtualFile.fromRelativePath(templateVirtualPath);
        Template template = TemplateLoader.load(templateFile);
        return template.render(props);
    }

}
