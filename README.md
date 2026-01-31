# File Deduplication Tool

A command-line utility written in Java for finding and managing duplicate files in directories using SHA-256 hash-based comparison.

## Features

- **Recursive Directory Scanning** - Traverses all subdirectories
- **Hash-Based Comparison** - Uses SHA-256 for accurate duplicate detection
- **Detailed Reporting** - Groups duplicates and shows file paths
- **Space Analysis** - Calculates total wasted storage space
- **Optional Deletion** - Safely removes duplicates (keeps first occurrence)
- **Human-Readable Output** - File sizes formatted in B/KB/MB/GB

## Compilation
```bash
javac -d bin src/main/java/com/dedup/*.java
```

## Usage

**List duplicates only:**
```bash
java -cp bin com.dedup.FileDedupTool /path/to/directory
```

**List and delete duplicates:**
```bash
java -cp bin com.dedup.FileDedupTool /path/to/directory --delete
```

## Example Output
```
Scanning directory: /home/user/Documents
This may take a while...

Found 1523 files

Processing 1523 files...

===== DUPLICATE FILES FOUND =====

Group 1 (3 copies, 2.45 MB each):
  - /home/user/Documents/photo.jpg
  - /home/user/Documents/backup/photo.jpg
  - /home/user/Downloads/photo.jpg

Group 2 (2 copies, 156.78 KB each):
  - /home/user/Documents/report.pdf
  - /home/user/Documents/old/report.pdf

===== SUMMARY =====
Total duplicate groups: 2
Wasted space: 5.06 MB
```

## How It Works

1. **FileScanner** - Recursively traverses the directory tree
2. **HashCalculator** - Computes SHA-256 hash for each file
3. **DuplicateFinder** - Groups files by hash and identifies duplicates
4. **ReportGenerator** - Formats and displays results

## Implementation Details

- **Hash Algorithm**: SHA-256 for collision resistance
- **Buffer Size**: 8KB for efficient file reading
- **Deletion Safety**: Always keeps the first file in each duplicate group

## Requirements

- Java 8 or higher
- Read permissions for target directory
- Write permissions if using `--delete` flag