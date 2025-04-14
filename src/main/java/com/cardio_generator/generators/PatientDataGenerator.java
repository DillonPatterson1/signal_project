package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient health data.
 * 
 * <p>This interface defines the contract for classes that generate simulated
 * health data for patients. Implementations of this interface are responsible
 * for creating realistic medical data such as vital signs, lab results, and
 * other health metrics.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Defines a standard method for generating patient data</li>
 *   <li>Supports different types of health data through implementations</li>
 *   <li>Allows for flexible output strategies</li>
 * </ul></p>
 */
public interface PatientDataGenerator {
    /**
     * Generates health data for a specific patient.
     * 
     * <p>This method is called periodically to generate new data points for
     * the specified patient. The generated data is sent to the provided
     * output strategy for processing.</p>
     *
     * @param patientId The unique identifier of the patient
     * @param outputStrategy The strategy to use for outputting the generated data
     * @throws IllegalArgumentException if patientId is invalid
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
