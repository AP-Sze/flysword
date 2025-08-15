package com.bird.flysword.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 飛劍位置和角度的數學計算工具
 * 基於3D向量數學和物理學原理自動計算最佳顯示效果
 */
public class SwordMathUtils {
    
    // 飛劍長度（米），用於計算位置偏移
    private static final double SWORD_LENGTH = 1.0;
    
    // 飛劍寬度（米），用於避免碰撞
    private static final double SWORD_WIDTH = 0.1;
    
    // 玩家身高（米），用於計算相對位置
    private static final double PLAYER_HEIGHT = 1.8;
    
    /**
     * 根據玩家的移動方向和速度計算飛劍的最佳位置和角度
     */
    public static SwordTransform calculateOptimalSwordTransform(Player player) {
        Location playerLoc = player.getLocation();
        Vector velocity = player.getVelocity();
        
        // 計算飛行方向向量
        Vector flightDirection = getFlightDirection(player, velocity);
        
        // 計算飛劍位置
        Vector swordPosition = calculateSwordPosition(playerLoc, flightDirection, velocity);
        
        // 計算飛劍角度
        Vector swordRotation = calculateSwordRotation(flightDirection, velocity);
        
        return new SwordTransform(swordPosition, swordRotation);
    }
    
    /**
     * 根據玩家視線方向計算飛劍應該指向的方向
     */
    private static Vector getFlightDirection(Player player, Vector velocity) {
        Location playerLoc = player.getLocation();
        
        // 基於玩家視線方向的方向向量
        Vector viewDirection = playerLoc.getDirection().normalize();
        
        // 如果玩家有移動速度，結合速度方向
        if (velocity.lengthSquared() > 0.01) {
            Vector velocityDirection = velocity.clone().normalize();
            
            // 加權平均：70%視線方向 + 30%速度方向
            viewDirection.multiply(0.7);
            velocityDirection.multiply(0.3);
            viewDirection.add(velocityDirection);
        }
        
        return viewDirection.normalize();
    }
    
    /**
     * 計算飛劍相對於玩家的最佳位置
     */
    private static Vector calculateSwordPosition(Location playerLoc, Vector flightDirection, Vector velocity) {
        // 基礎位置：玩家腳下
        Vector baseOffset = new Vector(0, -2.0, 0);
        
        // 根據飛行方向調整位置
        // 飛劍應該在玩家前方稍微偏下的位置
        Vector forwardOffset = flightDirection.clone().multiply(0.5);
        forwardOffset.setY(forwardOffset.getY() - 0.3); // 稍微向下偏移
        
        // 根據速度調整位置（高速時飛劍更前方）
        double speed = velocity.length();
        if (speed > 0.5) {
            double speedFactor = Math.min(speed / 2.0, 1.0); // 限制最大偏移
            Vector speedOffset = flightDirection.clone().multiply(speedFactor * 0.3);
            forwardOffset.add(speedOffset);
        }
        
        return baseOffset.add(forwardOffset);
    }
    
    /**
     * 計算飛劍的最佳角度（歐拉角，單位：度）
     */
    private static Vector calculateSwordRotation(Vector flightDirection, Vector velocity) {
        // 計算俯仰角 (Pitch / X軸旋轉)
        double pitch = Math.toDegrees(Math.asin(-flightDirection.getY()));
        
        // 計算偏航角 (Yaw / Y軸旋轉)
        double yaw = Math.toDegrees(Math.atan2(-flightDirection.getX(), flightDirection.getZ()));
        
        // 計算翻滾角 (Roll / Z軸旋轉)
        double roll = calculateRollAngle(velocity, flightDirection);
        
        // 調整角度使飛劍看起來更自然
        pitch = adjustPitchForNaturalLook(pitch, velocity.length());
        roll = adjustRollForStability(roll, velocity);
        
        return new Vector(pitch, yaw, roll);
    }
    
    /**
     * 根據橫向速度計算翻滾角度
     */
    private static double calculateRollAngle(Vector velocity, Vector flightDirection) {
        if (velocity.lengthSquared() < 0.01) {
            return 0; // 靜止時無翻滾
        }
        
        // 計算橫向速度分量
        Vector rightVector = getRightVector(flightDirection);
        double lateralSpeed = velocity.dot(rightVector);
        
        // 根據橫向速度計算翻滾角度（類似飛機轉彎）
        double maxRoll = 30.0; // 最大翻滾角度
        double rollFactor = Math.tanh(lateralSpeed * 2.0); // 使用 tanh 函數平滑過渡
        
        return rollFactor * maxRoll;
    }
    
