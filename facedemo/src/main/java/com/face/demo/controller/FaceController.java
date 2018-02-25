package com.face.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.face.demo.service.FaceService;
import com.face.demo.service.BlobStorageService;
import com.face.demo.DTO.FaceDTO;
import com.face.demo.DTO.PersonFaceDTO;
import com.face.demo.DTO.UnknownPersonDTO;



@Controller
public class FaceController {

    private final BlobStorageService storageService;

	@Autowired
	private FaceService faceservice;
	
	
    @Autowired
    public FaceController(BlobStorageService storageService) {
        this.storageService = storageService;
    }
	


    @RequestMapping(method = RequestMethod.GET, value="/persongroup/add")
    public String displayGroupForm(Model model) throws IOException {

        return "GroupForm";
    }
    
    @PostMapping("/persongroup/add")  @RequestMapping(method = RequestMethod.POST, value="/persongroup/add")
    public String addPersonGroup( @RequestParam String name, RedirectAttributes redirectAttributes) {

    		String result = faceservice.createPersonGroup(name); 
            redirectAttributes.addFlashAttribute("message",
                    "You successfully created person group Id: " + result + "!");
           
            return "redirect:/persongroup/add";
     
    }     
    
    
    
    
    @RequestMapping(method = RequestMethod.GET, value="/person/add/{groupId}")
    public String displayPersonForm( @PathVariable("groupId") String groupId, Model model) {
    	
    	model.addAttribute("groupName", groupId);
        return "PersonForm";
    } 
    
    @PostMapping("/person/add/{groupId}")  @RequestMapping(method = RequestMethod.POST, value="/person/add/{groupId}")
    public String addPerson( @RequestParam String name, @PathVariable("groupId") String groupId, RedirectAttributes redirectAttributes) {

    		String result = faceservice.createPerson(groupId, name);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully created person Id: " + result + "!");
           
            return "redirect:/person/add/" + groupId;
     
    }   
    
    
    
    @RequestMapping(method = RequestMethod.GET, value="/personface/add/{groupId}/{personId}")
    public String displayGroupForm(@PathVariable("groupId") String groupId, @PathVariable("personId") String personId, Model model) throws IOException {
    	
    	model.addAttribute("groupName", groupId);
    	model.addAttribute("personName", personId);

        return "uploadFaceForm";
    }
     
