package com.bird.flysword.flight;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;

public class FlightController {
    
    private final Flysword plugin;
    private final Map<UUID, FlightSession> activeFlights;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, FlightState> flightStates;
    
    // 飛行配置（從配置文件讀取）
    private final double BASE_SPEED;
    private final double MAX_SPEED;
    private final double ACCELERATION;
    private final double DECELERATION;
    private static final long COOLDOWN_TIME = 2000; // 2秒冷卻
    private static final int OBSTACLE_CHECK_DISTANCE = 3;
    private static final double SAFE_LANDING_DISTANCE = 2.0;
    
    public FlightController(Flysword plugin) {
        this.plugin = plugin;
        this.activeFlights = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.flightStates = new HashMap<>();
        
        // 從配置文件讀取飛行參數
        this.BASE_SPEED = plugin.getConfig().getDouble("flight.speed.base_speed", 0.4);
        this.MAX_SPEED = plugin.getConfig().getDouble("flight.speed.max_speed", 1.0);
        this.ACCELERATION = plugin.getConfig().getDouble("flight.speed.acceleration", 0.05);
        this.DECELERATION = plugin.getConfig().getDouble("flight.speed.deceleration", 0.03);
    }
    
    /**
     * 檢查玩家是否可以啟動飛行
     */
    public boolean canStartFlight(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 檢查冷卻時間
        if (isOnCooldown(uuid)) {
            long remaining = getCooldownRemaining(uuid);
            player.sendMessage("§e飛行冷卻中，請等待 " + (remaining / 1000.0) + " 秒");
            return false;
        }
        
        // 檢查權限
        if (!player.hasPermission("flysword.use")) {
            player.sendMessage("§c您沒有權限使用飛劍系統！");
            return false;
        }
        
        // 檢查是否已在飛行
        if (isFlying(uuid)) {
            player.sendMessage("§e您已經在飛行中了！");
            return false;
        }
        
        // 檢查耐久度
        if (!hasValidSwordDurability(player)) {
            player.sendMessage("§c飛劍耐久度不足，無法啟動飛行！");
            return false;
        }
        
        // 檢查區域限制
        if (!isInAllowedArea(player)) {
            player.sendMessage("§c此區域不允許飛行！");
            return false;
        }
        
        return true;
    }
    
    /**
     * 啟動飛行
     */
    public boolean startFlight(Player player) {
        if (!canStartFlight(player)) {
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        
        // 設置冷卻時間
        setCooldown(uuid);
        
        // 創建飛行會話
        FlightSession session = new FlightSession(plugin, player);
        activeFlights.put(uuid, session);
        
        // 設置飛行狀態
        FlightState state = new FlightState();
        state.setFlying(true);
        state.setSpeed(BASE_SPEED);
        state.setLastUpdateTime(System.currentTimeMillis());
        flightStates.put(uuid, state);
        
        // 添加到調度器
        plugin.getFlightScheduler().addActivePlayer(uuid);
        
        // 更新玩家數據
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        playerData.setFlying(true);
        playerData.setLastFlightTime(System.currentTimeMillis());
        plugin.getDataManager().savePlayerData(player);
        
        // 播放啟動特效
        plugin.getEffectManager().playFlightStartEffect(player);
        
        player.sendMessage("§a§l⚡ 飛劍模式已啟動！");
        return true;
    }
    
    /**
     * 停止飛行
     */
    public boolean stopFlight(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!isFlying(uuid)) {
            return false;
        }
        
        // 安全降落檢查
        if (!performSafeLanding(player)) {
            player.sendMessage("§e正在尋找安全降落點...");
            return false;
        }
        
        // 停止飛行會話
        FlightSession session = activeFlights.remove(uuid);
        if (session != null) {
            session.stop();
        }
        
        // 清除飛行狀態
        flightStates.remove(uuid);
        
        // 從調度器移除
        plugin.getFlightScheduler().removeActivePlayer(uuid);
        
        // 更新玩家數據
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        playerData.setFlying(false);
        plugin.getDataManager().savePlayerData(player);
        
        // 播放停止特效
        plugin.getEffectManager().playFlightStopEffect(player);
        
        player.sendMessage("§c§l💨 飛劍模式已關閉！");
        return true;
    }
    
