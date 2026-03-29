# 🔍 File Deduplication Tool

> Find and eliminate duplicate files using SHA-256 hashing. Built in Java.

![Java](https://img.shields.io/badge/Java-8+-orange?style=flat-square&logo=java)
![Algorithm](https://img.shields.io/badge/Algorithm-SHA--256-blue?style=flat-square)

---

## The Problem

Duplicate files silently eat your storage. Photos backed up twice, reports saved in multiple folders, downloads you forgot you already had. This tool finds them all in seconds.

---

## Features

- 🔐 **SHA-256 Hashing** — byte-perfect duplicate detection, no false positives
- 📁 **Recursive Scanning** — traverses entire directory trees
- 📊 **Space Analysis** — shows exactly how much storage is wasted
- 🗑️ **Safe Deletion** — removes duplicates, always keeps the original
- 📋 **Detailed Report** — groups duplicates with full file paths

---

## Example Output
```
Scanning directory: /home/user/Documents
Found 1523 files

===== DUPLICATE FILES FOUND =====

Group 1 (3 copies, 2.45 MB each):
  - /home/user/Documents/photo.jpg        ← kept
  - /home/user/Documents/backup/photo.jpg ← duplicate
  - /home/user/Downloads/photo.jpg        ← duplicate

===== SUMMARY =====
Total duplicate groups: 2
Wasted space: 5.06 MB
```

---

## Usage
```bash
# Compile
javac -d bin src/main/java/com/dedup/*.java

# Find duplicates (list only)
java -cp bin com.dedup.FileDedupTool /path/to/directory

# Find and delete duplicates
java -cp bin com.dedup.FileDedupTool /path/to/directory --delete
```

---

## How It Works
```
Directory → FileScanner → HashCalculator (SHA-256) → DuplicateFinder → Report
```

1. **FileScanner** — recursively walks the directory tree
2. **HashCalculator** — computes SHA-256 for every file (8KB buffer for efficiency)
3. **DuplicateFinder** — groups files by hash, flags duplicates
4. **ReportGenerator** — formats results with space savings

---

## Tech Stack

- **Language:** Java 8+
- **Hash Algorithm:** SHA-256
- **I/O:** Buffered file reading (8KB chunks)

---

## Requirements

- Java 8+
- Read permissions on target directory
- Write permissions if using `--delete`

---

*Demonstrates: file I/O, cryptographic hashing, recursive algorithms, CLI tool design.*
