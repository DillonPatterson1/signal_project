package com.cardio_generator.outputs;

/**
 * Interface for outputting patient health data.
 * 
 * <p>This interface defines the contract for classes that handle the output
 * of patient health data. Implementations can output data to various destinations
 * such as console, files, network sockets, or WebSocket connections.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Defines a standard method for outputting patient data</li>
 *   <li>Supports different output destinations through implementations</li>
 *   <li>Provides consistent data formatting across different output types</li>
 * </ul></p>
 */
public interface OutputStrategy {
    /**
     * Outputs patient health data.
     * 
     * <p>This method is called to output a single data point for a patient.
     * The data includes the patient ID, timestamp, data type label, and
     * the actual data value.</p>
     *
     * @param patientId The unique identifier of the patient
     * @param timestamp The time when the data was recorded, in milliseconds since epoch
     * @param label The type of data being recorded (e.g., "HeartRate", "BloodPressure")
     * @param data The actual data value to be recorded
     */
    void output(int patientId, long timestamp, String label, String data);
}
