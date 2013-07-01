package model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.DateTime;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gdata.data.dublincore.Date;

public class gaeQueueItem {
	//Method to get Entity instance
	public static Entity getQueueItem(String csvDataRef, String UserId, String CsvName, String DomainName ){
		Entity QueueItem = new Entity("QueueItem", getQueueItemKey(UserId, csvDataRef, DomainName ));
		QueueItem.setProperty("csvDataRef", csvDataRef);
		QueueItem.setProperty("CsvName", CsvName);
		QueueItem.setProperty("UserId", UserId);
		QueueItem.setProperty("DomainName", DomainName);		
		
		Date createDateTime = new Date();
		QueueItem.setProperty("CreateDateTime", createDateTime);
		QueueItem.setProperty("LastUpdated", createDateTime);
		QueueItem.setProperty("NoOfTimeProcessed", 0);
		QueueItem.setProperty("IsProcessed", false);		
		return QueueItem;
	}
	//Method to get Key for Entity
	public static Key getQueueItemKey(String UserId, String csvDataRef, String DomainName){
		return new KeyFactory.
				Builder("QueueItem", UserId).
				addChild("QueueItem", DomainName).
				addChild("QueueItem", csvDataRef).		
				getKey();
	}
	//Method to retrieve Entity
	public static  Map<Key, Entity> getEntity(List<Key> entLst){
		DatastoreService DataStoreSvc = DatastoreServiceFactory.getDatastoreService();
		DataStoreSvc.get(entLst);
		return DataStoreSvc.get(entLst);
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
}


