package com.dedup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {
    
    private List<File> files;
    
    public FileScanner() {
        this.files = new ArrayList<>();
    }
    
    public List<File> scanDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return files;
        }
        
        scanRecursive(directory);
        return files;
    }
    
    private void scanRecursive(File directory) {
        File[] contents = directory.listFiles();
        
        if (contents == null) {
            return;
        }
        
        for (File file : contents) {
            if (file.isDirectory()) {
                scanRecursive(file);
            } else if (file.isFile()) {
                files.add(file);
            }
        }
    }
    
    public int getFileCount() {
        return files.size();
    }
}