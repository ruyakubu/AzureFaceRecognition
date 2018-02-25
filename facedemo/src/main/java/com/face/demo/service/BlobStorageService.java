package com.face.demo.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.face.demo.DTO.FaceDTO;

import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface BlobStorageService {

    void init();

    String store(FaceDTO faceDto);
    
    String store(MultipartFile file);
    
    void setBlobMetaData(String url, String key, String value);
    
    String getBlobByMetaData(String key, String value);

    Stream<Path> loadAll();
      
    String getBlobUrl(String filename);
    
    String getFileNameFromBlobURI(URI uri, String containerName);
}
