package xyz.voroniyx.deathtotem.paper;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.voroniyx.deathtotem.config.JsonConfigManager;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.io.IOException;

public class DeathTotemPlugin extends JavaPlugin {

    public static final String COMMAND_PERMISSION = "deathtotemmod.command";

    private JsonConfigManager<ModConfig> config;

    @Override
    public void onEnable() {
        getLogger().info("DeathTotemMod (Paper) initializing");

        loadConfig();
        registerCommands();

        getServer().getPluginManager().registerEvents(new TotemResurrectListener(this), this);
    }

    private void loadConfig() {
        config = new JsonConfigManager<>(ModConfig.class, ModConfig.GetConfigPath(getDataFolder().toPath()));
        try {
            config.load();
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(TiabCommand.build(this), "Totem in a Barrel configuration")
        );
    }

    public JsonConfigManager<ModConfig> getConfigManager() {
        return config;
    }
}
