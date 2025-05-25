package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {
    private static final long REPEAT_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    private long lastTriggeredTime;

    public RepeatedAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
        this.lastTriggeredTime = decoratedAlert.getTimestamp();
    }

    public boolean shouldRepeat() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTriggeredTime) >= REPEAT_INTERVAL_MS;
    }

    public void updateLastTriggeredTime() {
        this.lastTriggeredTime = System.currentTimeMillis();
    }

    @Override
    public String getCondition() {
        return "[Repeated] " + decoratedAlert.getCondition();
    }
} 