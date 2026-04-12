# 🔍 File Deduplication Tool

> A Java CLI tool that finds and eliminates duplicate files using SHA-256 content hashing — byte-perfect detection, recursive directory scanning, wasted-space analysis, and safe deletion. Zero external dependencies.

![Java](https://img.shields.io/badge/Java-8+-orange?style=flat-square&logo=openjdk)
![Algorithm](https://img.shields.io/badge/Hashing-SHA--256-blue?style=flat-square)
![Dependencies](https://img.shields.io/badge/Dependencies-none-brightgreen?style=flat-square)
![I/O](https://img.shields.io/badge/I%2FO-8KB_buffered_streaming-lightgrey?style=flat-square)

---

## The Problem

File systems accumulate duplicates quietly. A photo gets synced, backed up, and downloaded again. A report gets copied into three different project folders. A dataset gets downloaded twice because nobody remembered the first time. Over years, on a machine with multiple users or an aggressive backup strategy, this adds up to gigabytes of wasted space — all of it invisible because the files have different names or live in different directories.

The naive fix is manual: open two folders side by side, squint, compare file sizes, give up. The wrong fix is comparing filenames — duplicates rarely share the same name, and files with the same name are often different versions. The right fix is comparing file *content*, cryptographically, so there are no false positives and no missed matches. That's what this tool does.

---

## How It Works

Every file gets reduced to a 64-character SHA-256 fingerprint. Files that share a fingerprint share identical content — regardless of name, path, or timestamp. The tool collects all fingerprints, groups files by them, and surfaces every group with more than one member as a duplicate set.

```
┌─────────────────────────────────────────────────────────────────┐
│                         INPUT                                   │
│              java FileDedupTool /path/to/dir                    │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. FileScanner                                                 │
│     Recursive depth-first walk → List<File>                     │
│     Skips directories, follows the tree until leaves            │
└────────────────────────┬────────────────────────────────────────┘
                         │  List of every file in the tree
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. HashCalculator                                              │
│     FileInputStream → 8KB chunks → SHA-256 MessageDigest       │
│     File → hex string fingerprint                               │
└────────────────────────┬────────────────────────────────────────┘
                         │  Map<filepath, sha256_hex>
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. DuplicateFinder                                             │
│     HashMap<hash, List<File>> grouping                          │
│     Filter: keep only groups where size > 1                     │
│     Calculate: wasted_space = file_size × (copies - 1)         │
└────────────────────────┬────────────────────────────────────────┘
                         │  Map<hash, List<File>> duplicates only
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. ReportGenerator                                             │
│     Format groups with full paths and sizes                     │
│     Print summary: groups found, total wasted space             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  5. Deletion (optional, --delete flag)                          │
│     Keep index[0] of each group (first occurrence found)       │
│     File.delete() on index[1..n]                                │
│     Report success/failure per file                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Features

### 🔐 SHA-256 Content Hashing — No False Positives, No False Negatives

Two files are duplicates if and only if they have identical SHA-256 hashes. SHA-256 produces a 256-bit fingerprint with no known collision attacks — meaning there is no known pair of different files that produce the same hash. This matters for a deletion tool. A false positive (marking two different files as duplicates) could cause data loss. MD5 and SHA-1 have documented collision vulnerabilities. SHA-256 does not.

**Under the hood:** `HashCalculator` uses Java's built-in `MessageDigest.getInstance("SHA-256")`. The file is read in 8KB chunks, each fed to the digest with `update()`, and the final hash is retrieved with `digest()`. The binary result is converted to a lowercase hex string via a byte-by-byte format loop — the 64-character string you'd recognize from any `sha256sum` output.

```java
// The core of byte-perfect duplicate detection
MessageDigest digest = MessageDigest.getInstance("SHA-256");
try (FileInputStream fis = new FileInputStream(file)) {
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = fis.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
    }
}
return bytesToHex(digest.digest());
```

---

### 📁 Recursive Directory Scanning

The scanner traverses the full directory tree depth-first, collecting every file it finds. Pass it `/home/user` and it will reach files nested ten directories deep. It handles the full tree in a single pass — no need to invoke it per-subdirectory.

**Under the hood:** `FileScanner.scanRecursive()` uses `File.listFiles()` to get the direct children of a directory. For each child: if it's a file, add it to the result list; if it's a directory, recurse. `listFiles()` returns `null` when the process lacks read permissions on a directory — the null check silently skips those branches rather than crashing, which is the right behavior when scanning system directories with mixed permissions.

---

### 📊 Wasted Space Analysis

The report doesn't just list duplicates — it tells you exactly how much disk space you're wasting, per group and in total. A group of three 50MB video files means 100MB wasted. You see that number before you decide whether to delete.

**Under the hood:** `DuplicateFinder.calculateWastedSpace()` iterates over each duplicate group and applies `file.length() × (group.size() - 1)`. One copy is the "original"; every additional copy is waste. Sizes are formatted into human-readable units (B, KB, MB, GB) with two decimal places using a simple threshold comparison — no format library needed.

---

### 🗑️ Safe Deletion with Explicit Opt-In

Scanning is always safe and non-destructive. Deletion only happens when you explicitly pass `--delete`. When it does, the tool keeps the first file in each duplicate group and removes the rest — and reports exactly which files were deleted and which deletions failed.

**Under the hood:** The deletion loop in `FileDedupTool` starts at index `1` of each sorted group list, calling `File.delete()` on every entry from the second copy onwards. Index `0` is never touched. Each deletion prints its outcome. There's no move-to-trash, no undo — `File.delete()` is permanent. The `--delete` flag requirement is the safety gate.

---

## Example Output

```
Scanning directory: /home/user/Documents
Found 1,523 files
Processing 1,523 files...

===== DUPLICATE FILES FOUND =====

Group 1 (3 copies, 2.45 MB each):
  - /home/user/Documents/photos/vacation.jpg        ← kept
  - /home/user/Documents/backup/vacation.jpg        ← duplicate
  - /home/user/Downloads/vacation.jpg               ← duplicate

Group 2 (2 copies, 0.38 MB each):
  - /home/user/Documents/reports/Q3_final.pdf       ← kept
  - /home/user/Documents/reports/archive/Q3_final.pdf ← duplicate

===== SUMMARY =====
Total duplicate groups: 2
Total wasted space: 5.06 MB
```

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 8+ | Strong typing makes the data flow explicit; JVM's `MessageDigest` and I/O stack are battle-tested; compiles to a single set of `.class` files with no runtime beyond a JDK |
| Hashing | SHA-256 (`java.security`) | No known collision attacks — critical for a deletion tool where false positives cause data loss. MD5 and SHA-1 are ruled out. Available in the JDK standard library with no added dependency |
| I/O | `FileInputStream` + 8KB buffer | 8,192 bytes matches typical OS I/O buffer alignment. Streaming in chunks means a 50GB file uses the same 8KB of heap as a 1KB file — constant memory regardless of what you scan |
| Collections | `HashMap<String, List<File>>` | Hash-keyed grouping gives O(1) average-case insertion and lookup. Building the duplicate map is a single linear pass over all files |
| Build | `javac` (no build tool) | Zero toolchain overhead. No Maven wrapper, no Gradle daemon, no plugin ecosystem to maintain. Any JDK installation can compile and run it in two commands |

---

## Running It

**Requirements:** Java 8 or later. Read permissions on the target directory; write permissions if using `--delete`.

**Compile:**
```bash
javac -d bin src/main/java/com/dedup/*.java
```

**Scan a directory (safe, read-only):**
```bash
java -cp bin com.dedup.FileDedupTool /path/to/directory
```

**Scan and delete duplicates:**
```bash
java -cp bin com.dedup.FileDedupTool /path/to/directory --delete
```

**Practical examples:**
```bash
# Find duplicates in your Downloads folder
java -cp bin com.dedup.FileDedupTool ~/Downloads

# Clean up a photo library backup
java -cp bin com.dedup.FileDedupTool ~/Pictures --delete

# Scan an entire home directory (read-only, see what's there first)
java -cp bin com.dedup.FileDedupTool /home/user
```

---

## Engineering Concepts Demonstrated

### Hash-Based Grouping
The duplicate detection problem reduces to a grouping problem: given N files, find all subsets with identical content. The naive approach — comparing every file against every other — is O(N²) in comparisons and O(N²) in I/O reads. The hash-based approach makes it O(N): one read per file, one hash per file, one HashMap insertion per file. Identical content produces identical keys, so grouping happens automatically as the map is built. This is the same principle behind hash joins in databases and deduplication in storage systems like ZFS.

### Streaming I/O for Constant Memory
A dedup tool that loads files into memory before hashing would fail on any directory containing files larger than available RAM. This tool uses streaming reads: the `MessageDigest.update()` API accepts chunks incrementally, so the hash state is maintained internally while the file is read in 8KB windows. Peak memory usage is bounded by the buffer size (8KB), not the file size. This is the same pattern used by checksum utilities like `sha256sum`, archive tools, and any production file processor.

### Cryptographic Hashing as an Engineering Choice
Choosing SHA-256 over MD5 or a simple CRC is a deliberate correctness decision, not overcaution. CRCs catch accidental corruption but are trivially collided. MD5 has published collision attacks — two crafted files can share an MD5 hash, which would cause this tool to mark them as duplicates and delete one. SHA-256 has no known collisions. For a tool whose failure mode is permanent data deletion, the hash algorithm is a safety property, not just a performance tuning knob.

### Pipeline Architecture with Single Responsibility
The five-class design (`FileScanner` → `HashCalculator` → `DuplicateFinder` → `ReportGenerator` → deletion in `FileDedupTool`) follows a strict pipeline: each class takes the output of the previous stage and produces the input for the next. No class knows about the others' internals. This means each component is independently testable, swappable, and understandable in isolation. You could replace `HashCalculator` with an MD5 implementation, or replace `ReportGenerator` with a JSON formatter, without touching anything else. The same pattern appears in Unix pipes, ETL systems, and compiler front-ends.

### Graceful Degradation on Permission Errors
File systems in the wild have directories the process can't read — system directories, other users' home folders, protected volumes. `File.listFiles()` returns `null` rather than throwing when permissions are denied. The null check in `FileScanner` silently skips those branches and continues scanning what it can, rather than crashing the entire run because one directory was inaccessible. This is the difference between a tool that works on real machines and one that only works in clean demo environments.

---

## Project Structure

```
file-dedup-tool/
└── src/main/java/com/dedup/
    ├── FileDedupTool.java    # Entry point: CLI argument parsing, orchestration, deletion loop
    ├── FileScanner.java      # Recursive directory walker → List<File>
    ├── HashCalculator.java   # SHA-256 streaming hasher → hex string fingerprint
    ├── DuplicateFinder.java  # HashMap grouping, duplicate filtering, wasted space calculation
    └── ReportGenerator.java  # Human-readable output formatting
```

Five classes, one responsibility each, no external dependencies. The kind of codebase where you can understand the full system by reading one file at a time.

---

*Built in Java because the standard library's cryptographic primitives and I/O stack are production-grade, strongly typed, and need no third-party wrapping. Sometimes the right tool for a focused problem is a focused implementation.*
