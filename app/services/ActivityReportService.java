package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import play.Logger;

import model.JsonTokenInfo;
import model.auditActivities;

import LogpalUtils.CryptoUtils;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

/**
 * A service to create an activity report. Uses OAuth2.
 */
public class ActivityReportService extends GenericReportingApiService {	
	
	@Override
    protected String getFirstCoumnNameForCSV() {
        return "date";
    }	
	
	@Override
    protected String getLastCoumnNameForCSV() {
        return "count_90_day_idle";
    }
	
    @Override
    protected String getReportName() {
        return "activity";
    }

    @Override
    protected String getCsvFilenamePrefix() {
        return "activity-log";
    }
    
}
