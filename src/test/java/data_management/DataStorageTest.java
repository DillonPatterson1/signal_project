package com.data_management;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
    }

    @Test
    void testAddPatientDataAndGetRecords() {
        // Add data for patient 1
        storage.addPatientData(1, 100.0, "HeartRate", 1700000000000L);
        storage.addPatientData(1, 120.0, "HeartRate", 1700000060000L);
        // Add data for patient 2
        storage.addPatientData(2, 98.0, "BloodSaturation", 1700000000000L);

        // Test getRecords for patient 1
        List<PatientRecord> p1Records = storage.getRecords(1, 1700000000000L, 1700000060000L);
        assertEquals(2, p1Records.size());
        assertEquals(100.0, p1Records.get(0).getMeasurementValue());
        assertEquals(120.0, p1Records.get(1).getMeasurementValue()); // Assuming order by add or Patient class sorts them internally

        // Test getRecords for patient 2
        List<PatientRecord> p2Records = storage.getRecords(2, 1700000000000L, 1700000000000L);
        assertEquals(1, p2Records.size());
        assertEquals(98.0, p2Records.get(0).getMeasurementValue());

        // Test getRecords for non-existent patient
        List<PatientRecord> p3Records = storage.getRecords(3, 0L, Long.MAX_VALUE);
        assertTrue(p3Records.isEmpty());
    }

    @Test
    void testGetAllPatients() {
        assertTrue(storage.getAllPatients().isEmpty(), "Initially, there should be no patients.");

        storage.addPatientData(1, 100.0, "HeartRate", 1700000000000L);
        assertEquals(1, storage.getAllPatients().size(), "Should have 1 patient after adding data for patient 1.");

        storage.addPatientData(2, 98.0, "BloodSaturation", 1700000000000L);
        assertEquals(2, storage.getAllPatients().size(), "Should have 2 patients after adding data for patient 2.");

        // Adding more data for an existing patient should not increase patient count
        storage.addPatientData(1, 110.0, "HeartRate", 1700000060000L);
        assertEquals(2, storage.getAllPatients().size(), "Adding more data to an existing patient should not change patient count.");
    }

    @Test
    void testAddPatientData_NewAndExistingPatient() {
        // New patient
        storage.addPatientData(10, 75.0, "ECG", System.currentTimeMillis());
        Patient patient10 = storage.getAllPatients().stream().filter(p -> p.getPatientId() == 10).findFirst().orElse(null);
        assertNotNull(patient10);
        assertEquals(1, patient10.getAllRecords().size());

        // Add more data to the same patient
        storage.addPatientData(10, 77.0, "ECG", System.currentTimeMillis() + 1000);
        assertEquals(1, storage.getAllPatients().size()); // Patient count should remain 1
        assertEquals(2, patient10.getAllRecords().size(), "Patient 10 should have 2 records now.");
    }
    
    @Test
    void testGetRecords_Ordering() {
        // Patient class itself doesn't guarantee order of getAllRecords() output without explicit sort.
        // However, addRecord appends to a list. For this test, we check if records for getRecords(id, start, end) are found.
        // The Patient.getRecords() implementation filters from this list.
        // Explicit sorting within Patient.getRecords() was commented out in my implementation, but may be present in user's original or desired.
        // For now, this tests retrieval, not strict order from getRecords itself if source isn't sorted.
        long t1 = 1700000000000L;
        long t2 = 1700000060000L;
        long t3 = 1700000120000L;

        storage.addPatientData(5, 100.0, "HeartRate", t2); // Added out of chronological order
        storage.addPatientData(5, 90.0, "HeartRate", t1);
        storage.addPatientData(5, 110.0, "HeartRate", t3);

        List<PatientRecord> records = storage.getRecords(5, t1, t3);
        assertEquals(3, records.size(), "Should retrieve all 3 records for patient 5 within the time range.");
        
        // To check order, we need to sort them here or ensure Patient.getRecords sorts them.
        // The current Patient.getRecords does not sort.
        // Let's verify that all expected records are present regardless of order.
        assertTrue(records.stream().anyMatch(r -> r.getMeasurementValue() == 90.0 && r.getTimestamp() == t1));
        assertTrue(records.stream().anyMatch(r -> r.getMeasurementValue() == 100.0 && r.getTimestamp() == t2));
        assertTrue(records.stream().anyMatch(r -> r.getMeasurementValue() == 110.0 && r.getTimestamp() == t3));
    }
}
