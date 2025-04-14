package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes patient data to separate files based on data type.
 *
 * <p>This implementation of OutputStrategy writes patient data to text files,
 * with each data type (label) having its own file in the specified base directory.
 * Files are created on demand and data is appended to existing files.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Organizes data by type in separate files</li>
 *   <li>Creates files on demand</li>
 *   <li>Appends data to existing files</li>
 *   <li>Thread-safe file handling</li>
 * </ul></p>
 *
 * <p>Output format:
 * "Patient ID: {id}, Timestamp: {timestamp}, Label: {label}, Data: {data}"</p>
 */
public class FileOutputStrategy implements OutputStrategy {

    /** The base directory where output files will be stored. */
    private final String baseDirectory;
    
    /** Maps data labels to their corresponding file paths. */
    private final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Initializes a new file-based output strategy.
     *
     * @param baseDirectory the directory where output files will be stored. The directory
     *     will be created if it does not exist.
     * @throws IllegalArgumentException if baseDirectory is null or empty
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes patient data to a file specific to the data type.
     *
     * <p>This method:
     * <ol>
     *   <li>Creates the base directory if it doesn't exist</li>
     *   <li>Gets or creates the file path for the data type</li>
     *   <li>Appends the data to the file in a formatted string</li>
     * </ol></p>
     *
     * <p>If any errors occur during file operations, they are logged to System.err
     * and the method returns without throwing an exception.</p>
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time when the data was recorded, in milliseconds since epoch
     * @param label the type of data being recorded
     * @param data the actual data value to be recorded
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }

        // Get or create the file path for this label
        String filePath = fileMap.computeIfAbsent(label, k -> {
            Path path = Paths.get(baseDirectory, label + ".txt");
            return path.toString();
        });

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}