package com.face.demo.service;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.stereotype.Service;

import com.face.demo.DTO.FaceDTO;
import com.face.demo.DTO.PersonFaceDTO;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class FaceService {
	
    public static final String storageConnectionString =
    		"DefaultEndpointsProtocol=https;"
    		+ "AccountName=<Enter-Your-Storage-Account-Name-Here>;"
    		+ "AccountKey=<Enter-Your-Storage-Account-Key-Here>";
    
    private static final String blobImageContainer = "photos";
    private static final String unknownBlobImageContainer = "unknowns";
	
	private static final String faceApikey = "<Enter-Your-Face-API-Key-Here>";
	
    private final BlobStorageService storageService;
	
    @Autowired
    public FaceService(BlobStorageService storageService) {
        this.storageService = storageService;
    }
	
	
	public String createPersonGroup(String name)
	{
		String group = name;
		String userData = "Person in group";
			
		try {
			  
        	
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);

    	    String body = "{ \"name\": \"" + group + "\",  \"userData\": \"" + userData + "\"}";
    	    	    
    	    System.out.println("Create PresonGroup: "+ body);
    		HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/persongroups/" + name)
    				.build();

    		System.out.println("Create PresonGroup Api: "+ builder.toUriString());
    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.PUT, entity, String.class);
    		System.out.println("Results of PersonGroup  " + result.getBody() );
    		
         }
        catch (Exception e)
        {
            System.out.println("Exception occurred in createPersonGroup" + e.getMessage());
        }
		return name;
	}
	
	
	public String createPerson(String personGroupId, String name)
	{
		String personId = null;
		
		String personGroupName = personGroupId;
		String person1 = name;
		String userData = "person name";

		
		try {
			  
        	
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    
    	    String body = "{ \"name\": \"" + person1 + "\",  \"userData\": \"" + userData + "\"}";
    	    System.out.println("Create PresonFace body "+ body);
    		HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/persongroups/" + personGroupName +  "/persons" )
    				.build();

    		System.out.println("Create PresonFace Api: "+ builder.toUriString());
    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
    		System.out.println("\n\n Results of PersonGroup  " + result.getBody().toString() );
    		String jsonResultBody = result.getBody().toString();
    		
    		//extract json result
    		if (jsonResultBody.charAt(0) == '{') {
                JSONObject jsonObject = new JSONObject(jsonResultBody);
                personId = jsonObject.getString("personId");
                System.out.println("Stored JSON personId:  " + personId);
            }
    		

       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in stored" + e.getMessage());
        }
		
		return personId;
	}
	
	
	
	public String addPersonFace(String imageUrl, FaceDTO faceDto)
	{
		String personFaceId = null;
		String key = "image" + faceDto.getCount();
		String userData = key;
		
		
		try {

    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    
    	    System.out.println("userData: " + userData);
    	    System.out.println("file Url: " + imageUrl);
    	    
    	    String body ="{ \"url\": \"" + imageUrl + "\" }";
    	    	    
    		HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/persongroups/" + faceDto.getGroupId() +  "/persons/" + faceDto.getPersonId() + "/persistedFaces")
    				.queryParam("userData", userData)
    				.build();

    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
    		System.out.println("\n\n Results of PersonGroup  " + result.getBody().toString() );
    		String jsonResultBody = result.getBody().toString();
    		
    		//extract json result
    		if (jsonResultBody.charAt(0) == '{') {
                JSONObject jsonObject = new JSONObject(jsonResultBody);
                personFaceId = jsonObject.getString("persistedFaceId");
                
                //store faceId in blob metadata
                storageService.setBlobMetaData(imageUrl, key, personFaceId);
            }
    		

       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in addFace: " + e.getMessage());

        }
		
		return personFaceId;
	}
	
	
	public String getPerson(String groupId, String personId)
	{
		String jsonResultBody = null;
		
		try {
			  
        	
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    	    
    		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("face/v1.0/persongroups/" + groupId + "/persons/" + personId)
    				.build();

    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
    		System.out.println("\n\n get Person..  " + result.getBody().toString() );
    		jsonResultBody = result.getBody().toString();


       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in get Person" + e.getMessage());
        }
		return jsonResultBody;
	}
	
	public String getPersonFaceMetaData(String groupId, String personId, String personFaceId)
	{
	
		String thumbnail = null;		
		try {
			  
        	
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    	    
    		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("face/v1.0/persongroups/" + groupId + "/persons/" + personId + "/persistedFaces/" + personFaceId)
    				.build();

    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
    		System.out.println("\n\n get Person Face..  " + result.getBody().toString() );
    		String jsonResultBody = result.getBody().toString();
    				
    		
    		//Todo extract faces
            if (jsonResultBody.charAt(0) == '{') {
                JSONObject jsonObject = new JSONObject(jsonResultBody);
                if( jsonObject.get("userData") != null)
                {
                	String key = jsonObject.get("userData").toString().trim();
                    thumbnail = storageService.getBlobByMetaData(key, personFaceId);
                    System.out.println("Here's the blobs metadata value:"  + thumbnail); 
                }
              
            } else {
                System.out.println(jsonResultBody);
            }	

       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in get Person Face" + e.getMessage());
        }
		return thumbnail;
	}
	
	
	public void trainPersonGroup(String personGroupId)
	{
		
		try {
			  
        	
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    	    
    		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
   
    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/persongroups/" + personGroupId +  "/train" )
    				.build();

    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
    		System.out.println("\n\n Training completed..  " + result.getBody() );

    		    		

       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in train" + e.getMessage());
        }
	}
	
	public String detectFace(String imageUrl)
	{
		
		String fileUrl = imageUrl;
		String jsonResultBody = null;
		
		try {
			    	  
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    
    	    String body = "{ \"url\": \"" + fileUrl + "\" }";
    	    System.out.println("file url in detect: " + body);
 	    
    		HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/detect")
    				.queryParam("returnFaceLandmarks", true)
    				.queryParam("returnFaceAttributes", "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise")
    				.build();

	
    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
    		System.out.println("\n\n Results of Stored Face detected:  " + result.getBody().toString() );
    		jsonResultBody = result.getBody().toString();

		}
        catch (Exception e)
        {
            System.out.println("Exception occurred in detect: " + e.getMessage());
        }
		
		return jsonResultBody;
	}
	
	public String identifyFace(String faceId, String groupId)
	{
		String jsonResultBody = null;
		
		try {
			    	  
    	    RestTemplate restTemplate = new RestTemplate();
    	    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    	      	    
    	    HttpHeaders headers = new HttpHeaders();
    	    headers.setContentType(MediaType.APPLICATION_JSON);
    	    headers.set("Ocp-Apim-Subscription-Key", faceApikey);
    	    
    	    String body = "{ \"personGroupId\": \"" + groupId + "\", \"faceIds\":[\"" + faceId + "\" ] }";
    	    System.out.println("file url in stored: " + body);
 	    
    		HttpEntity<String> entity = new HttpEntity<String>(body, headers);

    		UriComponents builder = UriComponentsBuilder.newInstance().scheme("https")
    				.host("eastus.api.cognitive.microsoft.com").path("/face/v1.0/identify")
    				.build();

	
    		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
    		System.out.println("\n\n Results of identify Face ed:  " + result.getBody().toString() );
    		jsonResultBody = result.getBody().toString();

       }
        catch (Exception e)
        {
            System.out.println("Exception occurred in identify" + e.getMessage());
        }

		return jsonResultBody;
	}
	

}
