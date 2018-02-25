package com.face.demo.DTO;

public class UnknownPersonDTO {
	
	private String personId;
	private String persistedFaceId;
	private String confidence;
	private String personName;
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	public String getPersistedFaceId() {
		return persistedFaceId;
	}
	public void setPersistedFaceId(String persistedFaceId) {
		this.persistedFaceId = persistedFaceId;
	}
	public String getConfidence() {
		return confidence;
	}
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	
	

}
