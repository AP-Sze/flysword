package com.bird.flysword.core.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.bird.flysword.Flysword;

/**
 * 多線程與性能優化模組
 * 負責管理插件性能，避免卡服
 */
public class PerformanceOptimizer {
    
    private final Flysword plugin;
    private final ExecutorService asyncExecutor;
    private final Map<String, Long> performanceMetrics;
    private final Set<UUID> trackedArmorStands;
    
    // 性能設定
    private double maxTPS = 18.0;
    private int maxParticlesPerTick = 100;
    private int maxSoundsPerTick = 20;
    private boolean adaptiveMode = true;
    
    // 性能統計
    private long lastPerformanceCheck = 0;
    private final Queue<Double> recentTPSValues = new LinkedList<>();
    private int currentParticleCount = 0;
    private int currentSoundCount = 0;
    
    public PerformanceOptimizer(Flysword plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread thread = new Thread(r, "FlySword-Async");
            thread.setDaemon(true);
            return thread;
        });
        this.performanceMetrics = new ConcurrentHashMap<>();
        this.trackedArmorStands = ConcurrentHashMap.newKeySet();
        
        startPerformanceMonitoring();
        startCleanupTask();
    }
    
    /**
     * 啟動性能監控
     */
    private void startPerformanceMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePerformanceMetrics();
                
                if (adaptiveMode) {
                    adaptPerformanceSettings();
                }
                
                // 重置每tick計數器
                currentParticleCount = 0;
                currentSoundCount = 0;
            }
        }.runTaskTimer(plugin, 0L, 1L); // 每tick執行
    }
    
    /**
     * 更新性能指標
     */
    private void updatePerformanceMetrics() {
        long currentTime = System.currentTimeMillis();
        
        // 每5秒更新一次詳細統計
        if (currentTime - lastPerformanceCheck >= 5000) {
            lastPerformanceCheck = currentTime;
            
            // 獲取TPS
            double currentTPS = getCurrentTPS();
            recentTPSValues.offer(currentTPS);
            if (recentTPSValues.size() > 12) { // 保留最近1分鐘的數據
                recentTPSValues.poll();
            }
            
            // 更新性能指標
            performanceMetrics.put("tps", Math.round(currentTPS * 100.0) / 100);
            performanceMetrics.put("online_players", (long) Bukkit.getOnlinePlayers().size());
            performanceMetrics.put("armor_stands", (long) trackedArmorStands.size());
            
            // 記錄到日誌（如果性能過低）
            if (currentTPS < maxTPS * 0.8) {
                plugin.getLogger().warning(String.format(
                    "性能警告: TPS=%.2f, 在線玩家=%d, 活躍飛行=%d, 盔甲架=%d",
                    currentTPS,
                    Bukkit.getOnlinePlayers().size(),
                    0, // TODO: Replace with actual active flight count if available
                    trackedArmorStands.size()
                ));
            }
        }
    }
    
    /**
     * 自適應性能設定
     */
    private void adaptPerformanceSettings() {
        double avgTPS = getAverageTPS();
        int onlineCount = Bukkit.getOnlinePlayers().size();
        
        if (avgTPS < maxTPS * 0.7) {
            // 嚴重性能問題，大幅降低特效
            maxParticlesPerTick = Math.max(20, maxParticlesPerTick - 10);
            maxSoundsPerTick = Math.max(5, maxSoundsPerTick - 2);
            
        } else if (avgTPS < maxTPS * 0.85) {
            // 輕微性能問題，小幅降低特效
            maxParticlesPerTick = Math.max(50, maxParticlesPerTick - 5);
            maxSoundsPerTick = Math.max(10, maxSoundsPerTick - 1);
            
        } else if (avgTPS > maxTPS * 0.95 && onlineCount < 50) {
            // 性能良好，可以增加特效
            maxParticlesPerTick = Math.min(200, maxParticlesPerTick + 5);
            maxSoundsPerTick = Math.min(30, maxSoundsPerTick + 1);
        }
    }
    
    /**
     * 獲取當前 TPS
     */
    private double getCurrentTPS() {
        try {
            // 這裡使用反射獲取 TPS，實際實現可能因版本而異
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            Object[] recentTps = (Object[]) server.getClass().getField("recentTps").get(server);
            return Math.min(20.0, (Double) recentTps[0]);
        } catch (Exception e) {
            // 如果無法獲取 TPS，返回默認值
            return 20.0;
        }
    }
    
    /**
     * 獲取平均 TPS
     */
    private double getAverageTPS() {
        if (recentTPSValues.isEmpty()) {
            return 20.0;
        }
        
        double sum = 0;
        for (double tps : recentTPSValues) {
            sum += tps;
        }
        return sum / recentTPSValues.size();
    }
    
    /**
     * 檢查是否可以播放粒子特效
     */
    public boolean canShowParticle() {
        return currentParticleCount < maxParticlesPerTick;
    }
    
    /**
     * 記錄粒子特效使用
     */
    public void recordParticleUsage() {
        currentParticleCount++;
    }
    
    /**
     * 檢查是否可以播放音效
     */
    public boolean canPlaySound() {
        return currentSoundCount < maxSoundsPerTick;
    }
    
    /**
     * 記錄音效使用
     */
    public void recordSoundUsage() {
        currentSoundCount++;
    }
    
    /**
     * 異步執行任務
     */
    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, asyncExecutor);
    }
    
    /**
     * 異步執行有返回值的任務
     */
    public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncExecutor);
    }
    
    /**
     * 批量處理任務
     */
    public <T> CompletableFuture<List<T>> batchProcess(List<T> items, 
            java.util.function.Function<T, T> processor, int batchSize) {
        
        List<CompletableFuture<List<T>>> futures = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, end);
            
            CompletableFuture<List<T>> future = supplyAsync(() -> {
                List<T> processed = new ArrayList<>();
                for (T item : batch) {
                    processed.add(processor.apply(item));
                }
                return processed;
            });
            
            futures.add(future);
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<T> result = new ArrayList<>();
                    for (CompletableFuture<List<T>> future : futures) {
                        result.addAll(future.join());
                    }
                    return result;
                });
    }
    
    /**
     * 註冊需要追蹤的盔甲架
     */
    public void trackArmorStand(ArmorStand armorStand) {
        trackedArmorStands.add(armorStand.getUniqueId());
    }
    
    /**
     * 取消追蹤盔甲架
     */
    public void untrackArmorStand(ArmorStand armorStand) {
        trackedArmorStands.remove(armorStand.getUniqueId());
    }
    
    /**
     * 清理殘留的實體
     */
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupOrphanedArmorStands();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // 每分鐘執行一次
    }
    
    /**
     * 清理孤立的盔甲架
     */
    private void cleanupOrphanedArmorStands() {
        runAsync(() -> {
            Set<UUID> toRemove = new HashSet<>();
            
            for (UUID armorStandId : trackedArmorStands) {
                boolean found = false;
                
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getUniqueId().equals(armorStandId) && entity instanceof ArmorStand && entity.isValid()) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                
                if (!found) {
                    toRemove.add(armorStandId);
                }
            }
            
            if (!toRemove.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    trackedArmorStands.removeAll(toRemove);
                    plugin.getLogger().info("清理了 " + toRemove.size() + " 個孤立的盔甲架記錄");
                });
            }
        });
    }
    
    /**
     * 強制清理所有追蹤的盔甲架
     */
    public void forceCleanupAllArmorStands() {
        runAsync(() -> {
            int removedCount = 0;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof ArmorStand && 
                        trackedArmorStands.contains(entity.getUniqueId())) {
                        
                        Bukkit.getScheduler().runTask(plugin, entity::remove);
                        removedCount++;
                    }
                }
            }
            
            final int finalRemovedCount = removedCount;
            Bukkit.getScheduler().runTask(plugin, () -> {
                trackedArmorStands.clear();
                plugin.getLogger().info("強制清理了 " + finalRemovedCount + " 個盔甲架");
            });
        });
    }
    
    /**
     * 獲取性能統計
     */
    public String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== FlySword 性能統計 ===\n");
        stats.append(String.format("平均TPS: %.2f\n", getAverageTPS()));
        stats.append(String.format("在線玩家: %d\n", Bukkit.getOnlinePlayers().size()));
        stats.append(String.format("活躍飛行: %d\n", 0)); // TODO: Replace 0 with actual active flight count if available
        stats.append(String.format("追蹤盔甲架: %d\n", trackedArmorStands.size()));
        stats.append(String.format("最大粒子/tick: %d\n", maxParticlesPerTick));
        stats.append(String.format("最大音效/tick: %d\n", maxSoundsPerTick));
        stats.append(String.format("自適應模式: %s\n", adaptiveMode ? "開啟" : "關閉"));
        
        return stats.toString();
    }
    
    /**
     * 關閉性能優化器
     */
    public void shutdown() {
        asyncExecutor.shutdown();
        forceCleanupAllArmorStands();
    }
    
    // Getters and Setters
    public double getMaxTPS() { return maxTPS; }
    public void setMaxTPS(double maxTPS) { this.maxTPS = maxTPS; }
    
    public int getMaxParticlesPerTick() { return maxParticlesPerTick; }
    public void setMaxParticlesPerTick(int maxParticlesPerTick) { this.maxParticlesPerTick = maxParticlesPerTick; }
    
    public int getMaxSoundsPerTick() { return maxSoundsPerTick; }
    public void setMaxSoundsPerTick(int maxSoundsPerTick) { this.maxSoundsPerTick = maxSoundsPerTick; }
    
    public boolean isAdaptiveMode() { return adaptiveMode; }
    public void setAdaptiveMode(boolean adaptiveMode) { this.adaptiveMode = adaptiveMode; }
    
    public Map<String, Long> getPerformanceMetrics() { return new HashMap<>(performanceMetrics); }
}
