package com.bird.flysword.core.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;

/**
 * 啟動與條件判斷模組
 * 負責檢查玩家是否滿足飛行條件
 */
public class ConditionChecker {
    
    private final Flysword plugin;
    private final Map<UUID, Long> cooldowns;
    
    public ConditionChecker(Flysword plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }
    
    /**
     * 檢查玩家是否可以啟動飛行
     */
    public FlightConditionResult checkFlightCondition(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        
        // 1. 檢查玩家狀態
        if (player.isDead()) {
            return FlightConditionResult.failure("§c你已經死亡，無法使用飛劍！");
        }
        
        if (player.isInsideVehicle()) {
            return FlightConditionResult.failure("§c你正在載具內，無法使用飛劍！");
        }
        
        if (playerData.isFlying()) {
            return FlightConditionResult.failure("§c你已經在飛行中！");
        }
        
        // 2. 檢查物品
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.DIAMOND_SWORD) {
            return FlightConditionResult.failure("§c請手持飛劍才能使用！");
        }
        
        // 3. 檢查權限
        if (!player.hasPermission("flysword.use")) {
            return FlightConditionResult.failure("§c你沒有使用飛劍的權限！");
        }
        
        // 4. 檢查冷卻時間
        if (isOnCooldown(uuid)) {
            long remaining = getCooldownRemaining(uuid);
            return FlightConditionResult.failure("§e飛行冷卻中，請等待 " + (remaining / 1000.0) + " 秒");
        }
        
        // 5. 檢查耐久度
        if (playerData.getDurability() <= 0) {
            return FlightConditionResult.failure("§c飛劍耐久度不足，請修復後再使用！");
        }
        
        // 6. 檢查區域限制
        FlightConditionResult areaCheck = checkAreaRestrictions(player.getLocation());
        if (!areaCheck.isSuccess()) {
            return areaCheck;
        }
        
        // 7. 檢查環境安全
        FlightConditionResult safetyCheck = checkEnvironmentSafety(player.getLocation());
        if (!safetyCheck.isSuccess()) {
            return safetyCheck;
        }
        