    /**
     * 更新飛行狀態
     */
    public void updateFlight(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!isFlying(uuid)) {
            return;
        }
        
        // 檢查耐久度
        if (!hasValidSwordDurability(player)) {
            player.sendMessage("§c飛劍耐久度耗盡，自動停止飛行！");
            stopFlight(player);
            return;
        }
        
        FlightSession session = activeFlights.get(uuid);
        FlightState state = flightStates.get(uuid);
        
        if (session == null || state == null) {
            return;
        }
        
        // 更新飛行邏輯
        updateFlightMovement(player, state);
        updateFlightEffects(player, state);
        checkObstacles(player, state);
        
        // 更新會話
        session.update();
        
        // 更新狀態時間
        state.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 更新飛行移動
     */
    private void updateFlightMovement(Player player, FlightState state) {
        Location playerLoc = player.getLocation();
        
        // 獲取玩家輸入
        boolean isSneaking = player.isSneaking();
        
        // 處理 Shift 鍵強制降落邏輯
        handleShiftLanding(player, state, isSneaking);
        
        // 原有的移動邏輯（僅在非強制降落狀態下執行）
        if (!state.isShiftLandingActive()) {
            boolean isJumping = isSneaking; // 蹲下上升
            boolean isDescending = !isSneaking && player.getLocation().getPitch() > 30; // 俯視下降
            
            // 計算移動方向
            Vector direction = playerLoc.getDirection();
            
            // 應用垂直移動
            if (isJumping) {
                direction.setY(Math.min(direction.getY() + 0.3, 1.0));
            } else if (isDescending) {
                direction.setY(Math.max(direction.getY() - 0.2, -0.5));
            } else {
                direction.setY(direction.getY() * 0.8); // 自然下降
            }
            
            // 應用速度
            double currentSpeed = state.getSpeed();
            Vector velocity = direction.multiply(currentSpeed);
            
            // 檢查高度限制
            double maxHeight = plugin.getConfig().getDouble("flight.max_height", 256);
            double minHeight = plugin.getConfig().getDouble("flight.min_height", 0);
            
            if (playerLoc.getY() >= maxHeight) {
                // 開始違反高度限制
                state.startHeightViolation();
                
                // 阻止繼續上升
                if (velocity.getY() > 0) {
                    velocity.setY(0);
                }
                
                // 檢查是否需要強制降落
                if (state.shouldForceLanding()) {
                    player.sendMessage("§c超過最大飛行高度10秒！正在強制降落...");
                    performForceLanding(player);
                    return;
                } else {
                    // 顯示倒計時警告
                    int remainingTime = state.getRemainingWarningTime();
                    if (state.canSendHeightWarning()) {
                        player.sendMessage("§e§l⚠ 警告：已達到最大飛行高度！");
                        player.sendMessage("§e§l" + remainingTime + " 秒後將強制降落，請立即下降！");
                    }
                }
            } else {
                // 清除高度違反狀態
                state.clearHeightViolation();
            }
            
            if (playerLoc.getY() <= minHeight && velocity.getY() < 0) {
                velocity.setY(0);
                if (state.canSendHeightWarning()) {
                    player.sendMessage("§e已達到最小飛行高度！無法繼續下降");
                }
            }
            
            // 設置玩家速度
            player.setVelocity(velocity);
            
            // 更新飛行速度（根據附魔）
            updateFlightSpeed(player, state);
        }
    }
    
