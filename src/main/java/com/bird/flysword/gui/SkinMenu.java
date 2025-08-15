package com.bird.flysword.gui;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.data.SwordSkin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkinMenu {
    
    private final Flysword plugin;
    private static final String MENU_TITLE = "§6§l飛劍皮膚選單";
    private static final int MENU_SIZE = 54;
    
    public SkinMenu(Flysword plugin) {
        this.plugin = plugin;
    }
    
    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);
        
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        String selectedSkin = playerData.getSelectedSkin();
        
        int slot = 0;
        for (SwordSkin skin : plugin.getSkinManager().getAllSkins().values()) {
            if (slot >= MENU_SIZE - 9) break; // 保留底部一行
            
            ItemStack item = createSkinItem(skin, playerData.hasSkin(skin.getId()), skin.getId().equals(selectedSkin));
            menu.setItem(slot, item);
            slot++;
        }
        
        // 添加底部控制按鈕
        addControlButtons(menu, playerData);
        
        player.openInventory(menu);
    }
    
    private ItemStack createSkinItem(SwordSkin skin, boolean unlocked, boolean selected) {
        Material material = unlocked ? Material.DIAMOND_SWORD : Material.BARRIER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName = skin.getDisplayName();
            if (selected) {
                displayName = "§a§l✓ " + displayName + " §a(已選擇)";
            } else if (unlocked) {
                displayName = "§e" + displayName;
            } else {
                displayName = "§c" + displayName + " §7(未解鎖)";
            }
            
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + skin.getDescription());
            lore.add("");
            
            if (unlocked) {
                if (selected) {
                    lore.add("§a✓ 已選擇此皮膚");
                } else {
                    lore.add("§e點擊選擇此皮膚");
                }
            } else {
                lore.add("§c您尚未解鎖此皮膚");
                lore.add("§7解鎖方式: " + getUnlockTypeText(skin.getUnlockType()));
            }
            
            meta.setLore(lore);
            
            if (unlocked) {
                meta.setCustomModelData(skin.getCustomModelData());
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String getUnlockTypeText(String unlockType) {
        switch (unlockType) {
            case "default":
                return "默認解鎖";
            case "item":
                return "使用解鎖道具";
            case "vip":
                return "VIP專屬";
            case "achievement":
                return "成就解鎖";
            case "shop":
                return "商城購買";
            case "event":
                return "活動獎勵";
            default:
                return "未知方式";
        }
    }
    
    private void addControlButtons(Inventory menu, PlayerData playerData) {
        // 耐久度顯示
        ItemStack durabilityItem = new ItemStack(Material.ANVIL);
        ItemMeta durabilityMeta = durabilityItem.getItemMeta();
        if (durabilityMeta != null) {
            durabilityMeta.setDisplayName("§6耐久度");
            List<String> durabilityLore = new ArrayList<>();
            durabilityLore.add("§7當前耐久度: §a" + playerData.getDurability() + "%");
            durabilityLore.add("§7飛行狀態: " + (playerData.isFlying() ? "§a飛行中" : "§c未飛行"));
            durabilityMeta.setLore(durabilityLore);
            durabilityItem.setItemMeta(durabilityMeta);
        }
        menu.setItem(MENU_SIZE - 9, durabilityItem);
        
        // 關閉按鈕
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c關閉選單");
            closeItem.setItemMeta(closeMeta);
        }
        menu.setItem(MENU_SIZE - 5, closeItem);
        
        // 幫助按鈕
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        if (helpMeta != null) {
            helpMeta.setDisplayName("§e幫助");
            List<String> helpLore = new ArrayList<>();
            helpLore.add("§7點擊皮膚進行選擇");
            helpLore.add("§7綠色勾號表示已選擇");
            helpLore.add("§7紅色表示未解鎖");
            helpMeta.setLore(helpLore);
            helpItem.setItemMeta(helpMeta);
        }
        menu.setItem(MENU_SIZE - 1, helpItem);
    }
    
    public void handleClick(Player player, int slot) {
        if (slot >= MENU_SIZE - 9) {
            // 底部控制按鈕
            if (slot == MENU_SIZE - 5) {
                player.closeInventory();
            }
            return;
        }
        
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        List<SwordSkin> skins = new ArrayList<>(plugin.getSkinManager().getAllSkins().values());
        
        if (slot >= skins.size()) {
            return;
        }
        
        SwordSkin clickedSkin = skins.get(slot);
        
        if (!playerData.hasSkin(clickedSkin.getId())) {
            player.sendMessage("§c您尚未解鎖此皮膚！");
            return;
        }
        
        if (clickedSkin.getId().equals(playerData.getSelectedSkin())) {
            player.sendMessage("§e您已經選擇了此皮膚！");
            return;
        }
        
        playerData.setSelectedSkin(clickedSkin.getId());
        plugin.getDataManager().savePlayerData(player);
        
        player.sendMessage("§a已選擇皮膚: " + clickedSkin.getDisplayName());
        player.closeInventory();
        
        // 播放選擇特效
        plugin.getEffectManager().playSkinUnlockEffect(player, clickedSkin.getId());
    }
}
