package com.alerts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlertDecoratorTest {
    @Test
    void testRepeatedAlertDecorator() {
        Alert baseAlert = new Alert("123", "Test Alert", System.currentTimeMillis());
        RepeatedAlertDecorator decorator = new RepeatedAlertDecorator(baseAlert);
        
        assertEquals("[Repeated] Test Alert", decorator.getCondition());
        assertEquals("123", decorator.getPatientId());
        assertTrue(decorator.shouldRepeat());
        
        decorator.updateLastTriggeredTime();
        assertFalse(decorator.shouldRepeat());
    }

    @Test
    void testPriorityAlertDecorator() {
        Alert baseAlert = new Alert("123", "Test Alert", System.currentTimeMillis());
        PriorityAlertDecorator decorator = new PriorityAlertDecorator(baseAlert, 2);
        
        assertEquals("[Priority 2] Test Alert", decorator.getCondition());
        assertEquals("123", decorator.getPatientId());
        assertEquals(2, decorator.getPriorityLevel());
    }

    @Test
    void testMultipleDecorators() {
        Alert baseAlert = new Alert("123", "Test Alert", System.currentTimeMillis());
        Alert priorityDecorator = new PriorityAlertDecorator(baseAlert, 1);
        Alert repeatedDecorator = new RepeatedAlertDecorator(priorityDecorator);
        
        assertEquals("[Repeated] [Priority 1] Test Alert", repeatedDecorator.getCondition());
        assertEquals("123", repeatedDecorator.getPatientId());
    }
} 