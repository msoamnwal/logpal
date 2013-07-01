package model;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import play.Logger;

import LogpalUtils.GaeFileSystem;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gdata.data.dublincore.Date;

public class MasterQueueConfig {
	public static String prop_dailyApiLimit = "dailyApiLimit";
	public static String prop_BatchInterval = "BatchInterval";
	public static String prop_FileName = "MasterQueueFileKey";

	public static String prop_ToAddress = "ToAddress";
	public static String prop_FromAddress = "FromAddress";
	public static String prop_ReplyToAddress = "ReplyToAddress";
	public static String prop_EmailSubject = "EmailSubject";
	public static String prop_EmailBody = "EmailBody";
	public static String prop_LastQueueProcessedTime ="LastQueueProcessedTime";
	public static String prop_WhitelistedIPs = "WhitelistedIPs";
	public static String prop_WhitelistedDomains = "WhitelistedDomains";//salesforce.com
	
	public static Entity set(Integer dailyApiLimit, Integer BatchInterval, String ToAddress, 
			String FromAddress, String ReplyToAddress, String EmailSubject, String EmailBody){
		Entity QueueItem = MasterQueueConfig.getEntity();
		if(QueueItem==null){
			QueueItem = new Entity("MasterQueueConfig", 1);
		}
		QueueItem.setProperty(prop_dailyApiLimit, dailyApiLimit);
		QueueItem.setProperty(prop_BatchInterval, BatchInterval);		
		
		QueueItem.setProperty(prop_ToAddress, ToAddress);
		QueueItem.setProperty(prop_FromAddress, FromAddress);
		QueueItem.setProperty(prop_ReplyToAddress, ReplyToAddress);
		QueueItem.setProperty(prop_EmailSubject, EmailSubject);
		QueueItem.setProperty(prop_EmailBody, EmailBody);
		
		String MasterQueueFileKey = "";
		if(QueueItem.getProperty(prop_FileName)!=null){
			MasterQueueFileKey = QueueItem.getProperty(prop_FileName)+"";
		}
		if("".equalsIgnoreCase(MasterQueueFileKey)){
			MasterQueueFileKey = createMasterQuFile();
			//Logger.info("!!!!!! Creating getMasterDefault KEY :"+MasterQueueFileKey);
		}
    	
    	QueueItem.setProperty(prop_FileName, MasterQueueFileKey);		
    	List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(QueueItem));
		return MasterQueueConfig.getEntity();
	}
	public static Entity set(String WhitelistedIPs, String WhitelistedDomains){
		Entity QueueItem = MasterQueueConfig.getEntity();
		if(QueueItem==null){
			QueueItem = new Entity("MasterQueueConfig", 1);
		}
		QueueItem.setProperty(prop_WhitelistedIPs, WhitelistedIPs);
		QueueItem.setProperty(prop_WhitelistedDomains, WhitelistedDomains);
		
		String MasterQueueFileKey = "";
		if(QueueItem.getProperty(prop_FileName)!=null){
			MasterQueueFileKey = QueueItem.getProperty(prop_FileName)+"";
		}
		if("".equalsIgnoreCase(MasterQueueFileKey)){
			MasterQueueFileKey = createMasterQuFile();
			//Logger.info("!!!!!! SET WhitelistedIPs KEY :"+WhitelistedIPs);
			QueueItem.setProperty(prop_FileName, MasterQueueFileKey);
		}
    			
    	List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(QueueItem));
    	//Logger.info("!!!!!! GET WhitelistedIPs KEY :"+MasterQueueConfig.getEntity().getProperty(prop_WhitelistedIPs));
		return MasterQueueConfig.getEntity();		
	}
	public static Entity set(Integer dailyApiLimit, Integer BatchInterval){
		Entity QueueItem = MasterQueueConfig.getEntity();
		if(QueueItem==null){
			QueueItem = new Entity("MasterQueueConfig", 1);
		}
		QueueItem.setProperty(prop_dailyApiLimit, dailyApiLimit);
		QueueItem.setProperty(prop_BatchInterval, BatchInterval);		
		
		String MasterQueueFileKey = "";
		if(QueueItem.getProperty(prop_FileName)!=null){
			MasterQueueFileKey = QueueItem.getProperty(prop_FileName)+"";
		}
		if("".equalsIgnoreCase(MasterQueueFileKey)){
			MasterQueueFileKey = createMasterQuFile();
			//Logger.info("!!!!!! Creating getMasterDefault KEY :"+MasterQueueFileKey);
			QueueItem.setProperty(prop_FileName, MasterQueueFileKey);
		}
    			
    	List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(QueueItem));
		return MasterQueueConfig.getEntity();
	}

	public static Entity set(String ToAddress, String FromAddress, String ReplyToAddress, String EmailSubject, String EmailBody){
		Entity QueueItem = MasterQueueConfig.getEntity();
		if(QueueItem==null){
			QueueItem = new Entity("MasterQueueConfig", 1);
		}
		QueueItem.setProperty(prop_ToAddress, ToAddress);
		QueueItem.setProperty(prop_FromAddress, FromAddress);
		QueueItem.setProperty(prop_ReplyToAddress, ReplyToAddress);
		QueueItem.setProperty(prop_EmailSubject, EmailSubject);
		QueueItem.setProperty(prop_EmailBody, EmailBody);
		
		String MasterQueueFileKey = "";
		if(QueueItem.getProperty(prop_FileName)!=null){
			MasterQueueFileKey = QueueItem.getProperty(prop_FileName)+"";
		}
		if("".equalsIgnoreCase(MasterQueueFileKey)){
			MasterQueueFileKey = createMasterQuFile();
			//Logger.info("!!!!!! Creating getMasterDefault KEY :"+MasterQueueFileKey);
			QueueItem.setProperty(prop_FileName, MasterQueueFileKey);
		}
    	List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(QueueItem));
		return MasterQueueConfig.getEntity();
	}
	
	
	//Method to get Key for Entity
	public static Key getKey(){
		return  KeyFactory.createKey("MasterQueueConfig", 1);		
	}
	//Method to retrive Entity
	public static  Entity getEntity() {		
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();		
		try {
			return DataStoreSvc.get(getKey());
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return null;
	}
	//Method to add/update Entity
	public static  List<Key> putEntity(List<Entity> entLst){
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
		return DataStoreSvc.put(entLst);
	}
	//Method to delete Entity
	public static  void delEntity(List<Key> entLst){
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
		DataStoreSvc.delete(entLst);		
	}
	public static String createMasterQuFile(){
		try {
			return GaeFileSystem.Write("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}
	public static Entity getMasterDefault(){		
	    Entity masterEty = MasterQueueConfig.getEntity();		
	    if(masterEty==null){
			masterEty = MasterQueueConfig.set(1990, 10, "", "", "", "", "");
	    }
	    return masterEty;
	}
	public static String getMasterQueueFile(){
		Entity masterEty = getMasterDefault();
		//Logger.info("getMasterQueueFile key :"+masterEty.getKey().toString());
		return masterEty.getProperty(prop_FileName)+"";
	}
	public static Entity setMasterDefault(String MasterQueueFileKey){		
	    Entity masterEty = getMasterDefault();
	    masterEty.setProperty(prop_FileName, MasterQueueFileKey);	    
	    List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(masterEty));
	    //Logger.info("*** seted  :"+MasterQueueFileKey+", "+keys.get(0));
	    //to-do delete old file.
	    //queue/keep all the old key and del/cleanup  on next-batch-start.
		return masterEty;
	}
	public static Entity setMasterQueueProcessedTime(java.util.Date DateTimeForSave){		
	    Entity masterEty = getMasterDefault();
		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// explicitly set timezone of input if needed
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));	    
	    masterEty.setProperty(prop_LastQueueProcessedTime, df.format(DateTimeForSave));	    
	    List<com.google.appengine.api.datastore.Key> keys = MasterQueueConfig.putEntity(Arrays.asList(masterEty));
	    //Logger.info("*** seted  :"+prop_LastQueueProcessedTime+", "+keys.get(0));
	    //to-do delete old file.
	    //queue/keep all the old key and del/cleanup  on next-batch-start.
		return masterEty;
	}
	/*Date createDateTime = new Date();
	QueueItem.setProperty("LastUpdated", createDateTime.toString());	
	*/
	

}
