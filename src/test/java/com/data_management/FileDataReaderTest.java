package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FileDataReaderTest {

    @TempDir
    Path tempDir;

    private DataStorage dataStorage;
    private FileDataReader fileDataReader;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage();
        fileDataReader = new FileDataReader(tempDir.toString());
    }

    @Test
    void testReadData_ValidFiles() throws IOException {

        Path file1 = tempDir.resolve("HeartRate.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(file1)) {
            writer.write("Patient ID: 1, Timestamp: 1700000000000, Label: HeartRate, Data: 75.0\n");
            writer.write("Patient ID: 1, Timestamp: 1700000060000, Label: HeartRate, Data: 78.0\n");
            writer.write("Patient ID: 2, Timestamp: 1700000000000, Label: HeartRate, Data: 60.0\n");
        }

        Path file2 = tempDir.resolve("BloodPressure.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(file2)) {
            writer.write("Patient ID: 1, Timestamp: 1700000000000, Label: BloodPressure, Data: 120/80\n");
        }
        
        Path file3 = tempDir.resolve("BloodSaturation.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(file3)) {
            writer.write("Patient ID: 2, Timestamp: 1700000120000, Label: BloodSaturation, Data: 95.0\n");
        }

        fileDataReader.readData(dataStorage);

        List<Patient> patients = dataStorage.getAllPatients();
        assertEquals(2, patients.size(), "Should have read data for 2 patients.");

        Patient patient1 = dataStorage.getAllPatients().stream().filter(p -> p.getPatientId() == 1).findFirst().orElse(null);
        assertNotNull(patient1);

        assertEquals(4, patient1.getAllRecords().size(), "Patient 1 should have 4 records (2 HR, 1 BP_Sys, 1 BP_Dia).");

        assertTrue(patient1.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("HeartRate") && r.getMeasurementValue() == 78.0 && r.getTimestamp() == 1700000060000L));

         assertTrue(patient1.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("BloodPressureSystolic") && r.getMeasurementValue() == 120.0));
        assertTrue(patient1.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("BloodPressureDiastolic") && r.getMeasurementValue() == 80.0));

        Patient patient2 = dataStorage.getAllPatients().stream().filter(p -> p.getPatientId() == 2).findFirst().orElse(null);
        assertNotNull(patient2);

        assertEquals(2, patient2.getAllRecords().size(), "Patient 2 should have 2 records (1 HR, 1 Saturation).");
        assertTrue(patient2.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("BloodSaturation") && r.getMeasurementValue() == 95.0));
    }

    @Test
    void testReadData_EmptyDirectory() throws IOException {
        fileDataReader.readData(dataStorage);
        assertTrue(dataStorage.getAllPatients().isEmpty(), "DataStorage should be empty if directory has no files.");
    }

    @Test
    void testReadData_MalformedFile() throws IOException {
        Path malformedFile = tempDir.resolve("malformed.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(malformedFile)) {
            writer.write("Patient ID: 1, Timestamp: 1700000000000, Label: HeartRate, Data: 75.0\n"); // Correct
            writer.write("This is a malformed line\n");
            writer.write("Patient ID: 2, Timestamp: 1700000060000, Label: HeartRate, Data: 80.0\n"); // Correct
        }

        fileDataReader.readData(dataStorage);

        assertEquals(2, dataStorage.getAllPatients().size(), "Should parse valid lines and skip malformed ones.");
        Patient patient1 = dataStorage.getAllPatients().stream().filter(p -> p.getPatientId() == 1).findFirst().orElse(null);
        assertNotNull(patient1);
        assertEquals(1, patient1.getAllRecords().size());

        Patient patient2 = dataStorage.getAllPatients().stream().filter(p -> p.getPatientId() == 2).findFirst().orElse(null);
        assertNotNull(patient2);
        assertEquals(1, patient2.getAllRecords().size());
    }

    @Test
    void testReadData_EmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        fileDataReader.readData(dataStorage);
        assertTrue(dataStorage.getAllPatients().isEmpty(), "DataStorage should be empty if only an empty file is present.");
    }

     @Test
    void testReadData_BloodPressureParsing() throws IOException {
        Path bpFile = tempDir.resolve("bp_specific.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(bpFile)) {
            writer.write("Patient ID: 3, Timestamp: 1700000000000, Label: BloodPressure, Data: 130/85\n");
            writer.write("Patient ID: 3, Timestamp: 1700000000000, Label: BloodPressure, Data: invalid\n"); // Invalid BP data
             writer.write("Patient ID: 3, Timestamp: 1700000000000, Label: BloodPressure, Data: 140/\n"); // Invalid BP data
        }

        fileDataReader.readData(dataStorage);
        Patient patient3 = dataStorage.getAllPatients().stream().filter(p -> p.getPatientId() == 3).findFirst().orElse(null);
        assertNotNull(patient3, "Patient 3 should exist.");

        assertEquals(2, patient3.getAllRecords().size(), "Patient 3 should have 2 records from the valid BP entry.");
        assertTrue(patient3.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("BloodPressureSystolic") && r.getMeasurementValue() == 130.0));
        assertTrue(patient3.getAllRecords().stream()
            .anyMatch(r -> r.getRecordType().equals("BloodPressureDiastolic") && r.getMeasurementValue() == 85.0));
    }
} 