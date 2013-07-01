package jobs;

/* @author Tom Muse
**/
//import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/*
* FileOutput
* Demonstration of FileOutputStream and PrintStream classes
*/
import java.io.*;

import org.apache.commons.lang.time.DateUtils;

import play.Logger;
import play.mvc.Controller;
import services.ServiceUtility;
import services.apiHandler;

import model.MasterQueueConfig;
import model.RecentAdminAccess;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import controllers.Instructions;

import LogpalUtils.GaeFileSystem;
public class MasterQueue {
	
	/*
	 * Main for testing
	 */
	//public static void main(String args[]){
	//	demoTheQueue();
	//}
	public static String getTotalPendingBatches(){
		if(totalPendingBatches==null){
			return "0";
		}
		return totalPendingBatches.toString();
	}
	private static Double totalPendingBatches; 
	/*
	 * Sub-Batch window function. This gets a section of the Queue starting at the head and moving down "x" items.
	 * If for some reason the system has a failure, a new batch window will be formed with the first item being the 
	 * next item on the queue.
	 */
	public static String masterFile = ""; 
	public static List<List<String>> getSubBatch(List<String> currentQueueItem, Integer windowSize, Boolean includeCurrentItem){
		List<List<String>> SubQueueList = new ArrayList<List<String>>();
		List<List<String>> QueueList = getQueueList();
		totalPendingBatches = (double) 0;
		Boolean foundCurrent = false;
		
		if(currentQueueItem.get(0).equals(QueueList.get(0).get(0)) && includeCurrentItem){
			//Logger.info("You are at the beginning of the queue. Grabbing " + windowSize + " items to process");
			foundCurrent = true;//You are at the beginning of the queue.
		}
		Integer counter = 0;
		Integer items = 0;
		for(List<String> queueItem: QueueList){
			if(foundCurrent){
				SubQueueList.add(queueItem);
				items++;
				if(items == windowSize){
					break;
				}
			}
			if(!foundCurrent && QueueList.get(counter).get(0).equals(currentQueueItem.get(0))){
				foundCurrent = true;
			}
			counter++;	
		}
		if(items>0){
			totalPendingBatches = Math.ceil((QueueList.size() - counter)/ windowSize);
		}
		return SubQueueList;
	}
	
	/*
	 * Simple reusable function to get the current Queue list. This will return a List of Lists containing String data.
	 * If you want this in string form, see listOfListStringsToString() below.
	 */
    public static List<List<String>> getQueueList(){
    	List<List<String>> QueueList = new ArrayList<List<String>>();
    	try{
    		masterFile = MasterQueueConfig.getMasterQueueFile();
    		if(masterFile!=null && !"".equalsIgnoreCase(masterFile)){
    			QueueList = parseItemsFromCsvFile(masterFile);
    		}
    	}catch (Exception e){
    		System.err.println("Error: (LINE71)" + e.getMessage());
    	}
    	return QueueList;
    }
    
