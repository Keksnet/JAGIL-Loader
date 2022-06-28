package de.neo.jagil;

import de.neo.jagil.cmd.GuiBuilderCmd;
import de.neo.jagil.cmd.GuiExportCmd;
import de.neo.jagil.cmd.GuiOpenerCMD;
import org.bukkit.plugin.java.JavaPlugin;

public class JAGILLoader extends JavaPlugin {
	
	public void onEnable() {
		if(!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		getCommand("guibuilder").setExecutor(new GuiBuilderCmd());
		getCommand("guiexport").setExecutor(new GuiExportCmd());
		getCommand("guiopener").setExecutor(new GuiOpenerCMD());

		this.getLogger().info("JAGIL loaded!");
		JAGIL.init(this);
		JAGIL.loaderPlugin = this;
	}
}
