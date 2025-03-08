package de.neo.jagil;

import de.neo.jagil.cmd.GuiBuilderCmd;
import de.neo.jagil.cmd.GuiExportCmd;
import de.neo.jagil.cmd.GuiOpenerCMD;
import de.neo8.jagil.JAGIL;
import de.neo8.jagil.config.CachingConfig;
import de.neo8.jagil.config.GlobalJAGILConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JAGILLoader extends JavaPlugin {

    public void onEnable() {
        this.loadConfig();

        JAGIL.setGlobalJAGILConfig(this.buildGlobalConfig());

        getCommand("guibuilder").setExecutor(new GuiBuilderCmd());
        getCommand("guiexport").setExecutor(new GuiExportCmd());
        getCommand("guiopener").setExecutor(new GuiOpenerCMD());

        this.getLogger().info("JAGIL loaded!");
        JAGIL.setLoaderPlugin(this);
        JAGIL.init(this);
    }

    private void loadConfig() {
        Path dataDir = getDataPath();
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path configPath = dataDir.resolve("config.yml");
        if (!Files.exists(configPath)) {
            try {
                saveDefaultConfig();
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean supportsHeadDatabase() {
        return Bukkit.getPluginManager().isPluginEnabled("HeadDatabase");
    }

    private GlobalJAGILConfig buildGlobalConfig() {
        ConfigurationSection config = this.getConfig();
        List<String> features = new ArrayList<>();

        if (this.supportsHeadDatabase()) {
            features.add("head-database-api");
        }

        Map<String, String> providerOptions = new HashMap<>();
        ConfigurationSection cachingConfig = config.getConfigurationSection("caching.providerOptions");
        if (cachingConfig != null) {
            cachingConfig.getKeys(false).forEach(x -> {
                providerOptions.put(x, config.getString(x));
            });
        }

        return GlobalJAGILConfig.builder()
                                .loaderName(config.getString("loaderName", "JAGIL"))
                                .debugMode(config.getBoolean("debugMode", false))
                                .cachingConfig(CachingConfig.builder()
                                                            .enabled(config.getBoolean("caching.enabled", false))
                                                            .providerConfig(providerOptions)
                                                            .build())
                                .supportedFeatures(features)
                                .build();
    }
}
