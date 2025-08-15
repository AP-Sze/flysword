package com.bird.flysword.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.gui.SkinMenu;

public class FlyswordListener implements Listener {

    private final Flysword plugin;
    private final NamespacedKey skinKey;
    private final NamespacedKey unlockKey;

    public FlyswordListener(Flysword plugin) {
        this.plugin = plugin;
        this.skinKey = new NamespacedKey(plugin, "skin_id");
        this.unlockKey = new NamespacedKey(plugin, "unlock_item");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // 檢查是否為解鎖道具
        if (plugin.getUnlockItemManager().isUnlockItem(item)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.getUnlockItemManager().useUnlockItem(player, item);
                return;
            }
        }

        // 檢查是否為鑽石劍
        if (item.getType() != Material.DIAMOND_SWORD) {
            return;
        }

        // 檢查是否為右鍵
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 檢查是否為飛劍
        if (!isFlysword(item)) {
            // 靜默返回，不發送訊息避免洗頻
            return;
        }

        event.setCancelled(true);
        handleFlyswordUse(player);
    }

    private boolean isFlysword(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // 檢查是否有皮膚標記 - 這樣更準確
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(skinKey, PersistentDataType.STRING);
    }

    private void handleFlyswordUse(Player player) {
        // 使用新的飛行控制器
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            // 停止飛行
            plugin.getFlightController().stopFlight(player);
        } else {
            // 開始飛行
            plugin.getFlightController().startFlight(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);

        // 如果玩家上次在飛行中，重置狀態
        if (playerData.isFlying()) {
            playerData.setFlying(false);
            plugin.getDataManager().savePlayerData(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getFlightController().handlePlayerQuit(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getFlightController().handlePlayerDeath(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§6§l飛劍皮膚選單")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                SkinMenu skinMenu = new SkinMenu(plugin);
                skinMenu.handleClick(player, event.getRawSlot());
            }
        }
    }
}