    public static void utilsToCheckDupsAndDeleteRequest(List<String> newItem, List<List<String>>newQueueList, List<List<String>>QueueList){
    	if(!duplicateCheck(newItem, QueueList)){
			if(!duplicateCheck(newItem,newQueueList)){
				newQueueList.add(newItem);
			}else{
				//found a duplicate
				//Logger.info("The following record already exists or is about pending to be added to the queue: " + newItem);
			}
    	}else{
    		if("delete".equalsIgnoreCase(newItem.get(1).trim())){
    			//delete from store MasterQueue.
    			int nIndexForDelete = -1;
        		for(List<String> oldItem: QueueList){
        			if(newItem.get(0).equalsIgnoreCase(oldItem.get(0))){
        				break;
        			}
        			nIndexForDelete++;
        		}
        		if(nIndexForDelete> -1 && nIndexForDelete<QueueList.size()){
        			//delete the Item marked for delete.        			
    		        try {
    		        	QueueList.remove(nIndexForDelete);
    		        } catch (UnsupportedOperationException uoe) {
    		        	QueueList = new ArrayList<List<String>>(QueueList);
    		        	QueueList.remove(nIndexForDelete);
    		        	
    		        }
    		        //Logger.info("deleted from store MasterQueue. :"+newItem.get(1).trim()+" :: "+QueueList.size());
        		}
    		}
    	}
    }
    /*
     * This function allows for the inserting of items into the queue based on the position desired.
     * For higher permission users(Admin or Managers), they will be allowed to insert at the beginning of the queue.
     * This will happen not at the head of the master queue, but after the current batch. The batch implementation is yet
     * to be prototyped in this class.
      
      //This function also support delete operation via file upload. For record deletion in MasterQueue the record falg must have 'delete' flag.
     */
    public static void insertQueueList(String position, List<List<String>> insertables, List<List<String>> curBatch){
    	List<List<String>> QueueList = getQueueList();
		if( insertables.size()==0){
			return;
		}    	
    	if("end".equalsIgnoreCase(position)){
    		//Fixed By Ranjeet, Check for duplicate
    		List<List<String>> newQueueList = new ArrayList<List<String>>();
    		for(List<String> newItem: insertables){
    			utilsToCheckDupsAndDeleteRequest(newItem, newQueueList, QueueList);
			}
    		QueueList.addAll(newQueueList);
    		//Append the new list to the current queue
    		writeQueueItems(listOfListStringsToString(QueueList));
    	}else if("next".equalsIgnoreCase(position)){
    		Boolean eobFound = false;//eobFound is a flag to indicate when the loop has reached the current batch end.
    		Boolean alreadyProcessed = false;
    		List<List<String>> newQueueList = new ArrayList<List<String>>();
    		if(QueueList.size()==0 ){
        		for(List<String> newItem: insertables){
        			utilsToCheckDupsAndDeleteRequest(newItem, null, QueueList);        			
    			}
        		writeQueueItems(listOfListStringsToString(QueueList));
    		}else{
	    		for(List<String> item: QueueList){
	    			if(item.get(0).equals(curBatch.get(curBatch.size()-1).get(0))){
	    				eobFound = true;
	    			}
	    			if(eobFound && !alreadyProcessed){
	    				//Insert the items at middle of list. 
	    				newQueueList.add(item);
	    				for(List<String> newItem: insertables){
	    					utilsToCheckDupsAndDeleteRequest(newItem, newQueueList, QueueList);
	    				}
	    				alreadyProcessed = true;
	    			}else{
	    				//Adding remeaning existing Queue Items, after middle insert done.
	    				newQueueList.add(item);
	    			}
	    		}
	    		//if(eobFound){//Fixed by ranjeet, if eobFound then also need to append the file.
	    			writeQueueItems(listOfListStringsToString(newQueueList));
	    		//}
	    		//Insert the new list to the head of the queue
    		}
    	}
    }
    
    /*
     * This function will allow for the deletion of items from the queue that are intended to run only once.
     */
    public static void deleteQueueItems(List<List<String>> delList, Boolean delAll){
    	List<List<String>> QueueList = getQueueList();
    	List<List<String>> insertableQueueList = new ArrayList<List<String>>();
    	List<List<String>> newQueueList = new ArrayList<List<String>>();
    	String toDeleteOrNotDelete = "";
    	if(delList.size() > 0 && !delAll){
    		Map<String, Boolean> delMap = new HashMap<String, Boolean>();    		
    		Map<String, Boolean> queueMap = new HashMap<String, Boolean>();
    		for(List<String> item: QueueList){
    			Boolean delFlag = false;
    			//if(item.get(1).toLowerCase().replaceAll("\\s", "").equalsIgnoreCase("true")){
    			//Check if item set for delete
    			if(item.get(1).trim().toLowerCase().equalsIgnoreCase("true")){
    				delFlag = true;
    			}
    			//queueMap.put(item.get(1),delFlag);
    			queueMap.put(item.get(0),delFlag);
    		}
    		for(List<String> item: delList){
    			toDeleteOrNotDelete = item.get(1).toLowerCase().trim();
    			if(toDeleteOrNotDelete.equalsIgnoreCase("delete")){
    				//Logger.info(item.get(1) + " will be deleted");
    				delMap.put(item.get(0),true);
    			}else if(queueMap.get(item.get(0)) != null){//Queue item already exists don't need to add it
    				//Delete the item if marked for delete for QueueCleanup after Processing completed.
    				if(queueMap.get(item.get(0))==true){
    					delMap.put(item.get(0),true);
    				}
    			}else{
	    			if(!duplicateCheck(item,insertableQueueList)){
	    				//Logger.info(item.get(0) + " will be added to the end of the queue");
	    				insertableQueueList.add(item);
	    			}else{
	    				//found a duplicate
	    				//Logger.info("The following record already exists or is about pending to be added to the queue: " + item);
	    			}
    			}
    		}
	    	for(List<String> item: QueueList){
	    		if(delMap.get(item.get(0)) == null){//
	    			newQueueList.add(item);
	    		}else{
	    			//Logger.info(item.get(0) + " will be deleted");
	    			//The item is scheduled for complete delete. Any matching user will be removed from the Queue.
	    		}
	    	}
    	}else if(delAll){
    		//Logger.info("You have chosen to remove users from the queue entirelly");
    		/*
    		 * newQueueList will remain empty causing the entire list to get deleted.
    		 */
    	}
    	//Logger.info("**** Delete Log 4 :"+newQueueList.size());
    	writeQueueItems(listOfListStringsToString(newQueueList));
		if(insertableQueueList.size() > 0){
			//Logger.info("**** Delete Log 5"+insertableQueueList.size());
			List<List<String>> curBatch = new ArrayList<List<String>>();
			insertQueueList("end", insertableQueueList, curBatch);//insert new items into the queue
		}    	
    }
    
