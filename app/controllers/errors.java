package controllers;

import java.io.IOException;

import model.MasterQueueConfig;
import model.mainPageInfoModel;

import com.google.appengine.api.datastore.EntityNotFoundException;

import play.Logger;
import play.mvc.Controller;

public class errors extends Controller { 
   public static void accessIP() throws EntityNotFoundException, IOException {
	   render();
   }	
   public static void accessDomain() throws EntityNotFoundException, IOException {
	   render();
   }
   public static void accessAPI() throws EntityNotFoundException, IOException {
	   render();
   }	      

}
