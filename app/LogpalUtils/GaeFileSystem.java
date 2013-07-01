package LogpalUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import org.h2.store.fs.FileUtils;

import play.Logger;
import services.ServiceUtility;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;

/*
 * Writing Files to the Blobstore (Experimental)
 * https://developers.google.com/appengine/docs/java/blobstore/overview#Writing_Files_to_the_Blobstore
 * 
 * App Engine allows you to create Blobstore blobs programmatically, providing a file-like API that you can use to read and write to blobs. Some common uses of this functionality include exporting data and generating reports—or any function that involves generating large binary data objects.
 * 
 
	Quotas:
	===========
	Data stored in the blobstore counts toward the Stored Data (billable) quota, described above. The following quotas apply specifically to use of the blobstore.
	
	Blobstore Stored Data
	    The total amount of data stored in the blobstore. Also counts toward the Stored Data (billable) quota. Available for both paid and free apps.
	
	Resource 				Free Default Limit 	Billing Enabled Default Limit
	Blobstore Stored Data 	5 GB 				5 GB free; no maximum
*/
public class GaeFileSystem {
	
	public GaeFileSystem(){
		
	}
	/*
		try:
		  mail.SendMessage(to='test@example.com',
		                   from='admin@example.com',
		                   subject='Test Email',
		                   body='Testing')
		except apiproxy_errors.OverQuotaError, message:
		  # Log the error.
		  logging.error(message)
		  # Display an informative message to the user.
		  self.response.out.write('The email could not be sent. '
		                          'Please try again later.')
	 */
	
	//Method to write Text Content to a Blobstore.
	public static String Write(String FileContent) throws IOException {
		// Get a file service		
		FileService fileService = FileServiceFactory.getFileService();	
		// Create a new Blob file with mime-type "text/plain"
		AppEngineFile file = null;		
		//Open/Create a file to write content.
		/*
		if(path!=null && !("".equalsIgnoreCase(path))){			
	    	try {	    		
	    		BlobKey blobKey = new BlobKey(path);
	    		Logger.info("***** Write >2");
	    		file = fileService.getBlobFile(blobKey);
	    		Logger.info("***** Write >3");
	    	} catch (FileNotFoundException e) {
	    		//log.severe("getImageBytes_V2(): Error: fileService error " + e.toString());
	    		e.printStackTrace();
	    	}
		}*/
		if(file==null){		
		    try{
		    	file = CloudFileUtils.createFile("text/plain", FileContent.getBytes());
		    }
		    catch(Exception e){
		    	Logger.info("Error :"+e+">>"+e.getMessage());
		    }		    
		}
		return CloudFileUtils.getFileBlobKey(file).getKeyString();
		// Open a channel to write to it
	}
	
	
	/*
	The AppEngine Blobstore only allows you to read data in chunks up to BlobstoreService.MAX_BLOB_FETCH_SIZE.

	The documentation states that Google upped the limit to 32MB per read call but in dev mode it still seems to be 1MB (AppEngine 1.6.1 on a Mac).
	
	http://code.google.com/p/gwt-examples/wiki/DemoGAEMultiFileBlobUpload
	*/
	public static byte[] Read(String path) throws IOException {
		FileService fileService = FileServiceFactory.getFileService();	
		// Create a new Blob file with mime-type "text/plain"
		AppEngineFile file = null;		
		//Open/Create a file to write content.
		//Logger.info("***** read >1"+path);
		if(path!=null && !("".equalsIgnoreCase(path))){			
	    	try {	    		
	    		BlobKey blobKey = new BlobKey(path);
	    		//Logger.info("***** read >2");
	    		file = fileService.getBlobFile(blobKey);
	    		//Logger.info("***** read >3");
	    		return CloudFileUtils.getFileContent(file);
	    		
	    	} catch (FileNotFoundException e) {
	    		//log.severe("getImageBytes_V2(): Error: fileService error " + e.toString());
	    		e.printStackTrace();
	    	}
		}		
		//Logger.info("***** read >10");
		return "".getBytes();
	}
/*
This is a low-level API. You can use the high-level mapreduce API to create Blobstore files based on datastore data.

The following sample shows how to create a new Blobstore file and manipulate it using the File API:

  // Get a file service
  FileService fileService = FileServiceFactory.getFileService();

  // Create a new Blob file with mime-type "text/plain"
  AppEngineFile file = fileService.createNewBlobFile("text/plain");

  // Open a channel to write to it
  boolean lock = false;
  FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);

  // Different standard Java ways of writing to the channel
  // are possible. Here we use a PrintWriter:
  PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
  out.println("The woods are lovely dark and deep.");
  out.println("But I have promises to keep.");

  // Close without finalizing and save the file path for writing later
  out.close();
  String path = file.getFullPath();

  // Write more to the file in a separate request:
  file = new AppEngineFile(path);

  // This time lock because we intend to finalize
  lock = true;
  writeChannel = fileService.openWriteChannel(file, lock);

  // This time we write to the channel directly
  writeChannel.write(ByteBuffer.wrap
            ("And miles to go before I sleep.".getBytes()));

  // Now finalize
  writeChannel.closeFinally();

  // Later, read from the file using the file API
  lock = false; // Let other people read at the same time
  FileReadChannel readChannel = fileService.openReadChannel(file, false);

  // Again, different standard Java ways of reading from the channel.
  BufferedReader reader =
          new BufferedReader(Channels.newReader(readChannel, "UTF8"));
       String line = reader.readLine();
  // line = "The woods are lovely dark and deep."

  readChannel.close();

  // Now read from the file using the Blobstore API
  BlobKey blobKey = fileService.getBlobKey(file);
  BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
  String segment = new String(blobStoreService.fetchData(blobKey, 30, 40));
 * */	
}
