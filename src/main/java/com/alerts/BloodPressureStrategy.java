package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.stream.Collectors;

public class BloodPressureStrategy implements AlertStrategy {
    private static final double SYSTOLIC_CRITICAL_HIGH = 180.0;
    private static final double SYSTOLIC_CRITICAL_LOW = 90.0;
    private static final double DIASTOLIC_CRITICAL_HIGH = 120.0;
    private static final double DIASTOLIC_CRITICAL_LOW = 60.0;
    private static final double BP_TREND_CHANGE_THRESHOLD = 10.0;
    private static final int BP_TREND_CONSECUTIVE_READINGS = 3;

    @Override
    public void checkAlert(Patient patient, List<PatientRecord> allRecords, AlertFactory alertFactory) {
        List<PatientRecord> systolicRecords = filterAndSortRecords(allRecords, "BloodPressureSystolic");
        List<PatientRecord> diastolicRecords = filterAndSortRecords(allRecords, "BloodPressureDiastolic");

        checkSystolicAlerts(patient, systolicRecords, alertFactory);
        checkDiastolicAlerts(patient, diastolicRecords, alertFactory);
        checkTrends(patient, systolicRecords, diastolicRecords, alertFactory);
    }

    private void checkSystolicAlerts(Patient patient, List<PatientRecord> records, AlertFactory alertFactory) {
        if (!records.isEmpty()) {
            PatientRecord latest = records.get(records.size() - 1);
            if (latest.getMeasurementValue() > SYSTOLIC_CRITICAL_HIGH) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    "Critical Systolic High: " + latest.getMeasurementValue(), 
                    latest.getTimestamp());
            }
            if (latest.getMeasurementValue() < SYSTOLIC_CRITICAL_LOW) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    "Critical Systolic Low: " + latest.getMeasurementValue(), 
                    latest.getTimestamp());
            }
        }
    }

    private void checkDiastolicAlerts(Patient patient, List<PatientRecord> records, AlertFactory alertFactory) {
        if (!records.isEmpty()) {
            PatientRecord latest = records.get(records.size() - 1);
            if (latest.getMeasurementValue() > DIASTOLIC_CRITICAL_HIGH) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    "Critical Diastolic High: " + latest.getMeasurementValue(), 
                    latest.getTimestamp());
            }
            if (latest.getMeasurementValue() < DIASTOLIC_CRITICAL_LOW) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    "Critical Diastolic Low: " + latest.getMeasurementValue(), 
                    latest.getTimestamp());
            }
        }
    }

    private void checkTrends(Patient patient, List<PatientRecord> systolicRecords, 
                           List<PatientRecord> diastolicRecords, AlertFactory alertFactory) {
        checkTrend(patient, systolicRecords, "Systolic", alertFactory);
        checkTrend(patient, diastolicRecords, "Diastolic", alertFactory);
    }

    private void checkTrend(Patient patient, List<PatientRecord> records, String bpType, AlertFactory alertFactory) {
        if (records.size() < BP_TREND_CONSECUTIVE_READINGS) return;

        for (int i = 0; i <= records.size() - BP_TREND_CONSECUTIVE_READINGS; i++) {
            List<PatientRecord> window = records.subList(i, i + BP_TREND_CONSECUTIVE_READINGS);
            boolean increasingTrend = true;
            boolean decreasingTrend = true;

            for (int j = 0; j < BP_TREND_CONSECUTIVE_READINGS - 1; j++) {
                double diff = window.get(j + 1).getMeasurementValue() - window.get(j).getMeasurementValue();
                if (!(diff > BP_TREND_CHANGE_THRESHOLD)) {
                    increasingTrend = false;
                }
                if (!(diff < -BP_TREND_CHANGE_THRESHOLD)) {
                    decreasingTrend = false;
                }
            }

            long alertTimestamp = window.get(BP_TREND_CONSECUTIVE_READINGS - 1).getTimestamp();
            if (increasingTrend) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    bpType + " Increasing Trend", alertTimestamp);
            }
            if (decreasingTrend) {
                alertFactory.createAlert(String.valueOf(patient.getPatientId()), 
                    bpType + " Decreasing Trend", alertTimestamp);
            }
        }
    }

    private List<PatientRecord> filterAndSortRecords(List<PatientRecord> allRecords, String recordType) {
        return allRecords.stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .collect(Collectors.toList());
    }
} 