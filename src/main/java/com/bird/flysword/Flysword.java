package com.bird.flysword;

import org.bukkit.plugin.java.JavaPlugin;

import com.bird.flysword.commands.FlyswordCommand;
import com.bird.flysword.commands.FlyswordModelTabCompleter;
import com.bird.flysword.commands.FlyswordTabCompleter;
import com.bird.flysword.commands.FlyswordTestTabCompleter;
import com.bird.flysword.flight.FlightController;
import com.bird.flysword.listeners.FlyswordListener;
import com.bird.flysword.managers.DataManager;
import com.bird.flysword.managers.EffectManager;
import com.bird.flysword.managers.EnchantManager;
import com.bird.flysword.managers.FlightManager;
import com.bird.flysword.managers.SkinManager;
import com.bird.flysword.managers.UnlockItemManager;
import com.bird.flysword.scheduler.FlightScheduler;
import com.bird.flysword.utils.ArmorStandFixTestUtils;
import com.bird.flysword.utils.ConfigManager;
import com.bird.flysword.utils.SwordDisplayTestUtils;
import com.bird.flysword.utils.TestUtils;

public final class Flysword extends JavaPlugin {

    private static Flysword instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private FlightManager flightManager;
    private FlightController flightController;
    private FlightScheduler flightScheduler;
    private SkinManager skinManager;
    private EnchantManager enchantManager;
    private EffectManager effectManager;
    private UnlockItemManager unlockItemManager;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // 初始化數據管理器
        dataManager = new DataManager(this);
        dataManager.loadData();

        // 初始化皮膚管理器
        skinManager = new SkinManager(this);
        skinManager.loadSkins();

        // 初始化附魔管理器
        enchantManager = new EnchantManager(this);
        enchantManager.loadEnchants();

        // 初始化特效管理器
        effectManager = new EffectManager(this);

        // 初始化解鎖道具管理器
        unlockItemManager = new UnlockItemManager(this);

        // 初始化飛行管理器
        flightManager = new FlightManager(this);
        
        // 初始化飛行控制器
        flightController = new FlightController(this);
        
        // 初始化飛行調度器
        flightScheduler = new FlightScheduler(this);
        flightScheduler.start();

        // 註冊指令
        getCommand("flysword").setExecutor(new FlyswordCommand(this));
        getCommand("flysword").setTabCompleter(new FlyswordTabCompleter(this));

        // 註冊測試指令 (僅開發模式)
        if (getConfig().getBoolean("debug.enable_test_commands", false)) {
            getCommand("flyswordtest").setExecutor(new TestUtils(this));
            getCommand("flyswordtest").setTabCompleter(new FlyswordTestTabCompleter(this));
        }
        
        // 註冊模型測試指令 (管理員使用)
        getCommand("flyswordmodel").setExecutor(new com.bird.flysword.utils.ModelTestUtils(this));
        getCommand("flyswordmodel").setTabCompleter(new FlyswordModelTabCompleter(this));
        
        // 註冊飛行測試指令 (調試使用)
        getCommand("flysword-test").setExecutor(new com.bird.flysword.utils.FlightTestUtils(this));
        getCommand("flysword-test").setTabCompleter(new FlyswordTestTabCompleter(this));
        
        // 註冊模型顯示測試指令 (調試使用)
        getCommand("modeltest").setExecutor(new com.bird.flysword.utils.ModelDisplayTest(this));
        getCommand("modeltest").setTabCompleter(new FlyswordModelTabCompleter(this));
        
        // 註冊耐久度測試指令 (調試使用)
        getCommand("durabilitytest").setExecutor(new com.bird.flysword.utils.DurabilityTestUtils(this));
        
        // 註冊速度測試指令 (調試使用)
        getCommand("speedtest").setExecutor(new com.bird.flysword.utils.SpeedTestUtils(this));
        
        // 註冊飛劍位置調整指令 (管理員使用)
        getCommand("swordpos").setExecutor(new com.bird.flysword.commands.SwordPositionCommand(this));
        getCommand("swordpos").setTabCompleter(new com.bird.flysword.commands.SwordPositionTabCompleter());
        
        // 註冊飛劍角度調整指令 (管理員使用)
        getCommand("swordrotation").setExecutor(new com.bird.flysword.commands.SwordRotationCommand(this));
        getCommand("swordrotation").setTabCompleter(new com.bird.flysword.commands.SwordRotationTabCompleter());
        
        // 註冊 Shift 降落測試指令 (調試使用)
        getCommand("shiftlandingtest").setExecutor(new com.bird.flysword.utils.ShiftLandingTestUtils(this));
        
        // 註冊飛劍顯示測試指令 (調試使用)
        getCommand("sworddisplaytest").setExecutor(new SwordDisplayTestUtils(this));
        
        // 註冊盔甲座固定測試指令 (調試使用)
        getCommand("armorstandtest").setExecutor(new ArmorStandFixTestUtils(this));
        
        // 註冊智能飛劍數學計算指令 (管理員使用)
        getCommand("smartsword").setExecutor(new com.bird.flysword.utils.SmartSwordCommand(this));

        // 註冊監聽器
        getServer().getPluginManager().registerEvents(new FlyswordListener(this), this);
        
        // 註冊資源包檢查器
        getServer().getPluginManager().registerEvents(new com.bird.flysword.utils.ResourcePackChecker(this), this);

        getLogger().info("飛劍系統已啟動！");
    }

    @Override
    public void onDisable() {
        // 停止飛行調度器
        if (flightScheduler != null) {
            flightScheduler.stop();
        }
        
        // 保存所有數據
        if (dataManager != null) {
            dataManager.saveData();
        }

        // 停止所有飛行
        if (flightManager != null) {
            flightManager.stopAllFlights();
        }
        
        if (flightController != null) {
            flightController.stopAllFlights();
        }

        getLogger().info("飛劍系統已關閉！");
    }

    // Getter 方法
    public static Flysword getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public FlightManager getFlightManager() {
        return flightManager;
    }
    
    public FlightController getFlightController() {
        return flightController;
    }
    
    public FlightScheduler getFlightScheduler() {
        return flightScheduler;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    public UnlockItemManager getUnlockItemManager() {
        return unlockItemManager;
    }
}