    /**
     * 處理 Shift 鍵強制降落邏輯
     */
    private void handleShiftLanding(Player player, FlightState state, boolean isSneaking) {
        // 檢查功能是否啟用
        if (!plugin.getConfig().getBoolean("flight.shift_landing.enabled", true)) {
            return;
        }
        
        long delay = plugin.getConfig().getLong("flight.shift_landing.delay", 10000);
        boolean showCountdown = plugin.getConfig().getBoolean("flight.shift_landing.show_countdown", true);
        
        if (isSneaking) {
            // 開始或繼續 Shift 降落倒計時
            if (!state.isShiftLandingActive()) {
                state.startShiftLanding();
                if (showCountdown) {
                    player.sendMessage("§e§l✈ 開始強制降落倒計時...");
                    player.sendMessage("§7持續按住 Shift 鍵 " + (delay / 1000) + " 秒將強制降落");
                }
            } else {
                // 檢查是否到達強制降落時間
                if (state.shouldShiftLanding(delay)) {
                    player.sendMessage("§c§l⬇ 執行強制降落！");
                    performForceLanding(player);
                    return;
                }
                
                // 顯示倒計時（每秒顯示一次）
                if (showCountdown) {
                    int remainingTime = state.getRemainingShiftTime(delay);
                    if (remainingTime <= 5 && remainingTime > 0) {
                        player.sendMessage("§e§l" + remainingTime + " 秒後強制降落...");
                    }
                }
            }
        } else {
            // 停止 Shift 降落倒計時
            if (state.isShiftLandingActive()) {
                state.stopShiftLanding();
                if (showCountdown) {
                    player.sendMessage("§a§l✓ 已取消強制降落倒計時");
                }
            }
        }
    }
    
    /**
     * 更新飛行速度
     */
    private void updateFlightSpeed(Player player, FlightState state) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        double speedEnchant = plugin.getEnchantManager().getEnchantEffect("speed", 
            playerData.getEnchantLevel("speed"));
        
        double targetSpeed = BASE_SPEED + speedEnchant;
        targetSpeed = Math.min(targetSpeed, MAX_SPEED);
        
        double currentSpeed = state.getSpeed();
        if (currentSpeed < targetSpeed) {
            currentSpeed = Math.min(currentSpeed + ACCELERATION, targetSpeed);
        } else if (currentSpeed > targetSpeed) {
            currentSpeed = Math.max(currentSpeed - DECELERATION, targetSpeed);
        }
        
