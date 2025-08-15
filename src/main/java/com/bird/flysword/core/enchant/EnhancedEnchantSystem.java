package com.bird.flysword.core.enchant;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;

/**
 * 進階附魔與能力模組
 * 負責處理飛劍的各種附魔效果
 */
public class EnhancedEnchantSystem {
    
    private final Flysword plugin;
    private final Map<String, EnchantEffect> enchantEffects;
    
    public EnhancedEnchantSystem(Flysword plugin) {
        this.plugin = plugin;
        this.enchantEffects = new HashMap<>();
        
        registerEnchantEffects();
    }
    
    /**
     * 註冊所有附魔效果
     */
    private void registerEnchantEffects() {
        // 飛行加速
        enchantEffects.put("fly_speed", new FlySpeedEnchant());
        
        // 飛行穩定
        enchantEffects.put("fly_stability", new FlyStabilityEnchant());
        
        // 能量回復
        enchantEffects.put("energy_recovery", new EnergyRecoveryEnchant());
        
        // 降落護盾
        enchantEffects.put("landing_shield", new LandingShieldEnchant());
        
        // 穿雲破霧
        enchantEffects.put("cloud_piercing", new CloudPiercingEnchant());
        
        // 風之庇佑
        enchantEffects.put("wind_blessing", new WindBlessingEnchant());
        
        // 雷電加速
        enchantEffects.put("lightning_boost", new LightningBoostEnchant());
        
        // 冰霜護盾
        enchantEffects.put("frost_shield", new FrostShieldEnchant());
    }
    
    /**
     * 應用飛行時的附魔效果
     */
    public FlightEnchantData applyFlightEnchants(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        Map<String, Integer> enchantLevels = playerData.getEnchantLevels();
        
        FlightEnchantData enchantData = new FlightEnchantData();
        
        for (Map.Entry<String, Integer> entry : enchantLevels.entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();
            
            if (level > 0 && enchantEffects.containsKey(enchantId)) {
                EnchantEffect effect = enchantEffects.get(enchantId);
                effect.applyFlightEffect(player, level, enchantData);
            }
        }
        
        return enchantData;
    }
    
    /**
     * 處理降落時的附魔效果
     */
    public void applyLandingEnchants(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        Map<String, Integer> enchantLevels = playerData.getEnchantLevels();
        
        for (Map.Entry<String, Integer> entry : enchantLevels.entrySet()) {
            String enchantId = entry.getKey();
            int level = entry.getValue();
            
            if (level > 0 && enchantEffects.containsKey(enchantId)) {
                EnchantEffect effect = enchantEffects.get(enchantId);
                effect.applyLandingEffect(player, level);
            }
        }
    }
    
    /**
     * 處理耐久度消耗時的附魔效果
     */
    public boolean processDurabilityConsumption(Player player, int baseCost) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        Map<String, Integer> enchantLevels = playerData.getEnchantLevels();
        
        int actualCost = baseCost;
        boolean shouldConsume = true;
        
        // 檢查能量回復附魔
        int energyRecoveryLevel = enchantLevels.getOrDefault("energy_recovery", 0);
        if (energyRecoveryLevel > 0) {
            EnchantEffect effect = enchantEffects.get("energy_recovery");
            if (effect instanceof EnergyRecoveryEnchant) {
                EnergyRecoveryEnchant energyEffect = (EnergyRecoveryEnchant) effect;
                if (energyEffect.shouldPreventConsumption(energyRecoveryLevel)) {
                    shouldConsume = false;
                }
            }
        }
        
        if (shouldConsume) {
            playerData.addDurability(-actualCost);
        }
        
        return shouldConsume;
    }
    
    /**
     * 飛行附魔數據容器
     */
    public static class FlightEnchantData {
        private double speedMultiplier = 1.0;
        private double stabilityBonus = 0.0;
        private boolean hasCloudPiercing = false;
        private boolean hasWindBlessing = false;
        private double accelerationBonus = 0.0;
        private double protectionLevel = 0.0;
        
        // Getters and Setters
        public double getSpeedMultiplier() { return speedMultiplier; }
        public void setSpeedMultiplier(double speedMultiplier) { this.speedMultiplier = speedMultiplier; }
        
        public double getStabilityBonus() { return stabilityBonus; }
        public void setStabilityBonus(double stabilityBonus) { this.stabilityBonus = stabilityBonus; }
        
        public boolean hasCloudPiercing() { return hasCloudPiercing; }
        public void setCloudPiercing(boolean cloudPiercing) { this.hasCloudPiercing = cloudPiercing; }
        
        public boolean hasWindBlessing() { return hasWindBlessing; }
        public void setWindBlessing(boolean windBlessing) { this.hasWindBlessing = windBlessing; }
        
        public double getAccelerationBonus() { return accelerationBonus; }
        public void setAccelerationBonus(double accelerationBonus) { this.accelerationBonus = accelerationBonus; }
        
        public double getProtectionLevel() { return protectionLevel; }
        public void setProtectionLevel(double protectionLevel) { this.protectionLevel = protectionLevel; }
    }
}

