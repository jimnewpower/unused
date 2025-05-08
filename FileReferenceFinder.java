import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileReferenceFinder {
    // Define the set of file extensions to search
    private static final Set<String> VALID_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".java"
    ));

    public static void main(String[] args) {
        // Check if project root directory is provided
        if (args.length != 1) {
            System.out.println("Usage: java FileReferenceFinder <project_root_directory>");
            return;
        }

        String projectRoot = args[0];
        File rootDir = new File(projectRoot);

        // Validate directory
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory: " + projectRoot);
            return;
        }

        try {
            // Convert root directory to Path for relative path calculations
            Path rootPath = rootDir.toPath().toAbsolutePath().normalize();
            
            // Get all files with valid extensions in the project
            List<File> allFiles = collectFiles(rootDir);
            
            if (allFiles.isEmpty()) {
                System.out.println("No files with extensions " + VALID_EXTENSIONS + " found in the project.");
                return;
            }

            // Process each file
            for (File targetFile : allFiles) {
                String fileName = targetFile.getName();
                if (!fileName.endsWith(".java") || !fileName.endsWith("Test.java")) {
                    continue;
                }

                // Get relative path of the target file
                Path targetPath = targetFile.toPath().toAbsolutePath().normalize();
                String relativePath = rootPath.relativize(targetPath).toString();
                
                // Strip off the extension
                String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.')); 
                // System.out.println("\nSearching for references to: " + fileNameWithoutExtension + " (" + relativePath + ")");
                
                // Search for references in all other files with valid extensions
                boolean foundReference = false;
                for (File searchFile : allFiles) {
                    // Skip the file itself
                    if (searchFile.equals(targetFile)) {
                        continue;
                    }
                    
                    // Search for the filename (without extension) in the content of searchFile
                    if (searchFileInContent(fileNameWithoutExtension, searchFile)) {
                        foundReference = true;
                        // System.out.println("  Found in: " + rootPath.relativize(searchFile.toPath().toAbsolutePath().normalize()));
                    }
                }
                
                if (!foundReference) {
                    System.out.println("No imports found for: " + relativePath);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    // Recursively collect all files with valid extensions in the directory
    private static List<File> collectFiles(File directory) throws IOException {
        List<File> fileList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : stream) {
                File file = path.toFile();
                if (file.isDirectory()) {
                    fileList.addAll(collectFiles(file));
                } else {
                    // Check if the file has a valid extension
                    String fileName = file.getName().toLowerCase();
                    if (VALID_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }

    // Search for a filename in the content of a file
    private static boolean searchFileInContent(String fileName, File searchFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(searchFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("import") && line.contains(fileName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + searchFile.getAbsolutePath() + ": " + e.getMessage());
        }
        return false;
    }
}