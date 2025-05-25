package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.alerts.AlertGenerator;

import java.util.List;

class AlertGeneratorTest {

    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private Patient patient;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage(); // Can be a mock or real, depending on test focus
        alertGenerator = new AlertGenerator(dataStorage);
        patient = new Patient(1);
        alertGenerator.clearTriggeredAlerts(); // Ensure a clean slate for each test
    }

    // --- Blood Pressure Alerts Tests ---
    @Test
    void testBPCriticalSystolicHigh() {
        patient.addRecord(181.0, "BloodPressureSystolic", System.currentTimeMillis());
        patient.addRecord(80.0, "BloodPressureDiastolic", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Critical Systolic High"));
    }

    @Test
    void testBPCriticalSystolicLow() {
        patient.addRecord(89.0, "BloodPressureSystolic", System.currentTimeMillis());
        patient.addRecord(80.0, "BloodPressureDiastolic", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Critical Systolic Low"));
    }

    @Test
    void testBPCriticalDiastolicHigh() {
        patient.addRecord(120.0, "BloodPressureSystolic", System.currentTimeMillis());
        patient.addRecord(121.0, "BloodPressureDiastolic", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Critical Diastolic High"));
    }

    @Test
    void testBPCriticalDiastolicLow() {
        patient.addRecord(120.0, "BloodPressureSystolic", System.currentTimeMillis());
        patient.addRecord(59.0, "BloodPressureDiastolic", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Critical Diastolic Low"));
    }

    @Test
    void testBPSystolicIncreasingTrend() {
        long time = System.currentTimeMillis();
        patient.addRecord(100.0, "BloodPressureSystolic", time);
        patient.addRecord(115.0, "BloodPressureSystolic", time + 1000);
        patient.addRecord(130.0, "BloodPressureSystolic", time + 2000);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Systolic Increasing Trend"));
    }

    @Test
    void testBPSystolicDecreasingTrend() {
        long time = System.currentTimeMillis();
        patient.addRecord(130.0, "BloodPressureSystolic", time);
        patient.addRecord(115.0, "BloodPressureSystolic", time + 1000);
        patient.addRecord(100.0, "BloodPressureSystolic", time + 2000); // Diff is -15, then -15. Both < -10
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Systolic Decreasing Trend"));
    }
    
    @Test
    void testBPDiastolicIncreasingTrend() {
        long time = System.currentTimeMillis();
        patient.addRecord(70.0, "BloodPressureDiastolic", time);
        patient.addRecord(85.0, "BloodPressureDiastolic", time + 1000);
        patient.addRecord(100.0, "BloodPressureDiastolic", time + 2000);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Diastolic Increasing Trend"));
    }

    @Test
    void testBPDiastolicDecreasingTrend() {
        long time = System.currentTimeMillis();
        patient.addRecord(100.0, "BloodPressureDiastolic", time);
        patient.addRecord(85.0, "BloodPressureDiastolic", time + 1000);
        patient.addRecord(70.0, "BloodPressureDiastolic", time + 2000);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Diastolic Decreasing Trend"));
    }

    @Test
    void testBPTrend_NotEnoughChange() {
        long time = System.currentTimeMillis();
        patient.addRecord(100.0, "BloodPressureSystolic", time);
        patient.addRecord(105.0, "BloodPressureSystolic", time + 1000); // Change is 5 (not > 10)
        patient.addRecord(110.0, "BloodPressureSystolic", time + 2000); // Change is 5 (not > 10)
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty());
    }

    // --- Blood Saturation Alerts Tests ---
    @Test
    void testSaturationLow() {
        patient.addRecord(91.0, "BloodSaturation", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Low Blood Saturation"));
    }

    @Test
    void testSaturationRapidDrop() {
        long currentTime = System.currentTimeMillis();
        patient.addRecord(98.0, "BloodSaturation", currentTime - (10 * 60 * 1000) + 1000); // 9 mins 59 secs ago
        patient.addRecord(92.0, "BloodSaturation", currentTime); // Drops by 6%
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size()); // Should be 1 for rapid drop. Potentially also low if < 92.
                                        // Current setup: 92 is not < 92, so only rapid drop.
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Rapid Blood Saturation Drop")));
    }

    @Test
    void testSaturationRapidDrop_And_Low() {
        long currentTime = System.currentTimeMillis();
        patient.addRecord(97.0, "BloodSaturation", currentTime - (5 * 60 * 1000)); // 5 mins ago
        patient.addRecord(91.0, "BloodSaturation", currentTime); // Drops by 6%, and is < 92%
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(2, alerts.size(), "Should trigger both Low Saturation and Rapid Drop alerts.");
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().startsWith("Low Blood Saturation")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Rapid Blood Saturation Drop")));
    }

    @Test
    void testSaturationNoRapidDrop_OutsideTimeWindow() {
        long currentTime = System.currentTimeMillis();
        patient.addRecord(98.0, "BloodSaturation", currentTime - (11 * 60 * 1000)); // 11 mins ago
        patient.addRecord(92.0, "BloodSaturation", currentTime); // Drops by 6% but outside window
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty(), "Should not trigger rapid drop if outside 10 min window.");
    }

    // --- Combined Alert Tests ---
    @Test
    void testHypotensiveHypoxemiaAlert() {
        long time = System.currentTimeMillis();
        patient.addRecord(89.0, "BloodPressureSystolic", time - 1000); // Systolic < 90
        patient.addRecord(80.0, "BloodPressureDiastolic", time - 1000);
        patient.addRecord(91.0, "BloodSaturation", time);         // Saturation < 92
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        // Expect 3 alerts: Systolic Low, Saturation Low, and Combined HypotensiveHypoxemia
        assertEquals(3, alerts.size()); 
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().startsWith("Critical Systolic Low")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().startsWith("Low Blood Saturation")));
    }

    @Test
    void testHypotensiveHypoxemiaAlert_SystolicOK() {
        long time = System.currentTimeMillis();
        patient.addRecord(95.0, "BloodPressureSystolic", time - 1000); // Systolic >= 90
        patient.addRecord(80.0, "BloodPressureDiastolic", time - 1000);
        patient.addRecord(91.0, "BloodSaturation", time);         // Saturation < 92
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size()); // Only Low Saturation
        assertFalse(alerts.stream().anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().startsWith("Low Blood Saturation")));
    }

    // --- ECG Data Alerts Tests ---
    @Test
    void testECGAbnormalPeak() {
        long time = System.currentTimeMillis();
        // Create a baseline average around 0.5 for the window
        for (int i = 0; i < 9; i++) {
            patient.addRecord(0.5 + (Math.random() * 0.2 - 0.1), "ECG", time + i * 1000); // avg around 0.5
        }
        patient.addRecord(2.0, "ECG", time + 9 * 1000); // Peak: 2.0, Avg will be around 0.5. 2.0 > 3 * 0.5 (1.5)
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Abnormal ECG Peak"));
    }
    
    @Test
    void testECGAbnormalPeak_FromZeroBaseline() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < 9; i++) {
            patient.addRecord(0.0, "ECG", time + i * 1000); // All zero baseline
        }
        patient.addRecord(1.5, "ECG", time + 9 * 1000); // Peak: 1.5. Avg is 0. 1.5 > 1.0 (our arbitrary threshold for zero avg)
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().startsWith("Abnormal ECG Activity (from zero baseline)"));
    }

    @Test
    void testECGNoAbnormalPeak_SmallFluctuation() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            patient.addRecord(0.5 + (Math.random() * 0.1 - 0.05), "ECG", time + i * 1000); // Small fluctuations around 0.5
        }
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty());
    }
    
    @Test
    void testECGNotEnoughData() {
        long time = System.currentTimeMillis();
        for(int i=0; i < AlertGenerator.ECG_SLIDING_WINDOW_SIZE -1; ++i) {
             patient.addRecord(0.5, "ECG", time + i * 1000);
        }
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty(), "Should not trigger ECG alert with insufficient data.");
    }

    // --- Manual Alert Tests ---
    @Test
    void testManualAlertTriggered() {
        patient.addRecord(1.0, "ManualAlert", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().equals("Manual Alert Triggered"));
    }

    @Test
    void testManualAlert_NoAlertIfValueZero() {
        // Assuming 0 or no record means no manual alert is active
        patient.addRecord(0.0, "ManualAlert", System.currentTimeMillis()); 
        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty());
    }
    
    // --- No Alerts Test ---
    @Test
    void testNoAlerts_NormalData() {
        long time = System.currentTimeMillis();
        patient.addRecord(120.0, "BloodPressureSystolic", time);
        patient.addRecord(80.0, "BloodPressureDiastolic", time);
        patient.addRecord(98.0, "BloodSaturation", time + 1000);
        patient.addRecord(0.5, "ECG", time + 2000);
        patient.addRecord(0.5, "ECG", time + 3000);
        patient.addRecord(0.5, "ECG", time + 4000);
        patient.addRecord(0.5, "ECG", time + 5000);
        patient.addRecord(0.5, "ECG", time + 6000);
        patient.addRecord(0.5, "ECG", time + 7000);
        patient.addRecord(0.5, "ECG", time + 8000);
        patient.addRecord(0.5, "ECG", time + 9000);
        patient.addRecord(0.5, "ECG", time + 10000);
        patient.addRecord(0.5, "ECG", time + 11000);

        alertGenerator.evaluateData(patient);
        assertTrue(alertGenerator.getTriggeredAlerts().isEmpty(), "Should not trigger any alerts for normal data.");
    }
} 