package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {
    private final int priorityLevel;

    public PriorityAlertDecorator(Alert decoratedAlert, int priorityLevel) {
        super(decoratedAlert);
        this.priorityLevel = priorityLevel;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    @Override
    public String getCondition() {
        return "[Priority " + priorityLevel + "] " + decoratedAlert.getCondition();
    }
} 