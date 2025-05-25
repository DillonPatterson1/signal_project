package com.cardio_generator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HealthDataSimulatorTest {
    @Test
    void testSingletonInstance() {
        HealthDataSimulator instance1 = HealthDataSimulator.getInstance();
        HealthDataSimulator instance2 = HealthDataSimulator.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testStartAndStopSimulation() {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        simulator.startSimulation();
        simulator.stopSimulation();
    }
} 