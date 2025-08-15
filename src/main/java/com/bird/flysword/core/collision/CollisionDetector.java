package com.bird.flysword.core.collision;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 三維運動與碰撞檢測模組
 * 負責飛行過程中的碰撞預測與閃避判斷
 */
public class CollisionDetector {
    
    // 不可穿越的方塊類型
    private static final Set<Material> SOLID_BLOCKS = new HashSet<>();
    
    static {
        // 添加所有固體方塊
        for (Material material : Material.values()) {
            if (material.isSolid() && material.isBlock()) {
                SOLID_BLOCKS.add(material);
            }
        }
        
        // 移除一些可以穿越的固體方塊
        SOLID_BLOCKS.remove(Material.SNOW);
        SOLID_BLOCKS.remove(Material.TALL_GRASS);
        SOLID_BLOCKS.remove(Material.SHORT_GRASS);
    }
    
    /**
     * 檢查指定路徑是否有碰撞
     */
    public CollisionResult checkPathCollision(Location start, Location end, double playerRadius) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        
        if (distance == 0) {
            return new CollisionResult(false, null, null);
        }
        
        direction.normalize();
        
        // 按步長檢查路徑
        double stepSize = 0.5;
        int steps = (int) Math.ceil(distance / stepSize);
        
        for (int i = 0; i <= steps; i++) {
            double currentDistance = Math.min(i * stepSize, distance);
            Location checkPoint = start.clone().add(direction.clone().multiply(currentDistance));
            
            CollisionResult collision = checkPointCollision(checkPoint, playerRadius);
            if (collision.hasCollision()) {
                return collision;
            }
        }
        
        return new CollisionResult(false, null, null);
    }
    
    /**
     * 檢查單點碰撞
     */
    public CollisionResult checkPointCollision(Location location, double playerRadius) {
        // 檢查玩家周圍的方塊
        for (double x = -playerRadius; x <= playerRadius; x += 0.5) {
            for (double y = -1.0; y <= 2.0; y += 0.5) {
                for (double z = -playerRadius; z <= playerRadius; z += 0.5) {
                    Location checkLoc = location.clone().add(x, y, z);
                    Block block = checkLoc.getBlock();
                    
                    if (SOLID_BLOCKS.contains(block.getType())) {
                        return new CollisionResult(true, checkLoc, block.getType());
                    }
                }
            }
        }
        
        return new CollisionResult(false, null, null);
    }
    
    /**
     * 預測未來位置的碰撞
     */
    public CollisionResult predictCollision(Player player, Vector velocity, double timeAhead) {
        Location currentLoc = player.getLocation();
        Vector predictedMovement = velocity.clone().multiply(timeAhead);
        Location predictedLoc = currentLoc.clone().add(predictedMovement);
        
        return checkPathCollision(currentLoc, predictedLoc, 0.6); // 玩家半徑約0.6方塊
    }
    
    /**
     * 計算閃避向量
     */
    public Vector calculateAvoidanceVector(Location collisionPoint, Location playerLocation) {
        Vector avoidance = playerLocation.toVector().subtract(collisionPoint.toVector());
        avoidance.setY(0); // 水平閃避
        
        if (avoidance.lengthSquared() == 0) {
            // 如果在同一位置，隨機選擇一個方向
            avoidance = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
        }
        
        return avoidance.normalize().multiply(0.5);
    }
    
    /**
     * 尋找安全的降落點
     */
    public Location findSafeLandingSpot(Location startLocation, double searchRadius) {
        Location center = startLocation.clone();
        
        // 從中心開始螺旋搜索
        for (double radius = 1.0; radius <= searchRadius; radius += 1.0) {
            for (double angle = 0; angle < 360; angle += 15) {
                double radians = Math.toRadians(angle);
                double x = center.getX() + radius * Math.cos(radians);
                double z = center.getZ() + radius * Math.sin(radians);
                
                Location testLoc = new Location(center.getWorld(), x, center.getY(), z);
                
                // 尋找地面
                Location groundLoc = findGroundBelow(testLoc, 50);
                if (groundLoc != null && isSafeLandingSpot(groundLoc)) {
                    return groundLoc;
                }
            }
        }
        
        return null; // 找不到安全點
    }
    
    /**
     * 在指定位置下方尋找地面
     */
    private Location findGroundBelow(Location location, int maxDepth) {
        for (int y = 0; y < maxDepth; y++) {
            Location checkLoc = location.clone().subtract(0, y, 0);
            Block block = checkLoc.getBlock();
            Block above = checkLoc.clone().add(0, 1, 0).getBlock();
            
            if (SOLID_BLOCKS.contains(block.getType()) && above.getType() == Material.AIR) {
                return checkLoc.add(0, 1, 0); // 地面上方一格
            }
        }
        
        return null;
    }
    
    /**
     * 檢查是否為安全的降落點
     */
    private boolean isSafeLandingSpot(Location location) {
        // 檢查降落點上方是否有足夠空間
        for (int y = 1; y <= 3; y++) {
            Block block = location.clone().add(0, y, 0).getBlock();
            if (SOLID_BLOCKS.contains(block.getType())) {
                return false;
            }
        }
        
        // 檢查是否在危險方塊上
        Block ground = location.clone().subtract(0, 1, 0).getBlock();
        Material groundType = ground.getType();
        
        return groundType != Material.LAVA && 
               groundType != Material.MAGMA_BLOCK && 
               groundType != Material.CACTUS &&
               groundType != Material.SWEET_BERRY_BUSH;
    }
    
    /**
     * 檢查垂直碰撞（天花板和地面）
     */
    public boolean checkVerticalCollision(Location location, double verticalVelocity) {
        if (verticalVelocity > 0) {
            // 向上移動，檢查天花板
            Block ceiling = location.clone().add(0, 2.5, 0).getBlock();
            return SOLID_BLOCKS.contains(ceiling.getType());
        } else if (verticalVelocity < 0) {
            // 向下移動，檢查地面
            Block ground = location.clone().subtract(0, 1, 0).getBlock();
            return SOLID_BLOCKS.contains(ground.getType());
        }
        
        return false;
    }
    
    /**
     * 碰撞檢測結果
     */
    public static class CollisionResult {
        private final boolean hasCollision;
        private final Location collisionPoint;
        private final Material collisionMaterial;
        
        public CollisionResult(boolean hasCollision, Location collisionPoint, Material collisionMaterial) {
            this.hasCollision = hasCollision;
            this.collisionPoint = collisionPoint;
            this.collisionMaterial = collisionMaterial;
        }
        
        public boolean hasCollision() {
            return hasCollision;
        }
        
        public Location getCollisionPoint() {
            return collisionPoint;
        }
        
        public Material getCollisionMaterial() {
            return collisionMaterial;
        }
        
        public boolean isDangerous() {
            if (!hasCollision) return false;
            
            return collisionMaterial == Material.LAVA ||
                   collisionMaterial == Material.MAGMA_BLOCK ||
                   collisionMaterial == Material.CACTUS ||
                   collisionMaterial == Material.SWEET_BERRY_BUSH;
        }
    }
}
