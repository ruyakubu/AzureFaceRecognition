package com.face.demo.service;

import java.io.IOException;

import java.util.Date;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.URI;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Policy;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.face.demo.DTO.FaceDTO;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.Permissions;
import com.microsoft.azure.storage.SharedAccessAccountPolicy;
import com.microsoft.azure.storage.SharedAccessAccountService;
import com.microsoft.azure.storage.SharedAccessAccountResourceType;
import com.microsoft.azure.storage.SharedAccessPolicy;

@Service
public class BlogStorageServiceImpl implements BlobStorageService {
	
    public static final String storageConnectionString =
    		"DefaultEndpointsProtocol=https;"
    	    		+ "AccountName=<Enter-Your-Storage-Account-Name-Here>;"
    	    		+ "AccountKey=<Enter-Your-Storage-Account-Key-Here>;"
    		+ "EndpointSuffix=core.windows.net";
    
    private static final String blobImageContainer = "photos";
    private Path rootLocation;
    

    @Override
    public String store(FaceDTO faceDto) {
    	
    	String fileUrl = null;
    	
		try {
			CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            //CloudBlobContainer container = serviceClient.getContainerReference(blobImageContainer);
            CloudBlobContainer container = serviceClient.getContainerReference(blobImageContainer);
            container.createIfNotExists();
            
            MultipartFile file = faceDto.getFile();
            String path = StringUtils.cleanPath(file.getOriginalFilename());
            File sourceFile = new File(path);
            String filename = sourceFile.getName();
     
            // Define the path to a local file.
            final String filePath = "C:\\Your\\local\\image\\file\\path\\" + filename;
            
            System.out.println("file name: " + filename);
            System.out.println("file path: " + filePath);  
            
            // Upload an image file.
            CloudBlockBlob blob = container.getBlockBlobReference(filename);
            File imagePath = new File(filePath);
            blob.upload(new FileInputStream(imagePath), imagePath.length());
            fileUrl = blob.getUri().toString();

        }
        catch (FileNotFoundException fileNotFoundException) {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        }
        catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
		
		return fileUrl;
    }
    
    @Override
    public String store(MultipartFile file) {
    	
    	String fileUrl = null;
    	
		try {
			CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            CloudBlobContainer container = serviceClient.getContainerReference(blobImageContainer);
            container.createIfNotExists();
            
            String path = StringUtils.cleanPath(file.getOriginalFilename());
            File sourceFile = new File(path);
            String filename = sourceFile.getName();
     
            // Define the path to a local file.
            final String filePath =  "C:\\Your\\local\\image\\file\\path\\" + filename;
            
            // Upload an image file.
            CloudBlockBlob blob = container.getBlockBlobReference(filename);
            File imagePath = new File(filePath);
            blob.upload(new FileInputStream(imagePath), imagePath.length());
            fileUrl = blob.getUri().toString();

        }
        catch (FileNotFoundException fileNotFoundException) {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        }
        catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
		
		return fileUrl;
    }
    
    

