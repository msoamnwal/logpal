package model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import play.Logger;

import LogpalUtils.CryptoUtils;

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

//Class to hold the recent admin login accessKey and  secureKey.
public class RecentAdminAccess {
	public static final String accessKey = "accessKey";
	public static final String secureKey = "secureKey";
	public static Entity set(String recentAccessKey, String recentSecureKey){
		com.google.appengine.api.datastore.Entity recentAdminAccess = RecentAdminAccess.getEntity();
		if(recentAdminAccess==null){
			recentAdminAccess = new Entity("RecentAdminAccess", 1);
		}
		recentAdminAccess.setProperty(accessKey, new CryptoUtils().encrypt(recentAccessKey));
		recentAdminAccess.setProperty(secureKey, new CryptoUtils().encrypt(recentSecureKey));
		Date createDateTime = new Date();
		recentAdminAccess.setProperty("LastUpdated", new CryptoUtils().encrypt(createDateTime.toString()));
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
		DataStoreSvc.put(recentAdminAccess);
		return RecentAdminAccess.getEntity();
	}
	public static String getAccessKey(Entity recentAdminAccess){		
		if(recentAdminAccess==null){
			recentAdminAccess = RecentAdminAccess.getEntity();
		}
		return new CryptoUtils().decrypt(recentAdminAccess.getProperty(accessKey)+"");		
	}
	public static String getSecureKey(Entity recentAdminAccess){		
		if(recentAdminAccess==null){
			recentAdminAccess = RecentAdminAccess.getEntity();
		}
		return new CryptoUtils().decrypt(recentAdminAccess.getProperty(secureKey)+"");		
	}
	//Method to get Key for Entity
	public static Key getKey(){
		return  KeyFactory.createKey("RecentAdminAccess", 1);		
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
	/*
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
	*/
}