/**
 * 附魔效果基礎介面
 */
interface EnchantEffect {
    void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData);
    void applyLandingEffect(Player player, int level);
    String getName();
    String getDescription();
}

/**
 * 飛行加速附魔
 */
class FlySpeedEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        double speedBonus = 1.0 + (level * 0.2); // 每級增加20%速度
        enchantData.setSpeedMultiplier(enchantData.getSpeedMultiplier() * speedBonus);
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 飛行加速在降落時無效果
    }
    
    @Override
    public String getName() { return "飛行加速"; }
    
    @Override
    public String getDescription() { return "提升飛行速度"; }
}

/**
 * 飛行穩定附魔
 */
class FlyStabilityEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        double stabilityBonus = level * 0.15; // 每級提升15%穩定性
        enchantData.setStabilityBonus(enchantData.getStabilityBonus() + stabilityBonus);
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 飛行穩定在降落時無效果
    }
    
    @Override
    public String getName() { return "飛行穩定"; }
    
    @Override
    public String getDescription() { return "減少飛行時的晃動"; }
}

/**
 * 能量回復附魔
 */
class EnergyRecoveryEnchant implements EnchantEffect {
    private final Random random = new Random();
    
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        // 能量回復在飛行過程中處理
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 能量回復在降落時無效果
    }
    
    public boolean shouldPreventConsumption(int level) {
        double chance = level * 0.15; // 每級15%機率不消耗耐久
        return random.nextDouble() < chance;
    }
    
    @Override
    public String getName() { return "能量回復"; }
    
    @Override
    public String getDescription() { return "有機率不消耗耐久度"; }
}

/**
 * 降落護盾附魔
 */
class LandingShieldEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        double protection = level * 0.25; // 每級提升25%保護
        enchantData.setProtectionLevel(enchantData.getProtectionLevel() + protection);
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 給予臨時無敵效果
        int duration = level * 40; // 每級2秒無敵時間（20 ticks = 1秒）
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, level - 1));
        player.sendMessage("§a降落護盾已啟動！");
    }
    
    @Override
    public String getName() { return "降落護盾"; }
    
    @Override
    public String getDescription() { return "降落時獲得傷害抗性"; }
}

/**
 * 穿雲破霧附魔
 */
class CloudPiercingEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        enchantData.setCloudPiercing(true);
        // 在高海拔飛行時無視天氣影響
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 穿雲破霧在降落時無效果
    }
    
    @Override
    public String getName() { return "穿雲破霧"; }
    
    @Override
    public String getDescription() { return "無視天氣影響"; }
}

/**
 * 風之庇佑附魔
 */
class WindBlessingEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        enchantData.setWindBlessing(true);
        double accelerationBonus = level * 0.1; // 每級提升10%加速度
        enchantData.setAccelerationBonus(enchantData.getAccelerationBonus() + accelerationBonus);
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 風之庇佑在降落時減少掉落傷害
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
    }
    
    @Override
    public String getName() { return "風之庇佑"; }
    
    @Override
    public String getDescription() { return "提升加速度並減少掉落傷害"; }
}

/**
 * 雷電加速附魔
 */
class LightningBoostEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        if (player.getWorld().hasStorm()) {
            // 雷雨天氣下額外速度加成
            double stormBonus = 1.0 + (level * 0.3);
            enchantData.setSpeedMultiplier(enchantData.getSpeedMultiplier() * stormBonus);
        }
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 雷電加速在降落時無效果
    }
    
    @Override
    public String getName() { return "雷電加速"; }
    
    @Override
    public String getDescription() { return "雷雨天氣下獲得額外速度"; }
}

/**
 * 冰霜護盾附魔
 */
class FrostShieldEnchant implements EnchantEffect {
    @Override
    public void applyFlightEffect(Player player, int level, EnhancedEnchantSystem.FlightEnchantData enchantData) {
        double protection = level * 0.2; // 每級提升20%保護
        enchantData.setProtectionLevel(enchantData.getProtectionLevel() + protection);
    }
    
    @Override
    public void applyLandingEffect(Player player, int level) {
        // 降落時給予緩慢效果周圍敵人
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
    }
    
    @Override
    public String getName() { return "冰霜護盾"; }
    
    @Override
    public String getDescription() { return "提供保護並在降落時給予火焰抗性"; }
}