    @PostMapping("/personface/add/{groupId}/{personId}")   @RequestMapping(method = RequestMethod.POST, value="/personface/add/{groupId}/{personId}")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam String name,
    		@PathVariable("groupId") String groupId, @PathVariable("personId") String personId, @RequestParam String count,
            RedirectAttributes redirectAttributes) {
    	
    		String jsonResultBody = null;
    		FaceDTO personFaceDto = new FaceDTO();
    		
    		personFaceDto.setName(name);
    		personFaceDto.setGroupId(groupId);
    		personFaceDto.setPersonId(personId);
    		personFaceDto.setCount(count);
    		personFaceDto.setFile(file);
    	
    		String fileUrl= storageService.store(personFaceDto);
    		jsonResultBody = faceservice.addPersonFace(fileUrl,personFaceDto);
        
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        
        
        redirectAttributes.addFlashAttribute("json", "Here is your Person Face ID:  " + jsonResultBody);

        return "redirect:/personface/add/" + groupId + "/" + personId;
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value="/train/{groupId}/{personId}")
    public String displayTrainForm(@PathVariable("groupId") String groupId, @PathVariable("personId") String personId, Model model)  {
    	
    	List<PersonFaceDTO> personFaceList = new ArrayList<PersonFaceDTO>();
    
    	String jsonPersonResult = faceservice.getPerson(groupId, personId);

        if (jsonPersonResult.charAt(0) == '{') {
      	
            JSONObject jsonObject = new JSONObject(jsonPersonResult);
            String personName = jsonObject.getString("name");
            
            if (jsonObject.get("persistedFaceIds").toString().charAt(0) == '[') {
           	
                JSONArray jsonArray = new JSONArray(jsonObject.get("persistedFaceIds").toString());
                
                for(int i=0; i<jsonArray.length(); i++){
                	
                	String persistedFaceId = jsonArray.getString(i);
                    
                    if(persistedFaceId != null)
                    {
                    	String thumbnail = faceservice.getPersonFaceMetaData(groupId, personId, persistedFaceId);
                    	
                    	PersonFaceDTO personFaceDto = new PersonFaceDTO();
                    	personFaceDto.setGroupId(groupId);
                    	personFaceDto.setPersonId(personId);
                    	personFaceDto.setPersonName(personName);
                    	personFaceDto.setPersonFaceUrl(thumbnail);
                    	personFaceList.add(personFaceDto);
  
                    }

                }  
            }
        } 
    	  	
    	model.addAttribute("personFaces", personFaceList);
        return "train";
    }
    
    @PostMapping("/train/{groupId}/{personId}")   @RequestMapping(method = RequestMethod.POST, value="/train/{groupId}/{personId}")
    public String trainGroup(@PathVariable("groupId") String groupId,  @PathVariable("personId") String personId, RedirectAttributes redirectAttributes) {
    	
    	faceservice.trainPersonGroup(groupId);
        
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + groupId + "!");
        
        return "redirect:/train/" + groupId + "/" + personId;
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value="/identify/{groupId}/{personId}")
    public String displayUploadForm(@PathVariable("groupId") String groupId, @PathVariable("personId") String personId, Model model) throws IOException {
    	
    	model.addAttribute("groupName", groupId);
    	model.addAttribute("personId", personId);
    	
    	return "uploadUnknownFaceForm";
    }
    
    
    @PostMapping("/identify/{groupId}/{personId}")   @RequestMapping(method = RequestMethod.POST, value="/identify/{groupId}/{personId}")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable("groupId") String groupId, @PathVariable("personId") String personId, RedirectAttributes redirectAttributes) {
    	
    	UnknownPersonDTO unknownPersonDto = new UnknownPersonDTO();
    	List<String> thumbnailList = new ArrayList<String>();
    	List<String> jsonFromIdentifyList = new ArrayList<String>();
    	
    	String confidence = null;
    	String identifiedPersonName = null;
 	
    	//Detect unknown image and search identity in group
    	String fileUrl= storageService.store(file);
    	String jsonFromDetect = faceservice.detectFace(fileUrl);
    	List<String> faceList =  parseFaceIdFromDetect(jsonFromDetect);
    	for(int i=0; i < faceList.size(); i++)
    	{

    		String faceId = faceList.get(i);
    		String jsonFromIdentify = faceservice.identifyFace(faceId, groupId);
    		unknownPersonDto = parseConfidenceFromIdentify(faceId, jsonFromIdentify);
    		
    		if(unknownPersonDto.getConfidence() != null)
    		{
    			confidence = unknownPersonDto.getConfidence().toString();

    			String jsonPersonResult = faceservice.getPerson(groupId, unknownPersonDto.getPersonId());
    			identifiedPersonName = parsePersonName(jsonPersonResult);
    		}
    		
    		if(jsonFromIdentify != null )
    		{
    			System.out.println("\n\nJson from Identify: " + jsonFromIdentify.toString());
    			jsonFromIdentifyList.add(jsonFromIdentify.toString());
    		}
    		
    	}
    	    	
    	//Retrieved all the stored in faces in the group
    	String jsonPersonResult = faceservice.getPerson(groupId, personId);
    	String personName = parsePersonName(jsonPersonResult);
    	

        if (jsonPersonResult.charAt(0) == '{') {
      	
            JSONObject jsonObject = new JSONObject(jsonPersonResult);
                     
            if (jsonObject.get("persistedFaceIds").toString().charAt(0) == '[') {
           	
                JSONArray jsonArray = new JSONArray(jsonObject.get("persistedFaceIds").toString());
                
                for(int i=0; i<jsonArray.length(); i++){
                	
                	String persistedFaceId = jsonArray.getString(i);
                    
                    if(persistedFaceId != null)
                    {
                    	String thumbnail = faceservice.getPersonFaceMetaData(groupId, personId, persistedFaceId);
                    	thumbnailList.add(thumbnail);                    	
  
                    }
                }  
            }
            
        } 
    
        redirectAttributes.addFlashAttribute("group", groupId);
        redirectAttributes.addFlashAttribute("personName", personName);
        redirectAttributes.addFlashAttribute("thumbnails", thumbnailList);

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
          
        redirectAttributes.addFlashAttribute("image", fileUrl);
        confidence = (confidence == null) ? "0.0" : confidence;  
        redirectAttributes.addFlashAttribute("confidence", "Identification for :  " + identifiedPersonName + " found w/ confidence: " + confidence);
        redirectAttributes.addFlashAttribute("jsonDetect", "Here json from face detection attributes:  " + jsonFromDetect);
        redirectAttributes.addFlashAttribute("jsonFromIdentifyList", "Here json from face identification attributes:  " + jsonFromIdentifyList);

        return "redirect:/identify/" + groupId + "/" + personId;
    }
    
    
    private String parsePersonName(String jsonResult)
    {
    	String personName = null;
    	if (jsonResult.charAt(0) == '{') {
            JSONObject jsonObject = new JSONObject(jsonResult);
            personName = jsonObject.getString("name");
     
        }
    	
    	return personName;
    }
    
    private List<String> parseFaceIdFromDetect(String jsonResultBody)
    {
    	List<String> faceIds = new ArrayList<String>();
    	
    	//Todo extract face
    	String faceId = null;
    	if(jsonResultBody != null)
    	{
	        if (jsonResultBody.charAt(0) == '[') {
	            JSONArray jsonArray = new JSONArray(jsonResultBody);
	            for(int i=0; i<jsonArray.length(); i++){
	                JSONObject obj = jsonArray.getJSONObject(i);
	
	                faceId = obj.getString("faceId");
	                System.out.println("Stored Face API ID:  " + faceId);
	                faceIds.add(faceId);

	            }   
	
	            System.out.println("Stored JSON object:  " + jsonArray.toString(2));
	        }
	        else if (jsonResultBody.charAt(0) == '{') {
	            JSONObject jsonObject = new JSONObject(jsonResultBody);
	            System.out.println("Stored JSON object:  " + jsonObject.toString(2));
	        } else {
	            System.out.println(jsonResultBody);
	        }
    	}
	    	return faceIds;
	    
    }
    
    private UnknownPersonDTO parseConfidenceFromIdentify(String faceId, String jsonResultBody)
    {
    	String id = null;
    	String confidence = null;
    	String personId = null;
    	UnknownPersonDTO unknownPersonDto = new UnknownPersonDTO();
    	
    	if (jsonResultBody != null ) {
    		//Todo extract confidence
            if (jsonResultBody.charAt(0) == '[') {
                JSONArray jsonArray = new JSONArray(jsonResultBody);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject obj = jsonArray.getJSONObject(i);        
                    id = obj.getString("faceId");
                    System.out.println("Stored Face API ID:  " + faceId);
                    System.out.println("Stored JSON object:  " + jsonArray.toString(2));
                    if (obj.get("candidates").toString().charAt(0) == '[') {
                        JSONArray jsonArray2 = new JSONArray(obj.get("candidates").toString());
                        for(int x=0; x<jsonArray2.length(); x++){
                        	String candidates = jsonArray2.get(x).toString();
                        	 System.out.println("candidates:  " + candidates);
                            if(candidates != null)
                            {
                            	if (candidates.charAt(0) == '{') {
                                    JSONObject jsonObject = new JSONObject(candidates);
                                    confidence = jsonObject.get("confidence").toString();
                                    personId = jsonObject.get("personId").toString();
                                    
                                    System.out.println("confidence:  " + confidence);
                                }
                            }
                        }
                    }   
                }
                
            }
        }
    	
    	unknownPersonDto.setConfidence(confidence);
    	unknownPersonDto.setPersonId(personId);
    	return unknownPersonDto;
    }
    
    
    

    
}
