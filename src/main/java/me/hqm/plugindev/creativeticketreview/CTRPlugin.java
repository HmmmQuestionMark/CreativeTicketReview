package me.hqm.plugindev.creativeticketreview;

import com.iciql.Db;
import me.hqm.plugindev.creativeticketreview.command.CTRCommand;
import me.hqm.plugindev.creativeticketreview.command.ReviewCommand;
import me.hqm.plugindev.creativeticketreview.model.PlayerModel;
import me.hqm.plugindev.creativeticketreview.model.TicketModel;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CTRPlugin extends JavaPlugin {
    // -- STATIC ACCESS -- //

    private static CTRPlugin INST;

    public static CTRPlugin getInst() {
        return INST;
    }

    // -- LISTENERS & COMMANDS //

    private void loadCommands() {
        getCommand("ctr").setExecutor(new CTRCommand());
        getCommand("review").setExecutor(new ReviewCommand());
    }

    private void loadListeners() {
        PluginManager registry = getServer().getPluginManager();
        registry.registerEvents(new CTRListener(), this);
    }

    // -- BUKKIT PLUGIN METHODS -- //

    public void onEnable() {
        INST = this;

        loadSQL();
        loadListeners();
        loadCommands();

        Db.open(CTRSetting.DATABASE_URL.getValue()).from(new PlayerModel()).select();
        Db.open(CTRSetting.DATABASE_URL.getValue()).from(new TicketModel()).select();

        getConfig().options().copyDefaults(true);
        saveConfig();

        getLogger().info("Successfully enabled.");
    }

    private void loadSQL() {
        if(CTRSetting.DATABASE_URL.getValue().startsWith("postgresql")) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (Exception ignored) {
            }
        } else if (CTRSetting.DATABASE_URL.getValue().startsWith("mysql")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (Exception ignored) {
            }
        }
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);

        getLogger().info("Successfully disabled.");
    }
}
