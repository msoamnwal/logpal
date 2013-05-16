package services;

import java.io.File;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

/**
 * A service to create an email clients report. Uses OAuth2.
 */
public class EmailClientsReportService extends GenericReportingApiService {

    @Override
    protected String getReportName() {
        return "email_clients";
    }

    @Override
    protected String getCsvFilenamePrefix() {
        return "emailclients-log";
    }
}
