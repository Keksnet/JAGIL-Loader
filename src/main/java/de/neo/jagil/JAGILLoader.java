package de.neo.jagil;

import de.neo.jagil.cmd.GuiBuilderCmd;
import de.neo.jagil.cmd.GuiExportCmd;
import de.neo.jagil.cmd.GuiOpenerCMD;
import de.neo8.jagil.JAGIL;
import de.neo8.jagil.util.GlobalJAGILConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JAGILLoader extends JavaPlugin {
	
	public void onEnable() {
		this.loadConfig();

		JAGIL.setGlobalJAGILConfig(buildGlobalConfig());

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
		List<String> features = new ArrayList<>();

		if (supportsHeadDatabase()) {
			features.add("head-database-api");
		}

		return GlobalJAGILConfig.builder()
				.loaderName(this.getConfig().getString("loaderName", "JAGIL"))
				.debugMode(this.getConfig().getBoolean("debugMode", false))
				.supportedFeatures(features)
				.build();
	}
}
