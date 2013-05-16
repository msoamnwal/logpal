package services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * An abstract CSV Writer.
 */
public abstract class CsvWriter implements AppConstants {

    private static final String DATETIME_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss z";

    protected abstract String getCsvFilenamePrefix();

    protected File createTempCsvFile() throws IOException {    	
        return File.createTempFile(APP_NAME + '-' + getCsvFilenamePrefix() + '-', CSV_FILE_EXT);
    }
    
    public String getFileName(){
    	return APP_NAME + '-' + getCsvFilenamePrefix() + CSV_FILE_EXT;
    }
    
    protected String formatLabel(String label) {
        return label.toLowerCase();
    }

    protected void printColumn(PrintWriter out, String value) {
        out.print(value);
        delimitColumn(out);
    }

    protected void delimitColumn(PrintWriter out) {
        out.print(',');
    }

    protected void beginQuotedColumnValue(PrintWriter out) {
        out.print('"');
    }

    protected void endQuotedColumnValue(PrintWriter out) {
        out.print('"');
        delimitColumn(out);
    }

    protected void delimitFieldWithinColumn(PrintWriter out) {
        out.print(", ");
    }

    protected void printTimeColumn(PrintWriter out, long time) {
        DateTimeFormatter format = DateTimeFormat.forPattern(DATETIME_FORMAT_PATTERN);
        // use UTC to be consistent with time zone Google uses in string fields
        format.getZone().setDefault(DateTimeZone.UTC);
        printColumn(out, format.print(time));
    }

}
