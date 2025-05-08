# Attempt to Detect Unused Java Classes
This simply scans a directory (and all subdirectories) for .java files, and given
the filename, scans all other .java files for lines that start with 'import' and
also contain the filename.

This program is meant to give you a starting point for identifying potentially
unused java classes. Use discretion before deleting any classes that are identified
by this program.

# How to Run
```
javac FileReferenceFinder.java
java FileReferenceFinder project_root_directory
```