    /*
     * Deduplication rules. If this function returns false, the queue item does not fail the deduplicaiton rules. If it returns true, then 
     * the queue item won't be inserted into the file.
     */
    public static Boolean duplicateCheck(List<String> queueItem, List<List<String>> QueueList){
    	Boolean dupFlag = false;
    	//to-do Items will be duplicated by REf-key/Data only.
    	//The first flag True,False,Delete should not included to find the dups.
    	if(QueueList!=null && QueueList.size()>0){
	    	String currentQueueString = listOfListStringsToString(QueueList);
	    	
	    	String pendingItemString = queueItem.get(0)+",";//;listStringsToString(queueItem);
	    	dupFlag = currentQueueString.contains(pendingItemString);
    	}
    	return dupFlag;
    }
    
    /*
     * Write back to the file
     */
    public static void writeQueueItems(String Queue){
    	try{
	    	//FileOutputStream fout =  new FileOutputStream("itemlist.csv");
	    	//PrintStream myOutput = new PrintStream(fout);
	    	//myOutput.println(c);
	    	//fout.close();
    		
    		GaeFileSystem gaeFS = new GaeFileSystem();
    		//temprory solution to write file    		    		
    		masterFile = gaeFS.Write(Queue);
    		if(masterFile!=null && !"".equalsIgnoreCase(masterFile)){
	        	MasterQueueConfig.setMasterDefault(masterFile);    	
    		}
    	}catch (Exception e){
    		System.err.println("Error: " + e.getMessage());
    	}
    }
    
    /*
     * This function gets the next Queue item and returns it based on the current queue item. There needs to be a item identifier to make
     * this unique otherwise, it will be difficult to get the next item after a system crash. This function will do a look forward into the
     * queue and get return the next batch of items to be processed.
     */
    public static List<List<String>> getNextQueueBatch(List<String> currentQueueItem, Integer batchWindow){
    	List<List<String>> nextQueueBatch = new ArrayList<List<String>>();
    	List<List<String>> QueueList = getQueueList();
    	totalPendingBatches =(double) 0;
    	if(QueueList.size() > 0){
			if(currentQueueItem.size() == 0){
				nextQueueBatch = getSubBatch(QueueList.get(0), batchWindow, true);//Your at the top of the list because you don't have a current position.
			}else {
				if(!currentQueueItem.get(0).equals(QueueList.get(QueueList.size() - 1).get(0))){
					//Logger.info("<ENDING THE PREVIOUS BATCH AND GETTING THE NEXT BATCH>");
		    		nextQueueBatch = getSubBatch(currentQueueItem, batchWindow, false);//(144 = 24hr * 60min / 10min) because we want the batches to be in 10min increments.
				}else{
					//Logger.info("<REACHED AT END OF BATCH FOR DAY >");
				}
	    	}
			//Change by Ranjeet, Removed Else to delete the marked-Items, if nothing more to process
			if(nextQueueBatch.size()==0 && (currentQueueItem.size()==0 || currentQueueItem.get(0).equals(QueueList.get(QueueList.size() - 1).get(0)))){
	    		//Logger.info("<YOU ARE AT THE END OF THE QUEUE, DELETING NON-REPEATING ITEMS AND ENDING THE Today's process.>");
	    		//If you are at the end of the queue, remove any successful non-repeating items and start over
	    		
				//Change by Ranjeet, Removed Else to delete the marked-Items, if nothing more to process
	    		deleteQueueItems(QueueList, false);
	    	}
		}
    	return nextQueueBatch;
    }
    
