package com.alerts;

public class BloodPressureAlertFactory implements AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "Blood Pressure: " + condition, timestamp);
    }
} 