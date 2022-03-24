package de.neo.jagil.cmd;

import com.mojang.authlib.GameProfile;
import de.neo.jagil.JAGILLoader;
import de.neo.jagil.gui.GUI;
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

        private final GUI.DataGui xmlGui;
        private final Path file;

        public OpenedGUI(String file, OfflinePlayer p) throws XMLStreamException, IOException {
            super(Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file), p);
            this.file = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file);
            this.xmlGui = new GUI.DataGui();
        }

        public OpenedGUI(String name, int size, String file, OfflinePlayer p) {
            super(name, size, p);
            this.xmlGui = new GUI.DataGui();
            this.xmlGui.size = size;
            this.file = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file);
        }

        @Override
        public boolean handle(InventoryClickEvent e) {
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
                        new OpenedGUI(file, p).show();
                    } catch (XMLStreamException | IOException e) {
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
