package services;

import java.io.File;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

/**
 * A service to create an activity report. Uses OAuth2.
 */
public class ActivityReportService extends GenericReportingApiService {

    @Override
    protected String getReportName() {
        return "activity";
    }

    @Override
    protected String getCsvFilenamePrefix() {
        return "activity-log";
    }
}
