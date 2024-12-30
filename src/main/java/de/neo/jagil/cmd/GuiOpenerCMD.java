package de.neo.jagil.cmd;

import de.neo.jagil.JAGILLoader;
import de.neo8.jagil.gui.inventory.InventoryGui;
import de.neo8.jagil.gui.inventory.InventoryGuiTypes;
import de.neo8.jagil.reader.GuiReaderManager;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiOpenerCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only a player can use this command!");
            return false;
        }

        if (!player.hasPermission("jagil.gui.open")) {
            player.sendMessage("§cYou do not have the permission jagil.gui.open");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /guiopener <file>");
            return false;
        }

        String file = args[0];
        try {
            Path path = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class)
                    .getDataFolder().getAbsolutePath(), file);
            InventoryGuiTypes.DataGui gui = GuiReaderManager.getInstance().readFile(path, null);
            new OpenedGUI(gui, path, player).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static class OpenedGUI extends InventoryGui {

        private final Path file;

        public OpenedGUI(InventoryGuiTypes.DataGui gui, Path path, Player player) {
            super(gui, player);
            this.file = path;
        }

        public OpenedGUI(Component name, int size, Path path, Player player) {
            super(name, size, player);
            this.file = path;
        }

        @Override
        public boolean handle(InventoryClickEvent e) {
            super.handle(e);
            return false;
        }

        @Override
        public boolean handleDrag(InventoryDragEvent e) {
            return false;
        }
    }

}
