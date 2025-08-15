package com.bird.flysword.flight;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;

public class FlightSession {
    
    private final Flysword plugin;
    private final Player player;
    private final PlayerData playerData;
    private ArmorStand swordEntity;
    private final Map<String, Double> enchantEffects;
    private boolean isActive;
    private int taskId;
    
    public FlightSession(Flysword plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getDataManager().getPlayerData(player);
        this.enchantEffects = new HashMap<>();
        this.isActive = true;
        
        loadEnchantEffects();
        spawnSwordEntity();
        startUpdateTask();
    }
    
    private void loadEnchantEffects() {
        for (Map.Entry<String, Integer> entry : playerData.getEnchantLevels().entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();
            double effect = plugin.getEnchantManager().getEnchantEffect(enchantId, level);
            enchantEffects.put(enchantId, effect);
        }
    }
    
    private void spawnSwordEntity() {
        Location playerLoc = player.getLocation();
        
        // 從配置文件讀取劍的位置偏移
        double offsetX = plugin.getConfig().getDouble("flight.sword_offset.x", 0.0);
        double offsetY = plugin.getConfig().getDouble("flight.sword_offset.y", -2.0);
        double offsetZ = plugin.getConfig().getDouble("flight.sword_offset.z", 0.0);
        
        Location swordLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);
        
        swordEntity = player.getWorld().spawn(swordLoc, ArmorStand.class);
        
        // 關鍵：設置盔甲座為完全固定狀態
        swordEntity.setVisible(false);          // 隱藏盔甲座本體
        swordEntity.setGravity(false);          // 禁用重力
        swordEntity.setInvulnerable(true);      // 無敵狀態
        swordEntity.setMarker(true);            // 標記狀態（無碰撞箱）
        swordEntity.setSmall(true);             // 小尺寸
        swordEntity.setBasePlate(false);        // 隱藏底盤
        swordEntity.setArms(false);             // 隱藏手臂（頭盔模式不需要）
        swordEntity.setCanPickupItems(false);   // 禁止拾取物品
        swordEntity.setSilent(true);            // 靜音
        swordEntity.setPersistent(false);       // 不持久化
        
        // 設置AI為空，防止移動
        swordEntity.setAI(false);
        
        // 設置飛劍物品
        String selectedSkin = playerData.getSelectedSkin();
        ItemStack swordItem = plugin.getSkinManager().createSwordWithSkin(selectedSkin);
        
        // 確保物品正確設置
        if (swordItem != null) {
            var skin = plugin.getSkinManager().getSkin(selectedSkin);
            if (skin != null && swordItem.hasItemMeta()) {
                var meta = swordItem.getItemMeta();
                if (meta != null) {
                    // 對於 customModelData > 0 的情況設置自定義模型
                    if (skin.getCustomModelData() > 0) {
                        meta.setCustomModelData(skin.getCustomModelData());
                        swordItem.setItemMeta(meta);
                    }
                    // customModelData = 0 時使用原版模型，不需要特殊處理
                }
            }
        }
        
        // 設置到頭盔位置而不是主手
        if (swordEntity.getEquipment() != null) {
            swordEntity.getEquipment().setHelmet(swordItem);
        }
        
        // 從配置讀取角度設定
        double rotX = Math.toRadians(plugin.getConfig().getDouble("flight.sword_rotation.x", 0));
        double rotY = Math.toRadians(plugin.getConfig().getDouble("flight.sword_rotation.y", 0));
        double rotZ = Math.toRadians(plugin.getConfig().getDouble("flight.sword_rotation.z", 0));
        
        // 設置頭部姿勢來控制飛劍角度
        swordEntity.setHeadPose(new org.bukkit.util.EulerAngle(rotX, rotY, rotZ));
        
