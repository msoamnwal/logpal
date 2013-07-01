package LogpalUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;

import play.Logger;

import services.ServiceUtility;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;

public class CloudFileUtils {

  public static AppEngineFile createFile(String contentType, byte[] data) throws IOException {
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile file = fileService.createNewBlobFile(contentType);
    writeFileData(file, fileService, data);
    return file;
  }
  
  public static AppEngineFile updateFileData(AppEngineFile file, byte[] data, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
    FileService fileService = FileServiceFactory.getFileService();
    writeFileData(file, fileService, data);
    return file;
  }
  
  public static byte[] getFileContent(AppEngineFile file) throws FileNotFoundException, LockException, IOException {
    FileService fileService = FileServiceFactory.getFileService();
    FileReadChannel readChannel = fileService.openReadChannel(file, false);
    InputStream inputStream = Channels.newInputStream(readChannel);    
    
    byte[] bytes = ServiceUtility.getInputStreamAsBytes(inputStream);
    readChannel.close();
    return bytes;
  }
  
  public static byte[] getFileContent(String fileName) throws IOException {
    AppEngineFile file = new AppEngineFile(fileName);
    return getFileContent(file);
  }
  
  public static BlobKey getFileBlobKey(AppEngineFile file) {
    FileService fileService = FileServiceFactory.getFileService();
    return fileService.getBlobKey(file);
  }
  
  public static long getFileSize(AppEngineFile file) throws IOException {
    BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
    BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(getFileBlobKey(file));
    return blobInfo.getSize();
  }

  public static void deleteFile(String fileName) {
    AppEngineFile file = new AppEngineFile(fileName);
    deleteFile(file);
  }
  
  public static void deleteFile(AppEngineFile file) {
    FileService fileService = FileServiceFactory.getFileService();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    blobstoreService.delete(fileService.getBlobKey(file));
  }

  private static void writeFileData(AppEngineFile file, FileService fileService, byte[] data) throws FileNotFoundException, FinalizationException, LockException, IOException {
	  FileWriteChannel writeChannel =null;
	  OutputStream outputStream = null;
	try{
		//Logger.info("***** openWriteChannel");
	    writeChannel = fileService.openWriteChannel(file, true);
	    //Logger.info("***** openWriteChannel done");
	    outputStream = Channels.newOutputStream(writeChannel);
	
	    int chunkSize = 1024 * 10;
	    int len = data.length;
	    int i = 0;
	    while (i < len) {
	      int bytesLeft = len - i;
	      outputStream.write(data, i, bytesLeft > chunkSize ? chunkSize : bytesLeft);
	      i += chunkSize;
	    }
	
	    outputStream.flush();
	    outputStream.close();
	    writeChannel.close();
	    writeChannel.closeFinally();
	    //Logger.info("***** @@@@@@@ closed");
	}finally{
	    while(writeChannel!=null && writeChannel.isOpen()){
	    	//Logger.info("***** @@@@@@@ close");
		    writeChannel.close();
		    writeChannel.closeFinally();
	    }
		
	}
    
    
  }
}