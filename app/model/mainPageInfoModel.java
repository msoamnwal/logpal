package model;

import java.io.IOException;

import jobs.MasterQueue;

import play.Logger;
import play.Play;
import services.ServiceUtility;
import services.UserInfoService;
import LogpalUtils.CryptoUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import controllers.lg3;

public class mainPageInfoModel {
	public Entity defaultSetting;
	public UserDataForAuthDetail loggedInUsrInfo;
	public Boolean isSuperAdmin;
	public String LastQueueProcessedTime;
	public String NextScheduledBathTime;
	public String currentQueueStatus;//Queue Status
	public String currentQueueStatusIcon;//Queue Status Icon
	public String currentQueueItemTime;//Current Item in progress.
	public String currentQueueItemDetail;//Current Item in progress.
	public String itemsPendingInCurrentBatch;
	public String totalBatchPendingInMasterQueue;
	
	public static mainPageInfoModel get(String accessToken, String accessTokenSecret) throws IOException{
		
		//Pull Default settings.
		mainPageInfoModel mainModel = new mainPageInfoModel();
		GoogleOAuthParameters oauthParameters = ServiceUtility.getOAuthParameters(new CryptoUtils().decrypt(accessToken), 
				new CryptoUtils().decrypt(accessTokenSecret));
    	UserInfoService usrSvr = new UserInfoService();    	    	    	
        Entity mainObj = MasterQueueConfig.getMasterDefault();
        mainModel.defaultSetting = mainObj;        
        UserDataForAuthDetail loggedinUsrInfo = usrSvr.getUserDetailByToken(oauthParameters, new CryptoUtils().decrypt(accessToken));
        mainModel.loggedInUsrInfo = loggedinUsrInfo;  
        //Logger.info("mainModel.loggedInUsrInfo :"+mainModel.loggedInUsrInfo);
        if(mainModel.loggedInUsrInfo!=null && mainModel.loggedInUsrInfo.data !=null){
			mainModel.isSuperAdmin = (Play.configuration.getProperty(lg3.GOOGLE_API_ADMINEMAIL).toLowerCase()).contains(mainModel.loggedInUsrInfo.data.email.toLowerCase());
			//Logger.info("mainModel.isSuperAdmin :"+mainModel.isSuperAdmin);
		}else{
			//Logger.info("mainModel.isSuperAdmin False:"+mainModel.loggedInUsrInfo);
			mainModel.isSuperAdmin = false;
		}
		//Calculating MasterQueue Details
		mainModel.itemsPendingInCurrentBatch = MasterQueue.getPendingItemInCurrentBatch(); 
		mainModel.currentQueueItemTime = "Started at :" + MasterQueue.getRecentItemProcessStart();
		String currentQueueItemDetail = MasterQueue.getCurrentQueueItem();
		if("".equalsIgnoreCase(currentQueueItemDetail)){
			currentQueueItemDetail = " -- ";
		}
		mainModel.currentQueueItemDetail = "Detail: " + currentQueueItemDetail;
		
        mainModel.LastQueueProcessedTime = MasterQueue.getLastQueueProcessedTime();
        if(mainModel.LastQueueProcessedTime==null || "".equalsIgnoreCase(mainModel.LastQueueProcessedTime) || "null".equalsIgnoreCase(mainModel.LastQueueProcessedTime)){
        	mainModel.LastQueueProcessedTime = "No batch yet completed.";
        }
        mainModel.totalBatchPendingInMasterQueue = MasterQueue.getTotalPendingBatches();
        mainModel.NextScheduledBathTime = MasterQueue.getNextScheduledTime();
        if(MasterQueue.stop==false){
        	//Queue is processing a Batch.
        	mainModel.currentQueueStatus = "Processing is enabled";
        	if(MasterQueue.getCurrentBatchInProgress()){
        		mainModel.currentQueueStatus += " and currently processing a batch.";
        		mainModel.currentQueueStatusIcon = "confirm32.png";
        	}else{
        		mainModel.currentQueueStatus += " and currently looking for next batch to process.";
        		mainModel.currentQueueStatusIcon = "securityconfirm48.gif";
        	}
        }else{
        	if(MasterQueue.getCurrentBatchInProgress()){
        		mainModel.currentQueueStatus = "Scheduled to stop and waiting for current batch to be completed.";
        		mainModel.currentQueueStatusIcon = "warning32.png";
        	}else{
        		mainModel.currentQueueStatus = "Processing stopped.";
        		mainModel.currentQueueStatusIcon = "error32.png";
        	}
        }		
        return mainModel;
	}
	
}
