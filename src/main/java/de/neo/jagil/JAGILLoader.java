package de.neo.jagil;

import de.neo.jagil.cmd.GuiBuilderCmd;
import de.neo.jagil.cmd.GuiExportCmd;
import org.bukkit.plugin.java.JavaPlugin;

public class JAGILLoader extends JavaPlugin {
	
	public void onEnable() {
		if(!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		getCommand("guibuilder").setExecutor(new GuiBuilderCmd());
		getCommand("guiexport").setExecutor(new GuiExportCmd());

		this.getLogger().info("JAGIL loaded!");
		JAGIL.init(this, false);
	}
}