        return FlightConditionResult.success();
    }
    
    /**
     * 檢查區域限制
     */
    private FlightConditionResult checkAreaRestrictions(Location location) {
        // 檢查世界限制
        String worldName = location.getWorld().getName();
        if (plugin.getConfig().getStringList("flight.disabled_worlds").contains(worldName)) {
            return FlightConditionResult.failure("§c此世界禁止使用飛劍！");
        }
        
        // 檢查高度限制
        double maxHeight = plugin.getConfig().getDouble("flight.max_height", 256);
        double minHeight = plugin.getConfig().getDouble("flight.min_height", 0);
        
        if (location.getY() > maxHeight) {
            return FlightConditionResult.failure("§c你已經飛得太高了！最大高度: " + maxHeight);
        }
        
        if (location.getY() < minHeight) {
            return FlightConditionResult.failure("§c這個高度太低，無法啟動飛劍！最小高度: " + minHeight);
        }
        
        // 檢查保護區域 (如果有WorldGuard等插件)
        if (isInProtectedRegion(location)) {
            return FlightConditionResult.failure("§c此區域禁止使用飛劍！");
        }
        
        return FlightConditionResult.success();
    }
    
    /**
     * 檢查環境安全
     */
    private FlightConditionResult checkEnvironmentSafety(Location location) {
        // 檢查頭頂空間
        Location above = location.clone().add(0, 2, 0);
        if (above.getBlock().getType() != Material.AIR) {
            return FlightConditionResult.failure("§c頭頂空間不足，無法啟動飛劍！");
        }
        
        // 檢查是否在危險環境中
        Material blockBelow = location.clone().add(0, -1, 0).getBlock().getType();
        if (blockBelow == Material.LAVA || blockBelow == Material.MAGMA_BLOCK) {
            return FlightConditionResult.failure("§c這裡太危險了，不適合啟動飛劍！");
        }
        
        return FlightConditionResult.success();
    }
    
    /**
     * 檢查是否在保護區域
     */
    private boolean isInProtectedRegion(Location location) {
        // 這裡可以整合 WorldGuard、Residence 等保護插件
        // 暫時返回 false，表示沒有保護區域檢查
        return false;
    }
    
    /**
     * 檢查玩家是否在冷卻中
     */
    public boolean isOnCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }
        
        long cooldownEnd = cooldowns.get(playerUUID);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * 獲取剩餘冷卻時間（毫秒）
     */
    public long getCooldownRemaining(UUID playerUUID) {
        if (!isOnCooldown(playerUUID)) {
            return 0;
        }
        
        return cooldowns.get(playerUUID) - System.currentTimeMillis();
    }
    
    /**
     * 設置玩家冷卻時間
     */
    public void setCooldown(UUID playerUUID, long cooldownMs) {
        cooldowns.put(playerUUID, System.currentTimeMillis() + cooldownMs);
    }
    
    /**
     * 設置玩家冷卻時間（使用配置文件中的默認值）
     */
    public void setCooldown(UUID playerUUID) {
        long cooldownTime = plugin.getConfig().getLong("flight.cooldown_time", 2000);
        setCooldown(playerUUID, cooldownTime);
    }
    
    /**
     * 清除玩家冷卻時間
     */
    public void clearCooldown(UUID playerUUID) {
        cooldowns.remove(playerUUID);
    }
    
    /**
     * 檢查玩家是否可以解鎖指定皮膚
     */
    public UnlockConditionResult checkUnlockCondition(Player player, String skinId) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        
        // 檢查是否已經擁有
        if (playerData.hasSkin(skinId)) {
            return UnlockConditionResult.failure("§e你已經擁有這個皮膚了！");
        }
        
        // 獲取皮膚信息
        var skin = plugin.getSkinManager().getSkin(skinId);
        if (skin == null) {
            return UnlockConditionResult.failure("§c皮膚不存在！");
        }
        
        String unlockType = skin.getUnlockType();
        String unlockValue = skin.getUnlockValue();
        
        switch (unlockType.toLowerCase()) {
            case "default":
                return UnlockConditionResult.success();
                
            case "vip":
                if (!playerData.hasVipLevel(unlockValue)) {
                    return UnlockConditionResult.failure("§c需要 " + unlockValue + " VIP 等級才能解鎖！");
                }
                break;
                
            case "achievement":
                long requiredProgress = 1; // 默認需要完成
                if (!hasAchievement(playerData, unlockValue, requiredProgress)) {
                    return UnlockConditionResult.failure("§c需要完成成就「" + unlockValue + "」才能解鎖！");
                }
                break;
                
            case "item":
                if (playerData.getSkinTokens() <= 0) {
                    return UnlockConditionResult.failure("§c你沒有足夠的皮膚券！");
                }
                break;
                
            case "shop":
                // 這裡可以整合經濟插件
                return UnlockConditionResult.failure("§c請通過商店購買此皮膚！");
                
            case "event":
                // 檢查活動狀態
                if (!isEventActive(unlockValue)) {
                    return UnlockConditionResult.failure("§c此皮膚只能在活動期間解鎖！");
                }
                break;
                
            default:
                return UnlockConditionResult.failure("§c未知的解鎖條件！");
        }
        
        return UnlockConditionResult.success();
    }
    
    /**
     * 檢查玩家是否有指定成就
     */
    private boolean hasAchievement(PlayerData playerData, String achievementId, long requiredProgress) {
        return playerData.getAchievementProgress(achievementId) >= requiredProgress;
    }
    
    /**
     * 檢查活動是否進行中
     */
    private boolean isEventActive(String eventId) {
        // 這裡可以實現活動系統邏輯
        // 暫時返回 false
        return false;
    }
}

/**
 * 飛行條件檢查結果
 */
class FlightConditionResult {
    private final boolean success;
    private final String message;
    
    private FlightConditionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static FlightConditionResult success() {
        return new FlightConditionResult(true, "");
    }
    
    public static FlightConditionResult failure(String message) {
        return new FlightConditionResult(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
}

/**
 * 解鎖條件檢查結果
 */
class UnlockConditionResult {
    private final boolean success;
    private final String message;
    
    private UnlockConditionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static UnlockConditionResult success() {
        return new UnlockConditionResult(true, "");
    }
    
    public static UnlockConditionResult failure(String message) {
        return new UnlockConditionResult(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
}
