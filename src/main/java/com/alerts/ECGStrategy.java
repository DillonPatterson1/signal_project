package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;
import java.util.stream.Collectors;

public class ECGStrategy implements AlertStrategy {
    private static final int ECG_SLIDING_WINDOW_SIZE = 10;
    private static final double ECG_PEAK_DEVIATION_FACTOR = 3.0;

    @Override
    public void checkAlert(Patient patient, List<PatientRecord> allRecords, AlertFactory alertFactory) {
        List<PatientRecord> ecgRecords = filterAndSortRecords(allRecords, "ECG");
        if (ecgRecords.size() < ECG_SLIDING_WINDOW_SIZE) return;

        checkECGPeaks(patient, ecgRecords, alertFactory);
    }

    private void checkECGPeaks(Patient patient, List<PatientRecord> records, AlertFactory alertFactory) {
        int startIndex = Math.max(0, records.size() - ECG_SLIDING_WINDOW_SIZE);
        List<PatientRecord> window = records.subList(startIndex, records.size());
        
        double sum = 0;
        for (PatientRecord record : window) {
            sum += record.getMeasurementValue();
        }
        double average = sum / window.size();

        PatientRecord latestEcg = window.get(window.size() - 1);
        if (Math.abs(latestEcg.getMeasurementValue()) > Math.abs(average * ECG_PEAK_DEVIATION_FACTOR) && average != 0) {
            alertFactory.createAlert(String.valueOf(patient.getPatientId()),
                "Abnormal ECG Peak: " + latestEcg.getMeasurementValue() + 
                " (Avg: " + String.format("%.2f", average) + ")",
                latestEcg.getTimestamp());
        } else if (average == 0 && Math.abs(latestEcg.getMeasurementValue()) > 1.0) {
            alertFactory.createAlert(String.valueOf(patient.getPatientId()),
                "Abnormal ECG Activity (from zero baseline): " + latestEcg.getMeasurementValue(),
                latestEcg.getTimestamp());
        }
    }

    private List<PatientRecord> filterAndSortRecords(List<PatientRecord> allRecords, String recordType) {
        return allRecords.stream()
                .filter(r -> r.getRecordType().equals(recordType))
                .collect(Collectors.toList());
    }
} 