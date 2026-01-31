package com.dedup;

import java.io.File;
import java.util.*;

public class FileDedupTool {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        
        String directory = args[0];
        boolean delete = false;
        
        if (args.length > 1 && args[1].equals("--delete")) {
            delete = true;
        }
        
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Error: Invalid directory path");
            return;
        }
        
        System.out.println("Scanning directory: " + directory);
        System.out.println("This may take a while...\n");
        
        FileScanner scanner = new FileScanner();
        List<File> files = scanner.scanDirectory(dir);
        
        System.out.println("Found " + files.size() + " files\n");
        
        DuplicateFinder finder = new DuplicateFinder();
        finder.processFiles(files);
        
        Map<String, List<File>> duplicates = finder.getDuplicates();
        long wastedSpace = finder.calculateWastedSpace(duplicates);
        
        ReportGenerator.printReport(duplicates, wastedSpace);
        
        if (delete && !duplicates.isEmpty()) {
            deleteDuplicates(duplicates);
        }
    }
    
    private static void deleteDuplicates(Map<String, List<File>> duplicates) {
        System.out.println("\n===== DELETING DUPLICATES =====\n");
        
        int deleted = 0;
        for (List<File> fileList : duplicates.values()) {
            for (int i = 1; i < fileList.size(); i++) {
                File file = fileList.get(i);
                if (file.delete()) {
                    System.out.println("Deleted: " + file.getAbsolutePath());
                    deleted++;
                } else {
                    System.out.println("Failed to delete: " + file.getAbsolutePath());
                }
            }
        }
        
        System.out.println("\nTotal files deleted: " + deleted);
    }
    
    private static void printUsage() {
        System.out.println("Usage: java FileDedupTool <directory> [--delete]");
        System.out.println("  <directory>  Directory to scan for duplicates");
        System.out.println("  --delete     Delete duplicate files (keeps first occurrence)");
    }
}