package com.alerts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlertFactoryTest {
    @Test
    public void testBloodPressureAlertFactory() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("123", "High Pressure", 1000L);
        
        assertEquals("123", alert.getPatientId());
        assertEquals("Blood Pressure: High Pressure", alert.getCondition());
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    public void testBloodOxygenAlertFactory() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("123", "Low Oxygen", 1000L);
        
        assertEquals("123", alert.getPatientId());
        assertEquals("Blood Oxygen: Low Oxygen", alert.getCondition());
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    public void testECGAlertFactory() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("123", "Irregular Rhythm", 1000L);
        
        assertEquals("123", alert.getPatientId());
        assertEquals("ECG: Irregular Rhythm", alert.getCondition());
        assertEquals(1000L, alert.getTimestamp());
    }
} 