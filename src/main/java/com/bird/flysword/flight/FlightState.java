package com.bird.flysword.flight;

import java.util.HashMap;
import java.util.Map;

public class FlightState {
    
    private boolean flying;
    private double speed;
    private double altitude;
    private long lastUpdateTime;
    private long startTime;
    private Map<String, Object> customData;
    
    // 訊息冷卻相關
    private long lastHeightWarningTime = 0;
    private long lastObstacleWarningTime = 0;
    private static final long WARNING_COOLDOWN = 3000; // 3秒冷卻
    
    // 強制降落相關
    private long heightViolationStartTime = 0; // 開始違反高度限制的時間
    private boolean heightViolationActive = false; // 是否正在違反高度限制
    private static final long FORCE_LANDING_DELAY = 10000; // 10秒後強制降落
    
    // Shift 鍵強制降落相關
    private long shiftLandingStartTime = 0; // 開始按住 Shift 的時間
    private boolean shiftLandingActive = false; // 是否正在執行 Shift 降落
    
    public FlightState() {
        this.flying = false;
        this.speed = 0.0;
        this.altitude = 0.0;
        this.lastUpdateTime = System.currentTimeMillis();
        this.startTime = System.currentTimeMillis();
        this.customData = new HashMap<>();
    }
    
    // Getter 和 Setter 方法
    public boolean isFlying() {
        return flying;
    }
    
    public void setFlying(boolean flying) {
        this.flying = flying;
        if (flying) {
            this.startTime = System.currentTimeMillis();
        }
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getAltitude() {
        return altitude;
    }
    
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getFlightDuration() {
        if (!flying) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    public Map<String, Object> getCustomData() {
        return customData;
    }
    
    public void setCustomData(String key, Object value) {
        this.customData.put(key, value);
    }
    
    public Object getCustomData(String key) {
        return this.customData.get(key);
    }
    
    public boolean hasCustomData(String key) {
        return this.customData.containsKey(key);
    }
    
    public void removeCustomData(String key) {
        this.customData.remove(key);
    }
    
    public void clearCustomData() {
        this.customData.clear();
    }
    
    /**
     * 檢查是否可以發送高度警告
     */
    public boolean canSendHeightWarning() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHeightWarningTime >= WARNING_COOLDOWN) {
            lastHeightWarningTime = currentTime;
            return true;
        }
        return false;
    }
    
    /**
     * 檢查是否可以發送障礙物警告
     */
    public boolean canSendObstacleWarning() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastObstacleWarningTime >= WARNING_COOLDOWN) {
            lastObstacleWarningTime = currentTime;
            return true;
        }
        return false;
    }
    
    /**
     * 開始違反高度限制
     */
    public void startHeightViolation() {
        if (!heightViolationActive) {
            heightViolationActive = true;
            heightViolationStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 清除高度違反狀態
     */
    public void clearHeightViolation() {
        heightViolationActive = false;
        heightViolationStartTime = 0;
    }
    
    /**
     * 檢查是否需要強制降落
     */
    public boolean shouldForceLanding() {
        if (!heightViolationActive) {
            return false;
        }
        return (System.currentTimeMillis() - heightViolationStartTime) >= FORCE_LANDING_DELAY;
    }
    
    /**
     * 獲取剩餘警告時間（秒）
     */
    public int getRemainingWarningTime() {
        if (!heightViolationActive) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - heightViolationStartTime;
        return Math.max(0, (int)((FORCE_LANDING_DELAY - elapsed) / 1000));
    }
    
    /**
     * 開始 Shift 鍵強制降落倒計時
     */
    public void startShiftLanding() {
        if (!shiftLandingActive) {
            shiftLandingActive = true;
            shiftLandingStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 停止 Shift 鍵強制降落倒計時
     */
    public void stopShiftLanding() {
        shiftLandingActive = false;
        shiftLandingStartTime = 0;
    }
    
    /**
     * 檢查是否應該執行 Shift 鍵強制降落
     */
    public boolean shouldShiftLanding(long delay) {
        if (!shiftLandingActive) {
            return false;
        }
        return (System.currentTimeMillis() - shiftLandingStartTime) >= delay;
    }
    
    /**
     * 獲取 Shift 降落剩餘時間（秒）
     */
    public int getRemainingShiftTime(long delay) {
        if (!shiftLandingActive) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - shiftLandingStartTime;
        return Math.max(0, (int)((delay - elapsed) / 1000));
    }
    
    /**
     * 檢查是否正在執行 Shift 降落
     */
    public boolean isShiftLandingActive() {
        return shiftLandingActive;
    }
}
