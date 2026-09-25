package xyz.voroniyx.deathtotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.voroniyx.deathtotem.commands.ConfigCommand;
import xyz.voroniyx.deathtotem.config.JsonConfigManager;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.io.IOException;

public class DeathTotemMod implements ModInitializer {

    public static final String MOD_ID = "deathtotemmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static JsonConfigManager<ModConfig> CONFIG;


    @Override
    public void onInitialize() {
        LOGGER.info("DeathTotemMod Initializing");

        loadConfig();

        registerCommands();
    }

    public void loadConfig() {
        CONFIG = new JsonConfigManager<>(ModConfig.class, ModConfig.GetConfigPath(FabricLoader.getInstance().getConfigDir()));
        try {
            CONFIG.load();
            CONFIG.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (
                        commandDispatcher,
                        _,
                        _
                ) -> {
                    new ConfigCommand().register(commandDispatcher);
                }
        );
    }
}
