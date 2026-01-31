package com.dedup;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    
    public static void printReport(Map<String, List<File>> duplicates, long wastedSpace) {
        if (duplicates.isEmpty()) {
            System.out.println("No duplicate files found!");
            return;
        }
        
        System.out.println("\n===== DUPLICATE FILES FOUND =====\n");
        
        int groupNumber = 1;
        for (Map.Entry<String, List<File>> entry : duplicates.entrySet()) {
            List<File> files = entry.getValue();
            long fileSize = files.get(0).length();
            
            System.out.println("Group " + groupNumber + " (" + files.size() + " copies, " + formatSize(fileSize) + " each):");
            
            for (File file : files) {
                System.out.println("  - " + file.getAbsolutePath());
            }
            
            System.out.println();
            groupNumber++;
        }
        
        System.out.println("===== SUMMARY =====");
        System.out.println("Total duplicate groups: " + duplicates.size());
        System.out.println("Wasted space: " + formatSize(wastedSpace));
    }
    
    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}