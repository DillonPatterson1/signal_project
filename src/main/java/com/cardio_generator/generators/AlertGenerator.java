package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Simulates medical alerts for patients based on configurable probabilities.
 *
 * <p>Alerts can be in two states: triggered or resolved. The generator uses
 * exponential distribution to determine when alerts are triggered and a fixed
 * probability to determine when they are resolved.
 */
public class AlertGenerator implements PatientDataGenerator {

    /** Random number generator for determining alert states. */
    private static final Random RANDOM_GENERATOR = new Random();
    
    /** Probability that an active alert will be resolved in the next period. */
    private static final double ALERT_RESOLUTION_PROBABILITY = 0.9;
    
    /** Average rate of alert triggers per period. */
    private static final double ALERT_TRIGGER_RATE = 0.1;
    
    /** Array tracking alert states for each patient. */
    private final boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Creates a new alert generator for the specified number of patients.
     *
     * @param patientCount the number of patients to generate alerts for. Must be positive.
     * @throws IllegalArgumentException if patientCount is less than 1
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates alert data for a specific patient.
     *
     * <p>If an alert is currently active, there is a 90% chance it will be resolved.
     * If no alert is active, there is a chance (based on exponential distribution)
     * that a new alert will be triggered.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the strategy to use for outputting the alert data
     * @throws IllegalArgumentException if patientId is invalid
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                handleActiveAlert(patientId, outputStrategy);
            } else {
                handleInactiveAlert(patientId, outputStrategy);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }

    /**
     * Handles the case where an alert is currently active.
     *
     * <p>There is a 90% chance the alert will be resolved. If resolved,
     * an "resolved" alert is output.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the strategy to use for outputting the alert data
     */
    private void handleActiveAlert(int patientId, OutputStrategy outputStrategy) {
        if (RANDOM_GENERATOR.nextDouble() < ALERT_RESOLUTION_PROBABILITY) {
            alertStates[patientId] = false;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
        }
    }

    /**
     * Handles the case where no alert is currently active.
     *
     * <p>Calculates the probability of a new alert being triggered based on
     * exponential distribution. If triggered, a "triggered" alert is output.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the strategy to use for outputting the alert data
     */
    private void handleInactiveAlert(int patientId, OutputStrategy outputStrategy) {
        double probability = calculateAlertProbability();
        boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < probability;

        if (alertTriggered) {
            alertStates[patientId] = true;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
        }
    }

    /**
     * Calculates the probability of an alert being triggered in the current period.
     *
     * <p>Uses the formula 1 - e^(-λ) where λ is the alert trigger rate.
     *
     * @return the probability of an alert being triggered, between 0 and 1
     */
    private double calculateAlertProbability() {
        return -Math.expm1(-ALERT_TRIGGER_RATE);
    }
}