    /*
     * This function simply gets the Queue contents in List of Lists containing String data. This is how the queue will be managed. 
     * So for adding to the queue, you would get the current queue and then write back to the system with the appended/inserted data included.
     */
    public static String getQueueContentsInString(String queueName){
    	//File IO Here...(For testing only)
    	List<List<String>> QueueList = new ArrayList<List<String>>();
    	try{
    		QueueList = parseItemsFromCsvFile(queueName);//"itemlist.csv"
    	}catch (Exception e){
    		System.err.println("Error: " + e.getMessage());
    	}    	
    	//Google Datastore Here...(Future Improvement)
    	return listOfListStringsToString(QueueList);
    }
    
    /*
     * This function allows for an easy way to take the List of Lists containing strings and convert it to a string which 
     * can be displayed to the screen for the user to view.
     */
    private static String listOfListStringsToString(List<List<String>> QueueList){    	
    	String stringQueue = "";
    	Integer counter = 0;
    	if(QueueList!=null){
	    	for(List<String> row: QueueList){
	    		Integer colCount = 1;
	    		for(String col: row){
	    			stringQueue += col;
	    			if(colCount != row.size()){
	    				stringQueue += ",";
	    			}
	    			colCount++;
	    		}
	    		counter++;
	    		if(counter != QueueList.size()){
	    			stringQueue+= "\n";
	    		}
	    		
	    	}
    	}
    	return stringQueue;
    }
    
    /*
     * This function returns the string representation of the single items.
     */
    private static String listStringsToString(List<String> QueueItem){
    	String stringQueue = "";
    	Integer counter = 0;
    	if(QueueItem!=null){
			for(String col: QueueItem){
				if(counter != QueueItem.size() - 1){
					stringQueue += col + ",";
				}else{
					stringQueue += col;
				}
				counter++;
			}
		}
    	return stringQueue;
    }
    
    /*
     * This function is built to somewhat mimic the Google Datastore. This will need to change but for testing/demo purposes
     * it is probably the best option. 20130502-TOM MUSE
     */
    private static List<List<String>> parseItemsFromCsvFile(String filename) throws IOException {
		GaeFileSystem gaeFS = new GaeFileSystem();		

    	//FileInputStream fstream = new FileInputStream(filename);
    	// Get the object of DataInputStream		
    	DataInputStream in = new DataInputStream(new ByteArrayInputStream(gaeFS.Read(filename)));// 
        List<List<String>> QueueList = new ArrayList<List<String>>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;        
        while ((line = br.readLine()) != null) {
        	line = line.replace('\t', ',');
        	if(line.length() > 5){
	        	String[] lineSplit = line.split(",");
	        	List<String> columnsInLine = new ArrayList<String>();
	        	for(String col: lineSplit){
	        		columnsInLine.add(col);
	        	}
	            QueueList.add(columnsInLine);
        	}
        }
        br.close();
        return QueueList;
        
    }
    public static Boolean stop = true;
    public static List<String> currentQueueItem;
    public static Integer dailyLimit ;
    public static Integer batchIncrement ;
    private static Date recentItemProcessStart;
    
    //Start the master queue.
    public static void startMasterQueue(){    	
    	//If processing not started, start it.
   		stop = false;
   		processMasterQueue();
    }
    //Stop the master queue.
    public static void stopMasterQueue(){
    	stop = true;
    }
    public static String getLastQueueProcessedTime(){    	
    	return zLastQueueProcessedTime;    	
    }
    public static Boolean getCurrentBatchInProgress(){    	
    	return isCurrentBatchInProgress;    	
    }
    public static String getCurrentQueueItem(){
    	return listStringsToString(currentQueueItem);
    }
    public static String getPendingItemInCurrentBatch(){
    	return totalItemInCurrentBatch.toString();
    }
    public static String getNextScheduledTime(){
		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// explicitly set timezone of input if needed
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		if(scheduledTime==null){
			return df.format(new Date());
		}else{
			return df.format(scheduledTime);
		}    	
    }
    
    public static String getRecentItemProcessStart(){
		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		// explicitly set timezone of input if needed
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		if(recentItemProcessStart==null){
			return "No item is processing.";
		}else{
			return df.format(recentItemProcessStart);
		}    	
    }
    