    /**
     * 獲取垂直於飛行方向的右向量
     */
    private static Vector getRightVector(Vector forward) {
        Vector up = new Vector(0, 1, 0);
        return forward.getCrossProduct(up).normalize();
    }
    
    /**
     * 調整俯仰角使飛劍看起來更自然
     */
    private static double adjustPitchForNaturalLook(double pitch, double speed) {
        // 高速時稍微上翹，低速時稍微下沉
        double speedAdjustment = Math.tanh(speed) * 5.0; // 最多調整5度
        
        // 限制極端角度
        pitch = Math.max(-85, Math.min(85, pitch + speedAdjustment));
        
        return pitch;
    }
    
    /**
     * 調整翻滾角度增加穩定性
     */
    private static double adjustRollForStability(double roll, Vector velocity) {
        // 低速時減少翻滾角度
        double speed = velocity.length();
        double stabilityFactor = Math.min(speed / 1.0, 1.0); // 速度低於1時逐漸減少翻滾
        
        return roll * stabilityFactor;
    }
    
    /**
     * 根據特定的飛行模式計算預設角度
     */
    public static Vector calculatePresetRotation(FlightMode mode, Vector velocity) {
        switch (mode) {
            case HORIZONTAL_CRUISE:
                // 水平巡航：劍水平指向前方
                return new Vector(0, 0, 0);
                
            case VERTICAL_ASCENT:
                // 垂直上升：劍指向上方
                return new Vector(-75, 0, 0);
                
            case VERTICAL_DESCENT:
                // 垂直下降：劍指向下方
                return new Vector(75, 0, 0);
                
            case BANKING_LEFT:
                // 左轉：劍向左傾斜
                return new Vector(0, 0, -30);
                
            case BANKING_RIGHT:
                // 右轉：劍向右傾斜
                return new Vector(0, 0, 30);
                
            case DIVING:
                // 俯衝：劍大角度向下
                return new Vector(45, 0, 0);
                
            case CLIMBING:
                // 爬升：劍中等角度向上
                return new Vector(-30, 0, 0);
                
            case DYNAMIC:
            default:
                // 動態模式：根據實際運動計算
                Location tempLoc = new Location(null, 0, 0, 0);
                tempLoc.setDirection(velocity.normalize());
                Vector flightDir = velocity.normalize();
                return calculateSwordRotation(flightDir, velocity);
        }
    }
    
    /**
     * 計算飛劍相對於特定角度的位置補償
     */
    public static Vector calculatePositionCompensation(Vector rotation, double swordLength) {
        // 將角度轉換為弧度
        double pitchRad = Math.toRadians(rotation.getX());
        double yawRad = Math.toRadians(rotation.getY());
        double rollRad = Math.toRadians(rotation.getZ());
        
        // 計算飛劍末端位置（避免穿透地面或方塊）
        double compensationX = Math.sin(yawRad) * Math.cos(pitchRad) * swordLength * 0.5;
        double compensationY = -Math.sin(pitchRad) * swordLength * 0.5;
        double compensationZ = Math.cos(yawRad) * Math.cos(pitchRad) * swordLength * 0.5;
        
        return new Vector(compensationX, compensationY, compensationZ);
    }
    
    /**
     * 飛行模式枚舉
     */
    public enum FlightMode {
        HORIZONTAL_CRUISE,  // 水平巡航
        VERTICAL_ASCENT,    // 垂直上升
        VERTICAL_DESCENT,   // 垂直下降
        BANKING_LEFT,       // 左轉
        BANKING_RIGHT,      // 右轉
        DIVING,             // 俯衝
        CLIMBING,           // 爬升
        DYNAMIC             // 動態計算
    }
    
    /**
     * 劍變換數據結構
     */
    public static class SwordTransform {
        public final Vector position;
        public final Vector rotation;
        
        public SwordTransform(Vector position, Vector rotation) {
            this.position = position;
            this.rotation = rotation;
        }
        
        @Override
        public String toString() {
            return String.format("Position(%.2f, %.2f, %.2f) Rotation(%.1f°, %.1f°, %.1f°)",
                position.getX(), position.getY(), position.getZ(),
                rotation.getX(), rotation.getY(), rotation.getZ());
        }
    }
}
