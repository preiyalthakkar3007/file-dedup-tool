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
        
        // TODO: Implement scanning logic
        System.out.println("Scan complete!");
    }
    
    private static void printUsage() {
        System.out.println("Usage: java FileDedupTool <directory> [--delete]");
        System.out.println("  <directory>  Directory to scan for duplicates");
        System.out.println("  --delete     Delete duplicate files (keeps first occurrence)");
    }
}