        plugin.getLogger().info("為玩家 " + player.getName() + " 生成飛劍實體，皮膚: " + selectedSkin + ", 盔甲座已固定在玩家腳下");
    }
    
    private void startUpdateTask() {
        taskId = new BukkitRunnable() {
            private int tickCounter = 0;
            
            @Override
            public void run() {
                if (!isActive || !player.isOnline()) {
                    // 通過 FlightController 正確停止飛行
                    plugin.getFlightController().stopFlight(player);
                    return;
                }
                
                // 每 tick 更新位置和特效（平滑）
                updateSwordPosition();
                applyFlightEffects();
                
                // 每秒（20 tick）消耗一次耐久度
                tickCounter++;
                if (tickCounter >= 20) {
                    consumeDurability();
                    tickCounter = 0;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L).getTaskId();
    }
    
    private void updateSwordPosition() {
        if (swordEntity == null || swordEntity.isDead()) {
            return;
        }
        
        Location playerLoc = player.getLocation();
        if (playerLoc == null) {
            return;
        }
        
        // 檢查是否啟用智能計算
        boolean smartCalculationEnabled = plugin.getConfig().getBoolean("flight.smart_calculation.enabled", false);
        
        Vector swordOffset;
        Vector swordRotation;
        
        if (smartCalculationEnabled) {
            // 使用智能數學計算
            com.bird.flysword.utils.SwordMathUtils.SwordTransform transform = 
                com.bird.flysword.utils.SwordMathUtils.calculateOptimalSwordTransform(player);
            
            swordOffset = transform.position;
            swordRotation = transform.rotation;
            
        } else {
            // 使用配置檔中的固定值
            double offsetX = plugin.getConfig().getDouble("flight.sword_offset.x", 0.0);
            double offsetY = plugin.getConfig().getDouble("flight.sword_offset.y", -2.0);
            double offsetZ = plugin.getConfig().getDouble("flight.sword_offset.z", 0.0);
            swordOffset = new org.bukkit.util.Vector(offsetX, offsetY, offsetZ);
            
            double rotX = plugin.getConfig().getDouble("flight.sword_rotation.x", 0);
            double rotY = plugin.getConfig().getDouble("flight.sword_rotation.y", 0);
            double rotZ = plugin.getConfig().getDouble("flight.sword_rotation.z", 0);
            swordRotation = new org.bukkit.util.Vector(rotX, rotY, rotZ);
        }
        
        // 計算目標位置
        Location targetLoc = playerLoc.clone().add(swordOffset);
        
        // 強制傳送到目標位置，不使用任何平滑移動
        swordEntity.teleport(targetLoc);
        
        // 確保盔甲座保持固定狀態
        swordEntity.setGravity(false);
        swordEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        
        // 保持盔甲架的朝向與玩家一致，但不影響飛劍角度
        swordEntity.setRotation(playerLoc.getYaw(), 0);
        
        // 應用計算出的飛劍角度
        double rotXRad = Math.toRadians(swordRotation.getX());
        double rotYRad = Math.toRadians(swordRotation.getY());
        double rotZRad = Math.toRadians(swordRotation.getZ());
        
        swordEntity.setHeadPose(new org.bukkit.util.EulerAngle(rotXRad, rotYRad, rotZRad));
    }
    
    private void applyFlightEffects() {
        // 飛行加速效果
        double speedEffect = enchantEffects.getOrDefault("speed", 0.0);
        if (speedEffect > 0) {
            Vector velocity = player.getVelocity();
            velocity.multiply(1.0 + speedEffect);
            player.setVelocity(velocity);
        }
        
        // 飛行穩定效果
        double stabilityEffect = enchantEffects.getOrDefault("stability", 0.0);
        if (stabilityEffect > 0) {
            // 減少飛行搖晃
            Location loc = player.getLocation();
            loc.setPitch((float)(loc.getPitch() * (1.0 - stabilityEffect)));
            player.teleport(loc);
        }
        
        // 能量回復效果（修復物品耐久度）
        double regenEffect = enchantEffects.getOrDefault("regen", 0.0);
        if (regenEffect > 0) {
            repairSword(regenEffect);
        }
    }
    
    private void consumeDurability() {
        // 消耗物品本身的耐久度
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType().getMaxDurability() > 0) {
            org.bukkit.inventory.meta.Damageable damageable = 
                (org.bukkit.inventory.meta.Damageable) mainHand.getItemMeta();
            
            if (damageable != null) {
                int currentDamage = damageable.getDamage();
                int maxDurability = mainHand.getType().getMaxDurability();
                
                // 每秒消耗1點耐久度（增加損害值）
                if (currentDamage < maxDurability) {
                    damageable.setDamage(currentDamage + 1);
                    mainHand.setItemMeta(damageable);
                    player.getInventory().setItemInMainHand(mainHand);
                    
                    // 如果耐久度已滿（物品損壞），通知 FlightController
                    if (damageable.getDamage() >= maxDurability) {
                        plugin.getFlightController().stopFlight(player);
                        player.sendMessage("§c飛劍耐久度已耗盡！");
                    }
                }
            }
        }
    }
    
    private void repairSword(double regenEffect) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType().getMaxDurability() > 0) {
            if (mainHand.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                org.bukkit.inventory.meta.Damageable damageable = 
                    (org.bukkit.inventory.meta.Damageable) mainHand.getItemMeta();
                
                int currentDamage = damageable.getDamage();
                if (currentDamage > 0) {
                    // 根據效果強度修復耐久度
                    int repairAmount = Math.max(1, (int)(regenEffect * 5));
                    int newDamage = Math.max(0, currentDamage - repairAmount);
                    
                    damageable.setDamage(newDamage);
                    mainHand.setItemMeta(damageable);
                    player.getInventory().setItemInMainHand(mainHand);
                }
            }
        }
    }
    
    public void stop() {
        isActive = false;
        
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        if (swordEntity != null && !swordEntity.isDead()) {
            swordEntity.remove();
            swordEntity = null;
        }
        
        playerData.setFlying(false);
    }
    
    public void update() {
        // 更新附魔效果
        loadEnchantEffects();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public ArmorStand getSwordEntity() {
        return swordEntity;
    }
}
