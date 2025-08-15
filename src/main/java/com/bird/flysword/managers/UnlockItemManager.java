package com.bird.flysword.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.data.SwordSkin;

public class UnlockItemManager {
    
    private final Flysword plugin;
    private final Map<String, UnlockItem> unlockItems;
    private final NamespacedKey unlockKey;
    private final NamespacedKey skinIdKey;
    
    public UnlockItemManager(Flysword plugin) {
        this.plugin = plugin;
        this.unlockItems = new HashMap<>();
        this.unlockKey = new NamespacedKey(plugin, "unlock_item");
        this.skinIdKey = new NamespacedKey(plugin, "unlock_skin_id");
        
        loadUnlockItems();
    }
    
    /**
     * 載入解鎖道具配置
     */
    private void loadUnlockItems() {
        // 從皮膚配置中載入解鎖道具
        for (SwordSkin skin : plugin.getSkinManager().getAllSkins().values()) {
            if ("item".equals(skin.getUnlockType()) && !skin.getUnlockValue().isEmpty()) {
                UnlockItem unlockItem = new UnlockItem(
                    skin.getUnlockValue(),
                    skin.getId(),
                    skin.getDisplayName(),
                    skin.getDescription()
                );
                unlockItems.put(skin.getUnlockValue(), unlockItem);
            }
        }
        
        plugin.getLogger().info("已載入 " + unlockItems.size() + " 個解鎖道具");
    }
    
    /**
     * 創建解鎖道具
     */
    public ItemStack createUnlockItem(String unlockItemId) {
        UnlockItem unlockItem = unlockItems.get(unlockItemId);
        if (unlockItem == null) {
            return null;
        }
        
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l" + unlockItem.getDisplayName() + " 解鎖券");
            
            meta.setLore(java.util.Arrays.asList(
                "§7" + unlockItem.getDescription(),
                "",
                "§e右鍵使用解鎖對應皮膚",
                "§7皮膚ID: " + unlockItem.getSkinId(),
                "",
                "§c注意：使用後道具將消失"
            ));
            
            // 設置持久化數據
            meta.getPersistentDataContainer().set(unlockKey, PersistentDataType.STRING, unlockItemId);
            meta.getPersistentDataContainer().set(skinIdKey, PersistentDataType.STRING, unlockItem.getSkinId());
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 檢查是否為解鎖道具
     */
    public boolean isUnlockItem(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(unlockKey, PersistentDataType.STRING);
    }
    
    /**
     * 使用解鎖道具
     */
    public boolean useUnlockItem(Player player, ItemStack item) {
        if (!isUnlockItem(item)) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        String unlockItemId = meta.getPersistentDataContainer().get(unlockKey, PersistentDataType.STRING);
        String skinId = meta.getPersistentDataContainer().get(skinIdKey, PersistentDataType.STRING);
        
        if (unlockItemId == null || skinId == null) {
            return false;
        }
        
        // 檢查皮膚是否存在
        if (!plugin.getSkinManager().hasSkin(skinId)) {
            player.sendMessage("§c錯誤：找不到對應的皮膚！");
            return false;
        }
        
        // 檢查玩家是否已擁有此皮膚
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        if (playerData.hasSkin(skinId)) {
            player.sendMessage("§e您已經擁有此皮膚了！");
            return false;
        }
        
        // 解鎖皮膚
        playerData.unlockSkin(skinId);
        plugin.getDataManager().savePlayerData(player);
        
        // 移除道具
        item.setAmount(item.getAmount() - 1);
        
        // 播放解鎖特效
        plugin.getEffectManager().playSkinUnlockEffect(player, skinId);
        
        // 發送消息
        SwordSkin skin = plugin.getSkinManager().getSkin(skinId);
        player.sendMessage("§a§l✨ 恭喜！您解鎖了新皮膚: " + skin.getDisplayName());
        player.sendMessage("§7使用 /flysword menu 查看並選擇新皮膚");
        
        return true;
    }
    
    /**
     * 批量發放解鎖道具
     */
    public void giveUnlockItem(Player player, String unlockItemId, int amount) {
        UnlockItem unlockItem = unlockItems.get(unlockItemId);
        if (unlockItem == null) {
            player.sendMessage("§c錯誤：找不到解鎖道具 " + unlockItemId);
            return;
        }
        
        ItemStack item = createUnlockItem(unlockItemId);
        if (item == null) {
            player.sendMessage("§c錯誤：創建解鎖道具失敗");
            return;
        }
        
        item.setAmount(amount);
        player.getInventory().addItem(item);
        
        player.sendMessage("§a您收到了 " + amount + " 個 " + unlockItem.getDisplayName() + " 解鎖券");
    }
    
    /**
     * 獲取所有解鎖道具
     */
    public Map<String, UnlockItem> getAllUnlockItems() {
        return unlockItems;
    }
    
    /**
     * 獲取所有解鎖道具ID
     */
    public java.util.Set<String> getAllUnlockItemIds() {
        return unlockItems.keySet();
    }
    
    /**
     * 檢查解鎖道具是否存在
     */
    public boolean hasUnlockItem(String unlockItemId) {
        return unlockItems.containsKey(unlockItemId);
    }
    
    /**
     * 重新載入解鎖道具
     */
    public void reloadUnlockItems() {
        unlockItems.clear();
        loadUnlockItems();
    }
    
    /**
     * 解鎖道具數據類
     */
    public static class UnlockItem {
        private final String id;
        private final String skinId;
        private final String displayName;
        private final String description;
        
        public UnlockItem(String id, String skinId, String displayName, String description) {
            this.id = id;
            this.skinId = skinId;
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getId() {
            return id;
        }
        
        public String getSkinId() {
            return skinId;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
