package com.bird.flysword.managers;

import com.bird.flysword.Flysword;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EffectManager {
    
    private final Flysword plugin;
    
    public EffectManager(Flysword plugin) {
        this.plugin = plugin;
    }
    
    public void playFlightStartEffect(Player player) {
        Location loc = player.getLocation();
        
        // 視覺特效
        player.getWorld().spawnParticle(Particle.FIREWORK, loc, 50, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 20, 0.3, 0.5, 0.3, 0.05);
        
        // 音效
        player.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.5f);
        player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
    }
    
    public void playFlightStopEffect(Player player) {
        Location loc = player.getLocation();
        
        // 視覺特效
        player.getWorld().spawnParticle(Particle.SMOKE, loc, 30, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 15, 0.2, 0.3, 0.2, 0.02);
        
        // 音效
        player.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 0.8f);
    }
    
    public void playSkinUnlockEffect(Player player, String skinId) {
        Location loc = player.getLocation();
        
        // 視覺特效
        player.getWorld().spawnParticle(Particle.FIREWORK, loc, 100, 0.5, 1, 0.5, 0.2);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.3, 0.5, 0.3, 0.1);
        
        // 音效
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
    }
    
    public void playFlightTrailEffect(Location location) {
        location.getWorld().spawnParticle(Particle.FIREWORK, location, 3, 0.1, 0.1, 0.1, 0.01);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 1, 0.05, 0.05, 0.05, 0.005);
    }
    
    public void playDurabilityWarningEffect(Player player) {
        Location loc = player.getLocation();
        
        // 警告特效
        player.getWorld().spawnParticle(Particle.SMOKE, loc, 10, 0.2, 0.5, 0.2, 0);
        player.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
    }
    
    public void playEnchantEffect(Player player, String enchantId) {
        Location loc = player.getLocation();
        
        // 根據附魔類型播放不同特效
        switch (enchantId) {
            case "speed":
                player.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.3, 0.5, 0.3, 0.1);
                player.getWorld().playSound(loc, Sound.ENTITY_HORSE_GALLOP, 0.3f, 1.5f);
                break;
            case "stability":
                player.getWorld().spawnParticle(Particle.SMOKE, loc, 15, 0.2, 0.3, 0.2, 0.05);
                player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_USE, 0.2f, 1.0f);
                break;
            case "regen":
                player.getWorld().spawnParticle(Particle.HEART, loc, 5, 0.2, 0.3, 0.2, 0);
                player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1.5f);
                break;
            case "shield":
                player.getWorld().spawnParticle(Particle.SMOKE, loc, 10, 0.3, 0.5, 0.3, 0);
                player.getWorld().playSound(loc, Sound.BLOCK_GLASS_PLACE, 0.3f, 0.8f);
                break;
        }
    }
}
