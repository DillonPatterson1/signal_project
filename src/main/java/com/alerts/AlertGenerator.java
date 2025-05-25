package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<Alert> triggeredAlerts;

    private static final double SYSTOLIC_CRITICAL_HIGH = 180.0;
    private static final double SYSTOLIC_CRITICAL_LOW = 90.0;
    private static final double DIASTOLIC_CRITICAL_HIGH = 120.0;
    private static final double DIASTOLIC_CRITICAL_LOW = 60.0;
    private static final double BP_TREND_CHANGE_THRESHOLD = 10.0;
    private static final int BP_TREND_CONSECUTIVE_READINGS = 3;

    private static final double SATURATION_LOW_THRESHOLD = 92.0;
    private static final double SATURATION_RAPID_DROP_PERCENTAGE = 5.0;
    private static final long SATURATION_RAPID_DROP_INTERVAL_MS = 10 * 60 * 1000; // 10 minutes

    public static final int ECG_SLIDING_WINDOW_SIZE = 10;
    private static final double ECG_PEAK_DEVIATION_FACTOR = 3.0; // e.g., value > 3 * average of window

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.triggeredAlerts = new ArrayList<>();
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            System.err.println("AlertGenerator: Patient data is null.");
            return;
        }
        List<PatientRecord> records = patient.getAllRecords();
        if (records.isEmpty()) {
            return;
        }
        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        checkBloodPressureAlerts(patient, records);
        checkBloodSaturationAlerts(patient, records);
        checkCombinedAlerts(patient, records);
        checkECGAlerts(patient, records);
        checkManualAlerts(patient, records);
    }

    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> allRecords) {
        List<PatientRecord> systolicRecords = filterAndSortRecords(allRecords, "BloodPressureSystolic");
        List<PatientRecord> diastolicRecords = filterAndSortRecords(allRecords, "BloodPressureDiastolic");


        if (!systolicRecords.isEmpty()) {
            PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
            if (latestSystolic.getMeasurementValue() > SYSTOLIC_CRITICAL_HIGH) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic High: " + latestSystolic.getMeasurementValue(), latestSystolic.getTimestamp()));
            }
            if (latestSystolic.getMeasurementValue() < SYSTOLIC_CRITICAL_LOW) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Low: " + latestSystolic.getMeasurementValue(), latestSystolic.getTimestamp()));
            }
        }

        if (!diastolicRecords.isEmpty()) {
            PatientRecord latestDiastolic = diastolicRecords.get(diastolicRecords.size() - 1);
            if (latestDiastolic.getMeasurementValue() > DIASTOLIC_CRITICAL_HIGH) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic High: " + latestDiastolic.getMeasurementValue(), latestDiastolic.getTimestamp()));
            }
            if (latestDiastolic.getMeasurementValue() < DIASTOLIC_CRITICAL_LOW) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Low: " + latestDiastolic.getMeasurementValue(), latestDiastolic.getTimestamp()));
            }
        }

        if (systolicRecords.size() >= BP_TREND_CONSECUTIVE_READINGS) {
            for (int i = 0; i <= systolicRecords.size() - BP_TREND_CONSECUTIVE_READINGS; i++) {
                checkTrend(patient, systolicRecords.subList(i, i + BP_TREND_CONSECUTIVE_READINGS), "Systolic");
            }
        }
        if (diastolicRecords.size() >= BP_TREND_CONSECUTIVE_READINGS) {
             for (int i = 0; i <= diastolicRecords.size() - BP_TREND_CONSECUTIVE_READINGS; i++) {
                checkTrend(patient, diastolicRecords.subList(i, i + BP_TREND_CONSECUTIVE_READINGS), "Diastolic");
            }
        }
    }

    private void checkTrend(Patient patient, List<PatientRecord> records, String bpType) {
        if (records.size() < BP_TREND_CONSECUTIVE_READINGS) return;

        boolean increasingTrend = true;
        boolean decreasingTrend = true;

        for (int i = 0; i < BP_TREND_CONSECUTIVE_READINGS - 1; i++) {
            double diff = records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue();
            if (!(diff > BP_TREND_CHANGE_THRESHOLD)) {
                increasingTrend = false;
            }
            if (!(diff < -BP_TREND_CHANGE_THRESHOLD)) {
                decreasingTrend = false;
            }
        }

        long alertTimestamp = records.get(BP_TREND_CONSECUTIVE_READINGS - 1).getTimestamp();
        if (increasingTrend) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), bpType + " Increasing Trend", alertTimestamp));
        }
        if (decreasingTrend) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), bpType + " Decreasing Trend", alertTimestamp));
        }
    }


    private void checkBloodSaturationAlerts(Patient patient, List<PatientRecord> allRecords) {
        List<PatientRecord> saturationRecords = filterAndSortRecords(allRecords, "BloodSaturation");
        if (saturationRecords.isEmpty()) return;

        PatientRecord latestSaturation = saturationRecords.get(saturationRecords.size() - 1);


        if (latestSaturation.getMeasurementValue() < SATURATION_LOW_THRESHOLD) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Low Blood Saturation: " + latestSaturation.getMeasurementValue(), latestSaturation.getTimestamp()));
        }
        for (int i = saturationRecords.size() - 2; i >= 0; i--) {
            PatientRecord earlierRecord = saturationRecords.get(i);
            if (latestSaturation.getTimestamp() - earlierRecord.getTimestamp() <= SATURATION_RAPID_DROP_INTERVAL_MS) {
                if (earlierRecord.getMeasurementValue() - latestSaturation.getMeasurementValue() >= SATURATION_RAPID_DROP_PERCENTAGE) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Blood Saturation Drop", latestSaturation.getTimestamp()));
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void checkCombinedAlerts(Patient patient, List<PatientRecord> allRecords) {
        List<PatientRecord> systolicRecords = filterAndSortRecords(allRecords, "BloodPressureSystolic");
        List<PatientRecord> saturationRecords = filterAndSortRecords(allRecords, "BloodSaturation");

        if (systolicRecords.isEmpty() || saturationRecords.isEmpty()) return;

        PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
        PatientRecord latestSaturation = saturationRecords.get(saturationRecords.size() - 1);

        if (latestSystolic.getMeasurementValue() < SYSTOLIC_CRITICAL_LOW &&
            latestSaturation.getMeasurementValue() < SATURATION_LOW_THRESHOLD) {
            long alertTimestamp = Math.max(latestSystolic.getTimestamp(), latestSaturation.getTimestamp());
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia Alert", alertTimestamp));
        }
    }

    private void checkECGAlerts(Patient patient, List<PatientRecord> allRecords) {
        List<PatientRecord> ecgRecords = filterAndSortRecords(allRecords, "ECG");
        if (ecgRecords.size() < ECG_SLIDING_WINDOW_SIZE) return; // Not enough data for sliding window

        // Consider the latest set of records for the sliding window
        int startIndex = Math.max(0, ecgRecords.size() - ECG_SLIDING_WINDOW_SIZE);
        List<PatientRecord> window = ecgRecords.subList(startIndex, ecgRecords.size());
        
        double sum = 0;
        for (PatientRecord record : window) {
            sum += record.getMeasurementValue();
        }
        double average = sum / window.size();

        // Check the most recent ECG value against the average of the window
        PatientRecord latestEcg = window.get(window.size() - 1); 
        if (Math.abs(latestEcg.getMeasurementValue()) > Math.abs(average * ECG_PEAK_DEVIATION_FACTOR) && average != 0) { // Avoid division by zero or alerts on zero-average if not meaningful
             triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal ECG Peak: " + latestEcg.getMeasurementValue() + " (Avg: " + String.format("%.2f", average) + ")", latestEcg.getTimestamp()));
        } else if (average == 0 && Math.abs(latestEcg.getMeasurementValue()) > 1.0) { // Arbitrary threshold if average is zero, assuming ECG should not be flatline then suddenly spike high without it being notable
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal ECG Activity (from zero baseline): " + latestEcg.getMeasurementValue(), latestEcg.getTimestamp()));
        }
    }
    
    private void checkManualAlerts(Patient patient, List<PatientRecord> allRecords) {
        // Assuming "ManualAlert" is a record type. Value > 0 means active.
        // This check could be improved if "untriggered" events also come as records (e.g., value 0).
        // For now, any "ManualAlert" record is treated as an active alert event at its timestamp.
        List<PatientRecord> manualAlertRecords = filterAndSortRecords(allRecords, "ManualAlert");
        for (PatientRecord record : manualAlertRecords) {
            if (record.getMeasurementValue() > 0) { // Or some other condition indicating active alert
                 triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Manual Alert Triggered", record.getTimestamp()));
            }
        }
    }


    private List<PatientRecord> filterAndSortRecords(List<PatientRecord> allRecords, String recordType) {
        return allRecords.stream()
                .filter(r -> r.getRecordType().equals(recordType))
                // .sorted(Comparator.comparingLong(PatientRecord::getTimestamp)) // Already sorted in evaluateData
                .collect(Collectors.toList());
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // Simple console log for now.
        System.out.println("ALERT TRIGGERED: Patient ID " + alert.getPatientId() +
                           " - Condition: " + alert.getCondition() +
                           " - Timestamp: " + alert.getTimestamp());
        this.triggeredAlerts.add(alert);
    }

    /**
     * Returns a list of alerts triggered during the last evaluation(s).
     * Useful for testing.
     * @return A list of triggered Alert objects.
     */
    public List<Alert> getTriggeredAlerts() {
        return new ArrayList<>(triggeredAlerts); // Return a copy
    }

    /**
     * Clears the list of triggered alerts. 
     * Useful for resetting state in tests.
     */
    public void clearTriggeredAlerts() {
        this.triggeredAlerts.clear();
    }
}
