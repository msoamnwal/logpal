package services;

import play.Play;

public interface AppConstants {

    String APP_NAME = Play.configuration.getProperty("application.name");

    String CSV_FILE_EXT = ".csv";

    long GOOGLE_APPS_CONTROL_PANEL_APPLICATION_ID = 207535951991L;

}
