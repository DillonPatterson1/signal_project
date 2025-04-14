package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood oxygen saturation data for patients.
 *
 * <p>This implementation of PatientDataGenerator creates realistic blood oxygen
 * saturation (SpO2) values for patients. The values are maintained within a
 * healthy range (90-100%) and show small, realistic fluctuations over time.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Maintains baseline values for each patient</li>
 *   <li>Simulates small, realistic fluctuations</li>
 *   <li>Ensures values stay within healthy range</li>
 *   <li>Handles errors gracefully</li>
 * </ul></p>
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    /** Random number generator for creating variations in saturation values. */
    private static final Random random = new Random();
    
    /** Array storing the last recorded saturation value for each patient. */
    private int[] lastSaturationValues;

    /**
     * Creates a new blood saturation data generator for the specified number of patients.
     *
     * <p>Initializes baseline saturation values for each patient between 95% and 100%,
     * which represents a healthy range for blood oxygen saturation.</p>
     *
     * @param patientCount The number of patients to generate data for
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates a new blood saturation value for the specified patient.
     *
     * <p>This method:
     * <ol>
     *   <li>Generates a small random variation (-1, 0, or 1)</li>
     *   <li>Adds the variation to the last recorded value</li>
     *   <li>Ensures the new value stays within the healthy range (90-100%)</li>
     *   <li>Outputs the value through the provided output strategy</li>
     * </ol></p>
     *
     * <p>If any errors occur during generation, they are logged to System.err
     * and the stack trace is printed for debugging purposes.</p>
     *
     * @param patientId The unique identifier of the patient
     * @param outputStrategy The strategy to use for outputting the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