    private static Date scheduledTime;
    private static Boolean isCurrentBatchInProgress = false;    
    private static String zLastQueueProcessedTime = "";
    private static Integer totalItemInCurrentBatch =0;
    //set this method on corn for 1-min interval
    public static void processMasterQueue(){
    	if(stop!=false){    		
    		//Logger.info("Unable to start process, as MasterQueue process is stopped");
    		return;
    	}
    	//Set the master queue to the head    	
		try{			
	    	Entity masterEt = MasterQueueConfig.getMasterDefault();    	
	    	dailyLimit = Integer.parseInt( masterEt.getProperty(MasterQueueConfig.prop_dailyApiLimit)+"");
	    	batchIncrement = Integer.parseInt(masterEt.getProperty(MasterQueueConfig.prop_BatchInterval)+"");//In Minutes - 1 batch will run every 10min in this example.	    	
	    	masterFile = masterEt.getProperty(MasterQueueConfig.prop_FileName)+""; 
	    	//get email settings
	    	String ToAddress = masterEt.getProperty(MasterQueueConfig.prop_ToAddress)+"";
	    	String[] ToAddresses = ToAddress.split(",");
	    	String FromAddress = masterEt.getProperty(MasterQueueConfig.prop_FromAddress)+"";
	    	String EmailSubject = masterEt.getProperty(MasterQueueConfig.prop_EmailSubject)+"";
	    	String EmailBody = masterEt.getProperty(MasterQueueConfig.prop_EmailBody)+"";
	    	zLastQueueProcessedTime = masterEt.getProperty(MasterQueueConfig.prop_LastQueueProcessedTime)+"";
	    	Date LastQueueProcessedTime = new Date();
	    	Date scheduledTime = null;
	    	if(zLastQueueProcessedTime!=null && !"".equalsIgnoreCase(zLastQueueProcessedTime) && !"null".equalsIgnoreCase(zLastQueueProcessedTime)){
	    		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    		// explicitly set timezone of input if needed
	    		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
	    		LastQueueProcessedTime = df.parse(zLastQueueProcessedTime);
	    		scheduledTime = DateUtils.addMinutes(LastQueueProcessedTime, batchIncrement);
	    	}else{
	    		scheduledTime = LastQueueProcessedTime;
	    		zLastQueueProcessedTime = "";
	    	}
	    	//Logger.info("A In process 3:"+stop+" ,"+(new Date()).before(scheduledTime));
	    	if((new Date()).before(scheduledTime)){
	    		return;
	    	}
	    	//Logger.info("You are starting out at the head of the queue... Please stand by while we retrieve your first Batch Window: "+masterFile);
	    	if(currentQueueItem==null){
	    		currentQueueItem = new ArrayList<String>();
	    	}			
			
			//Logger.info("getting next batch.");
			Integer maxPossibleBatches = (24 * 60)/batchIncrement;
    		Integer batchWindow = dailyLimit/maxPossibleBatches;//Max possible User-request can be processed for a batch.
    		
    		List<List<String>> nextQueueBatch = getNextQueueBatch(currentQueueItem, batchWindow);    		
    		if(nextQueueBatch!=null && nextQueueBatch.size()>0){    			
    			//Get the Access token to make the request, before batch starts.
    			Entity adminAccess = RecentAdminAccess.getEntity();    			
    			if(adminAccess==null){
    				//Logger.info("A Valid Recent AdminAccess not found.");
    				return;
    			}
    			String csvEmailData = "";
    			Boolean isHeaderRequired =true;
    			try{
    				if(nextQueueBatch.size()>0){
    					isCurrentBatchInProgress = true;
    					recentItemProcessStart = new Date();
    					totalItemInCurrentBatch = nextQueueBatch.size();
    				}
	    			for(List<String> QueueItem: nextQueueBatch){
		    			//Process the QueueItem.			    		
			    		currentQueueItem = QueueItem;//Each time we set the current queue item equal to the one we are finishing up with. We only do this after its done with this one.
			    		if(currentQueueItem.size() > 0){
			    			// The currentQueueItem is the single record.
			    			csvEmailData += apiHandler.processRequest(isHeaderRequired, currentQueueItem, RecentAdminAccess.getAccessKey(adminAccess), RecentAdminAccess.getSecureKey(adminAccess));
			    			isHeaderRequired = false;
			    		}
			    		totalItemInCurrentBatch--;
			    		//if stop, if flagged in between process, do we need to send the email for work done? 
		    		}
		    		if(csvEmailData!=null && csvEmailData.length()>7){		    			
		    			ServiceUtility.sendEmail(FromAddress,Arrays.asList(ToAddresses), EmailSubject, "Logpal_ProcesssedMasterQueue.csv", EmailBody, csvEmailData.getBytes());
		    		}
    			}finally{
    				totalItemInCurrentBatch = 0;
    				isCurrentBatchInProgress = false;
    				MasterQueueConfig.setMasterQueueProcessedTime(new Date());    				
    			}
    		}
		}
		catch(Exception e){
			e.printStackTrace();
			//The Loop can't be broken for any error.
		}
    }

    
    /*
     * Demo function showing how the queue will iterate.
     */    
    public static void demoTheQueue() throws EntityNotFoundException{
    	//We are going to delay the loop by 1 second per item processed. This will help slow things down to allow for realtime editing of the queue.
    	
    	/*
    	 * Testing the queue string display
    	 * Initial Test Passed - 20130504 12:37PM EST
    	 */
    	//Logger.info(getQueueContentsInString("itemlist.csv"));
    	Entity masterEt = MasterQueueConfig.getEntity();    	
    	Integer dailyLimit = Integer.parseInt( masterEt.getProperty(MasterQueueConfig.prop_dailyApiLimit)+"");
    	Integer batchIncrement = Integer.parseInt(masterEt.getProperty(MasterQueueConfig.prop_BatchInterval)+"");//In Minutes - 1 batch will run every 10min in this example.
    	Integer batchWindow = dailyLimit/(24 * 60 / batchIncrement);
    	
    	//Logger.info("You are starting out at the head of the queue... Please stand by while we retrieve your first Batch Window");

    	List<String> currentQueueItem = new ArrayList<String>();
    	//Set the master queue to the head
    	List<List<String>> nextQueueBatch = getNextQueueBatch(currentQueueItem, batchWindow);
    	List<List<String>> QueueList = getQueueList();
    	Boolean stop = false;
    	Integer adminInsertAt = 15;//At indices 15 in the queue, we will perform an insert.
    	while(!stop){
	    	for(List<String> QueueItem: nextQueueBatch){
	    		//Logger.info("Processing >> " + listStringsToString(QueueItem));
	    		currentQueueItem = QueueItem;//Each time we set the current queue item equal to the one we are finishing up with. We only do this after its done with this one.
	    		if(currentQueueItem.size() > 0){
		    		if(Integer.parseInt(currentQueueItem.get(0)) == adminInsertAt){
		    			//Logger.info("Your admin has inserted records to be processed after >> " + listStringsToString(nextQueueBatch.get(nextQueueBatch.size() - 1)));
		    			try{
		    				insertQueueList("next", parseItemsFromCsvFile("itemlist2.csv"), nextQueueBatch);//nextQueueBatch.get(nextQueueBatch.size() - 1).get(0)
		    				QueueList = getQueueList();
		    				//Logger.info("\n<YOUR UPDATED QUEUE>\n" + listOfListStringsToString(QueueList) + "\nQUEUE END\n");
		    			}catch (Exception e){
		    				//Logger.info("Error: " + e.getMessage());
		    	    		//System.err.println("Error: (LINE370)" + e.getMessage());
		    	    		stop = true;//Stop at the end of the queue this prevents an endless loop.
		    	    	}
		    		}
	    		}else{
	    			//Logger.info("The Master Queue is currently empty. Please load a user CSV file and retry.");
	    		}
	    	}
	    	nextQueueBatch = getNextQueueBatch(currentQueueItem, batchWindow);
		    if(QueueList.size() > 0){
		    	if(currentQueueItem.get(0).equals(QueueList.get(QueueList.size() - 1).get(0))){
		    		try{
		    			deleteQueueItems(parseItemsFromCsvFile("itemlist3.csv"), false);
		    		}catch (Exception e){
		    			Logger.info("Error: " + e.getMessage());	    	    		
	    	    		stop = true;//Stop at the end of the queue this prevents an endless loop.
	    	    	}
		    		try{
		    			List<List<String>> emptyQueueList = getQueueList();
		    			//deleteQueueItems(emptyQueueList, true);
		    		}catch (Exception e){	    	    		
	    	    		Logger.info("Error: " + e.getMessage());
	    	    		stop = true;//Stop at the end of the queue this prevents an endless loop.
	    	    	}
			    	stop = true;//Stop at the end of the queue this prevents an endless loop.
		    	}
		    }else{
    			//Logger.info("The Master Queue is currently empty. Please load a user CSV file and retry.");
		    	stop = true;//Stop at the end of the queue this prevents an endless loop.
		    }

	    	
    	}
    	
    }
}
