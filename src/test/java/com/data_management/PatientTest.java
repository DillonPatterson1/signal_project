package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
        // Add some sample records for testing
        patient.addRecord(100.0, "HeartRate", 1700000000000L); // Timestamp 1
        patient.addRecord(70.0, "HeartRate", 1700000060000L);  // Timestamp 2 (1 min later)
        patient.addRecord(120.0, "BloodPressureSystolic", 1700000000000L);
        patient.addRecord(80.0, "BloodPressureDiastolic", 1700000000000L);
        patient.addRecord(98.0, "BloodSaturation", 1700000120000L); // Timestamp 3 (2 mins after T1)
        patient.addRecord(75.0, "HeartRate", 1700000180000L);  // Timestamp 4 (3 mins after T1)
    }

    @Test
    void testAddRecordAndGetAllRecords() {
        List<PatientRecord> records = patient.getAllRecords();
        assertNotNull(records);
        assertEquals(4, records.size(), "Should retrieve all 4 added records initially (BP counts as 2 conceptually, but added as 2 separate record types).");
        
        // Let's refine based on how addRecord was called in setup.
        // We added 6 records in setup based on individual calls.
        patient = new Patient(2); // Fresh patient
        patient.addRecord(100.0, "HeartRate", 1700000000000L);
        patient.addRecord(70.0, "HeartRate", 1700000060000L);
        patient.addRecord(120.0, "BloodPressureSystolic", 1700000000000L);
        patient.addRecord(80.0, "BloodPressureDiastolic", 1700000000000L);
        patient.addRecord(98.0, "BloodSaturation", 1700000120000L);
        patient.addRecord(75.0, "HeartRate", 1700000180000L);
        
        records = patient.getAllRecords();
        assertNotNull(records);
        assertEquals(6, records.size(), "Should retrieve all 6 added records.");
    }

    @Test
    void testGetRecordsWithinTimeRange() {
        // Test case 1: Range includes two HeartRate records and the saturation record
        List<PatientRecord> records = patient.getRecords(1700000000000L, 1700000120000L);
        assertEquals(4, records.size(), "Should retrieve 4 records within the specified time range.");
        // Check types if necessary, e.g. ensure one is Saturation and two are HeartRate, one systolic, one diastolic.

        // Test case 2: Range includes only the last HeartRate record
        records = patient.getRecords(1700000150000L, 1700000200000L);
        assertEquals(1, records.size(), "Should retrieve 1 record (the last HeartRate).");
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(75.0, records.get(0).getMeasurementValue());

        // Test case 3: Range includes no records (before all records)
        records = patient.getRecords(1600000000000L, 1699999999999L);
        assertTrue(records.isEmpty(), "Should retrieve no records before all existing records.");

        // Test case 4: Range includes no records (after all records)
        records = patient.getRecords(1800000000000L, 1900000000000L);
        assertTrue(records.isEmpty(), "Should retrieve no records after all existing records.");

        // Test case 5: Exact timestamp match for one record
        records = patient.getRecords(1700000120000L, 1700000120000L);
        assertEquals(1, records.size(), "Should retrieve 1 record with exact timestamp match.");
        assertEquals("BloodSaturation", records.get(0).getRecordType());

        // Test case 6: Range covering all records
        records = patient.getRecords(1700000000000L, 1700000180000L);
        assertEquals(6, records.size(), "Should retrieve all 6 records when range covers all.");
    }

    @Test
    void testGetRecordsWithEmptyPatientRecords() {
        Patient emptyPatient = new Patient(2);
        List<PatientRecord> records = emptyPatient.getRecords(0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty(), "getRecords should return an empty list for a patient with no records.");
        records = emptyPatient.getAllRecords();
        assertTrue(records.isEmpty(), "getAllRecords should return an empty list for a patient with no records.");
    }
} 