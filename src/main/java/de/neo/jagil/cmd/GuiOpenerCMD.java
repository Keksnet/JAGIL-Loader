package de.neo.jagil.cmd;

import com.mojang.authlib.GameProfile;
import de.neo.jagil.JAGILLoader;
import de.neo.jagil.gui.GUI;
import de.neo.jagil.gui.GuiTypes;
import de.neo.jagil.manager.GuiReaderManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class GuiOpenerCMD implements CommandExecutor {

    private class OpenedGUI extends GUI {

        private final Path file;

        public OpenedGUI(GuiTypes.DataGui gui, Path path, OfflinePlayer p) {
            super(gui, p);
            this.file = path;
        }

        public OpenedGUI(String name, int size, Path path, OfflinePlayer p) {
            super(name, size, p);
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p) {
            if(p.hasPermission("jagil.gui.open")) {
                if(args.length == 1) {
                    String file = args[0];
                    try {
                        Path path = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class)
                                .getDataFolder().getAbsolutePath(), file);
                        GuiTypes.DataGui gui = GuiReaderManager.getInstance().readFile(path);
                        new OpenedGUI(gui, path, p).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    p.sendMessage("§cUsage: /guiopener <file>");
                }
            }else {
                p.sendMessage("§cYou do not have the permission jagil.gui.open");
            }
        }else {
            sender.sendMessage("Only a player can use this command!");
        }
        return false;
    }

}
