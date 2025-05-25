package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.stream.Collectors;

public class BloodOxygenStrategy implements AlertStrategy {
    private static final double SATURATION_LOW_THRESHOLD = 92.0;
    private static final double SATURATION_RAPID_DROP_PERCENTAGE = 5.0;
    private static final long SATURATION_RAPID_DROP_INTERVAL_MS = 10 * 60 * 1000; // 10 minutes

    @Override
    public void checkAlert(Patient patient, List<PatientRecord> allRecords, AlertFactory alertFactory) {
        List<PatientRecord> saturationRecords = filterAndSortRecords(allRecords, "BloodSaturation");
        if (saturationRecords.isEmpty()) return;

        checkLowSaturation(patient, saturationRecords, alertFactory);
        checkRapidDrop(patient, saturationRecords, alertFactory);
    }

    private void checkLowSaturation(Patient patient, List<PatientRecord> records, AlertFactory alertFactory) {
        PatientRecord latest = records.get(records.size() - 1);
        if (latest.getMeasurementValue() < SATURATION_LOW_THRESHOLD) {
            alertFactory.createAlert(String.valueOf(patient.getPatientId()),
                "Low Blood Saturation: " + latest.getMeasurementValue(),
                latest.getTimestamp());
        }
    }

    private void checkRapidDrop(Patient patient, List<PatientRecord> records, AlertFactory alertFactory) {
        PatientRecord latest = records.get(records.size() - 1);
        for (int i = records.size() - 2; i >= 0; i--) {
            PatientRecord earlierRecord = records.get(i);
            if (latest.getTimestamp() - earlierRecord.getTimestamp() <= SATURATION_RAPID_DROP_INTERVAL_MS) {
                if (earlierRecord.getMeasurementValue() - latest.getMeasurementValue() >= SATURATION_RAPID_DROP_PERCENTAGE) {
                    alertFactory.createAlert(String.valueOf(patient.getPatientId()),
                        "Rapid Blood Saturation Drop",
                        latest.getTimestamp());
                    break;
                }
            } else {
                break;
            }
        }
    }

    private List<PatientRecord> filterAndSortRecords(List<PatientRecord> allRecords, String recordType) {
        return allRecords.stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .collect(Collectors.toList());
    }
} 