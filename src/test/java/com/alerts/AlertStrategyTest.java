package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AlertStrategyTest {
    private Patient patient;
    private List<PatientRecord> records;
    private AlertFactory alertFactory;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
        records = new ArrayList<>();
        alertFactory = new BloodPressureAlertFactory();
    }

    @Test
    void testBloodPressureStrategy() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        records.add(new PatientRecord(1, 190.0, "BloodPressureSystolic", System.currentTimeMillis()));
        records.add(new PatientRecord(1, 50.0, "BloodPressureDiastolic", System.currentTimeMillis()));
        strategy.checkAlert(patient, records, alertFactory);
    }

    @Test
    void testBloodOxygenStrategy() {
        BloodOxygenStrategy strategy = new BloodOxygenStrategy();
        records.add(new PatientRecord(1, 90.0, "BloodSaturation", System.currentTimeMillis()));
        strategy.checkAlert(patient, records, alertFactory);
    }

    @Test
    void testECGStrategy() {
        ECGStrategy strategy = new ECGStrategy();
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(1, 1.0, "ECG", System.currentTimeMillis()));
        }
        records.add(new PatientRecord(1, 5.0, "ECG", System.currentTimeMillis()));
        strategy.checkAlert(patient, records, alertFactory);
    }
} 