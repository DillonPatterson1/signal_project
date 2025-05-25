package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class FileDataReader implements DataReader {

    private String outputDirectory;

    public FileDataReader(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path directoryPath = Paths.get(outputDirectory);
        List<File> files = Files.list(directoryPath)
                                .filter(Files::isRegularFile)
                                .map(Path::toFile)
                                .collect(Collectors.toList());

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseAndStoreData(line, dataStorage);
                }
            }
        }
    }

    private void parseAndStoreData(String line, DataStorage dataStorage) {
        try {
            String[] parts = line.split(", ");
            if (parts.length < 4) return;

            String patientIdStr = parts[0].split(": ")[1];
            String timestampStr = parts[1].split(": ")[1];
            String label = parts[2].split(": ")[1];
            String dataStr = parts[3].split(": ")[1];

            int patientId = Integer.parseInt(patientIdStr);
            long timestamp = Long.parseLong(timestampStr);

            if ("BloodPressure".equals(label)) {
                String[] bpValues = dataStr.split("/");
                if (bpValues.length == 2) {
                    double systolic = Double.parseDouble(bpValues[0]);
                    double diastolic = Double.parseDouble(bpValues[1]);

                    dataStorage.addPatientData(patientId, systolic, label + "Systolic", timestamp);
                    dataStorage.addPatientData(patientId, diastolic, label + "Diastolic", timestamp);
                }
            } else {
                double measurementValue = Double.parseDouble(dataStr);
                dataStorage.addPatientData(patientId, measurementValue, label, timestamp);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
        }
    }
} 