        state.setSpeed(currentSpeed);
    }
    
    /**
     * 更新飛行特效
     */
    private void updateFlightEffects(Player player, FlightState state) {
        // 飛行軌跡特效
        if (plugin.getConfig().getBoolean("effects.enable_particles", true)) {
            Location trailLoc = player.getLocation().add(0, -0.5, 0);
            plugin.getEffectManager().playFlightTrailEffect(trailLoc);
        }
        
        // 附魔特效
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        for (Map.Entry<String, Integer> entry : playerData.getEnchantLevels().entrySet()) {
            if (entry.getValue() > 0) {
                plugin.getEffectManager().playEnchantEffect(player, entry.getKey());
            }
        }
    }
    
    /**
     * 檢查障礙物
     */
    private void checkObstacles(Player player, FlightState state) {
        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection();
        
        // 檢查前方障礙物
        for (int i = 1; i <= OBSTACLE_CHECK_DISTANCE; i++) {
            Location checkLoc = playerLoc.clone().add(direction.clone().multiply(i));
            Block block = checkLoc.getBlock();
            
            if (block.getType().isSolid()) {
                // 發現障礙物，減速（但不重複提醒避免洗頻）
                double newSpeed = Math.max(state.getSpeed() * 0.8, BASE_SPEED * 0.5);
                if (state.getSpeed() > newSpeed + 0.1) { // 只有當速度真正降低時才提醒
                    state.setSpeed(newSpeed);
                    if (state.canSendObstacleWarning()) {
                        player.sendMessage("§e前方發現障礙物，自動減速！");
                    }
                }
                break;
            }
        }
    }
    
    /**
     * 執行安全降落
     */
    private boolean performSafeLanding(Player player) {
        Location playerLoc = player.getLocation();
        
        // 檢查腳下是否有安全區域
        for (int y = 0; y <= SAFE_LANDING_DISTANCE; y++) {
            Location checkLoc = playerLoc.clone().subtract(0, y, 0);
            Block block = checkLoc.getBlock();
            
            if (block.getType().isSolid()) {
                // 找到安全降落點
                Location safeLoc = checkLoc.clone().add(0, 1, 0);
                player.teleport(safeLoc);
                
                // 應用降落護盾
                applyLandingShield(player);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 應用降落護盾
     */
    private void applyLandingShield(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        int shieldLevel = playerData.getEnchantLevel("shield");
        
        if (shieldLevel > 0) {
            // 給予短暫的傷害減免
            player.setInvulnerable(true);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setInvulnerable(false);
                }
            }.runTaskLater(plugin, 20L * shieldLevel); // 每級護盾1秒無敵
            
            player.sendMessage("§b§l🛡️ 降落護盾已激活！");
        }
    }
    
    /**
     * 檢查是否在允許區域
     */
    private boolean isInAllowedArea(Player player) {
        // 這裡可以添加區域限制邏輯
        // 例如：檢查世界、檢查權限區域等
        return true;
    }
    
    /**
     * 冷卻時間管理
     */
    private boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        return System.currentTimeMillis() < cooldowns.get(uuid);
    }
    
    private long getCooldownRemaining(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        return Math.max(0, cooldowns.get(uuid) - System.currentTimeMillis());
    }
    
    private void setCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis() + COOLDOWN_TIME);
    }
    
    /**
     * 狀態檢查
     */
    public boolean isFlying(UUID uuid) {
        return activeFlights.containsKey(uuid);
    }
    
    public FlightSession getFlightSession(UUID uuid) {
        return activeFlights.get(uuid);
    }
    
    public FlightState getFlightState(UUID uuid) {
        return flightStates.get(uuid);
    }
    
    /**
     * 清理所有飛行
     */
    public void stopAllFlights() {
        for (FlightSession session : activeFlights.values()) {
            session.stop();
        }
        activeFlights.clear();
        flightStates.clear();
    }
    
    /**
     * 處理玩家離線
     */
    public void handlePlayerQuit(UUID uuid) {
        FlightSession session = activeFlights.remove(uuid);
        if (session != null) {
            session.stop();
        }
        flightStates.remove(uuid);
        cooldowns.remove(uuid);
        
        // 從調度器移除
        plugin.getFlightScheduler().removeActivePlayer(uuid);
    }
    
    /**
     * 處理玩家死亡
     */
    public void handlePlayerDeath(UUID uuid) {
        handlePlayerQuit(uuid);
    }
    
    /**
     * 執行強制降落
     */
    private void performForceLanding(Player player) {
        // 找到安全降落點
        Location safeLandingLoc = findSafeLandingLocation(player);
        
        if (safeLandingLoc != null) {
            // 平滑降落到安全位置
            player.teleport(safeLandingLoc);
            player.sendMessage("§a已安全降落到地面");
        } else {
            // 如果找不到安全位置，降落到當前位置下方的安全地點
            Location playerLoc = player.getLocation();
            for (int y = (int)playerLoc.getY(); y >= 0; y--) {
                Location checkLoc = playerLoc.clone();
                checkLoc.setY(y);
                if (checkLoc.getBlock().getType().isSolid()) {
                    checkLoc.setY(y + 1); // 在固體方塊上方
                    player.teleport(checkLoc);
                    player.sendMessage("§a已降落到安全位置");
                    break;
                }
            }
        }
        
        // 停止飛行
        stopFlight(player);
        
        // 播放降落特效
        plugin.getEffectManager().playFlightStopEffect(player);
    }
    
    /**
     * 找到安全降落位置
     */
    private Location findSafeLandingLocation(Player player) {
        Location playerLoc = player.getLocation();
        
        // 向下搜索安全降落點
        for (int y = (int)playerLoc.getY(); y >= 0; y--) {
            Location checkLoc = playerLoc.clone();
            checkLoc.setY(y);
            
            if (checkLoc.getBlock().getType().isSolid()) {
                // 找到固體方塊，檢查上方是否有足夠空間
                Location landingLoc = checkLoc.clone().add(0, 1, 0);
                if (!landingLoc.getBlock().getType().isSolid() && 
                    !landingLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                    return landingLoc;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 檢查玩家手中飛劍的耐久度是否有效
     */
    private boolean hasValidSwordDurability(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType().getMaxDurability() <= 0) {
            return true; // 如果物品沒有耐久度系統，視為有效
        }
        
        if (mainHand.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = 
                (org.bukkit.inventory.meta.Damageable) mainHand.getItemMeta();
            
            int currentDamage = damageable.getDamage();
            int maxDurability = mainHand.getType().getMaxDurability();
            
            // 如果損害值等於或超過最大耐久度，物品已損壞
            return currentDamage < maxDurability;
        }
        
        return true;
    }
}