    @Override
    public String getBlobUrl(String filename)
    {
    	String fileUrl = null;
    	
    	try
    	{
    	    // Retrieve storage account from connection-string.
    	    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

    	    // Create the blob client.
    	    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

    	    // Retrieve reference to a previously created container.
    	    CloudBlobContainer container = blobClient.getContainerReference(blobImageContainer);
    	    
    	    
    	    // Get blob file within the container and output the URI 
    	    if( container.getBlockBlobReference(filename).exists())
    	    {
    	    	CloudBlockBlob blobItem = container.getBlockBlobReference(filename); 
    	    	fileUrl = blobItem.getUri().toString();
    	    }
    	         	
    	}
    	catch (Exception e)
    	{
    	    // Output the stack trace.
    	    e.printStackTrace();
    	}
    	
    	  return fileUrl;
    }
    
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            System.out.println("Could not initialize storage " + e);
        }
    }
    
    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        }
        catch (IOException e) {
            System.out.println("Failed to read stored files " +  e);
            return null;
        }

    }
    
    @Override
    public void setBlobMetaData(String url, String key, String value)
    {
    	HashMap<String, String > metadata = new HashMap<String, String>();
    	metadata.put(key, value);
    	
    	try
    	{
    	    // Retrieve storage account from connection-string.
    	    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
    	    

            Calendar now = Calendar.getInstance();
            now.add(Calendar.HOUR, 24);
    	    
    	    // Create a new access policy for the account.
    	    SharedAccessAccountPolicy policy = new SharedAccessAccountPolicy();
    	    policy.setPermissions(EnumSet.of(SharedAccessAccountPermissions.READ, SharedAccessAccountPermissions.WRITE, SharedAccessAccountPermissions.LIST, SharedAccessAccountPermissions.CREATE));
    	    policy.setServices(EnumSet.of(SharedAccessAccountService.BLOB));
    	    policy.setResourceTypes(EnumSet.of(SharedAccessAccountResourceType.SERVICE));
    	    policy.setProtocols(SharedAccessProtocols.HTTPS_ONLY);
    	    policy.setSharedAccessExpiryTime(now.getTime());
    	  
    	    storageAccount.generateSharedAccessSignature(policy);
    	    

    	    // Create the blob client.
    	    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
    	    
       	    // Retrieve reference to a previously created container.
    	    CloudBlobContainer container = blobClient.getContainerReference(blobImageContainer);
    	    
    	    for (ListBlobItem blobItem : container.listBlobs()) {

    	    	String blobItemUrl = blobItem.getUri().toString().trim();
        	    if( blobItemUrl.equals( url.trim()) )
        	    {
        	    	String fName = getFileNameFromBlobURI(blobItem.getUri(), blobImageContainer);
        	    	if (blobItem instanceof CloudBlockBlob) {
            	    	CloudBlockBlob blob = container.getBlockBlobReference(fName); 

	        	    	blob.setMetadata(metadata);
	        	    	blob.uploadMetadata();
	        	    	blob.uploadProperties();
        	    	}
        	    }
    	    }
    	    

    	       	
    	}
    	catch (Exception e)
    	{
    	    // Output the stack trace.
    	    e.printStackTrace();
    	}
    }
    
    @Override
    public String getBlobByMetaData(String key, String value)
    {
    	String fileUrl = null;
    	HashMap<String, String > metadata = new HashMap<String, String>();

    	try
    	{
    	    // Retrieve storage account from connection-string.
    	    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

    	    // Create the blob client.
    	    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
    	    
       	    // Retrieve reference to a previously created container.
    	    CloudBlobContainer container = blobClient.getContainerReference(blobImageContainer);
    	    
    	    for (ListBlobItem blobItem : container.listBlobs()) {

    	    	String fName = getFileNameFromBlobURI(blobItem.getUri(), blobImageContainer);
    	    	if (blobItem instanceof CloudBlockBlob) {

        	    	CloudBlockBlob blob = container.getBlockBlobReference(fName); 
        	    	blob.downloadAttributes();
        	    	metadata = blob.getMetadata();
        	    	if(metadata != null)
        	    	{
        	    		if( (metadata.containsKey(key.trim())) && (metadata.containsValue(value.trim())) )
        	    		{
        	    			fileUrl = blob.getUri().toString();
        	    		}
        	    	}
      	    	  
        	    }
    	    }
       	 }
    	catch (Exception e)
    	{
    	    // Output the stack trace.
    	    e.printStackTrace();
    	}
    	
    	return fileUrl;
    }
    
    public String getFileNameFromBlobURI(URI uri, String containerName)
    {
        String urlStr = uri.toString();
        String keyword = "/"+containerName+"/";
        int index = urlStr.indexOf(keyword) + keyword.length();
        String filePath = urlStr.substring(index);
        return filePath;
    }

}
