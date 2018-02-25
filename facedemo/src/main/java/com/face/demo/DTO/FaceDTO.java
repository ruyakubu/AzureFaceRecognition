package com.face.demo.DTO;

import org.springframework.web.multipart.MultipartFile;

public class FaceDTO {
	
	private String groupId;
	private String personId;
	private String personFaceId;
	private String name;
	private String imageUrl;
	private String count;
	private MultipartFile file;
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	public String getPersonFaceId() {
		return personFaceId;
	}
	public void setPersonFaceId(String personFaceId) {
		this.personFaceId = personFaceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public MultipartFile getFile() {
		return file;
	}
	public void setFile(MultipartFile file) {
		this.file = file;
	}
	



}
