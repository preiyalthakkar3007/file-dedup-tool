package com.dedup;

import java.io.File;
import java.util.*;

public class DuplicateFinder {
    
    private Map<String, List<File>> hashMap;
    
    public DuplicateFinder() {
        this.hashMap = new HashMap<>();
    }
    
    public void processFiles(List<File> files) {
        System.out.println("Processing " + files.size() + " files...");
        
        for (File file : files) {
            String hash = HashCalculator.calculateHash(file);
            
            if (hash != null) {
                hashMap.putIfAbsent(hash, new ArrayList<>());
                hashMap.get(hash).add(file);
            }
        }
    }
    
    public Map<String, List<File>> getDuplicates() {
        Map<String, List<File>> duplicates = new HashMap<>();
        
        for (Map.Entry<String, List<File>> entry : hashMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }
        
        return duplicates;
    }
    
    public long calculateWastedSpace(Map<String, List<File>> duplicates) {
        long wastedSpace = 0;
        
        for (List<File> fileList : duplicates.values()) {
            long fileSize = fileList.get(0).length();
            wastedSpace += fileSize * (fileList.size() - 1);
        }
        
        return wastedSpace;
    }
}