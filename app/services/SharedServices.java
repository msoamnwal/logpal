package services;

import java.io.IOException;

import play.Logger;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Services which can be shared globally because they are thread-safe and/or stateless.
 */
public class SharedServices {

    public static final JsonFactory jsonFactory = new JacksonFactory();
    
    private SharedServices() {
        // singleton
    }

    public static <T> T getObject(String jsonSr, Class<T> entityClass)throws IOException {
    	JacksonFactory workerObject= new JacksonFactory();    	
    	return workerObject.fromString(jsonSr, entityClass);    	
    }

}
