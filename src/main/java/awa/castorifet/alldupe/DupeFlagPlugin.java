package awa.castorifet.alldupe;

import org.bukkit.plugin.java.JavaPlugin;

public class DupeFlagPlugin extends JavaPlugin {
    private static DupeFlagPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new FlagListener(this), this);
        getLogger().info("DupeFlag enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("DupeFlag disabled.");
    }

    public static DupeFlagPlugin getInstance() {
        return instance;
    }
}
