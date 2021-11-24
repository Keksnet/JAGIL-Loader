package de.neo.jagil.cmd;

import com.mojang.authlib.GameProfile;
import de.neo.jagil.JAGILLoader;
import de.neo.jagil.gui.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class GuiExportCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(p.hasPermission("jagil.gui.export")) {
                int size = 0;
                Path file = null;
                String name = "";
                if(args.length < 3) {
                    p.sendMessage("§cUsage: /guiexport <size> <file> <name>");
                    return false;
                }else {
                    size = Integer.parseInt(args[0]);
                    file = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), args[1] + ".xml");
                    if(!file.toFile().exists()) {
                        try {
                            Files.createFile(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    StringBuilder name_b = new StringBuilder();
                    for(int i = 2; i < args.length; i++) {
                        name_b.append(ChatColor.translateAlternateColorCodes('&', args[i])).append(" ");
                    }
                    name = name_b.toString().trim();
                }
                if(p.getTargetBlock(null, 5).getType() != Material.AIR) {
                    Block target = p.getTargetBlock(null, 5);
                    if(target.getType().equals(Material.CHEST) || target.getType().equals(Material.BARREL)) {
                        Container container = (Container) target.getState();
                        p.sendMessage("§aExporting GUI...");
                        p.sendMessage("§aSize: " + size);
                        p.sendMessage("§aFile: " + file);
                        p.sendMessage("§aName: " + name);
                        GUI.XmlGui gui = convert(container.getInventory());
                        gui.size = size;
                        gui.name = name;
                        try {
                            GuiBuilderCmd.writeInventoryToFile(gui, file);
                        } catch (IOException | XMLStreamException e) {
                            e.printStackTrace();
                        }
                        p.sendMessage("§aExported GUI!");

                    }
                }else {
                    p.sendMessage("§cYou need to look at a chest");
                }
            }
        }
        return false;
    }

    public static GUI.XmlGui convert(Inventory inv) {
        GUI.XmlGui xmlGui = new GUI.XmlGui();
        for(int i = 0; i < inv.getContents().length; i++) {
            ItemStack is = inv.getContents()[i];
            if(is != null) {
                GUI.XmlHead xmlItem = new GUI.XmlHead();
                xmlItem.slot = i;
                xmlItem.material = is.getType();
                if(xmlItem.material.equals(Material.PLAYER_HEAD)) {
                    SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
                    try {
                        Field profileField = skullMeta.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        GameProfile gp = (GameProfile) profileField.get(skullMeta);
                        xmlItem.texture = gp.getProperties().get("textures").iterator().next().getValue();
                    } catch (IllegalAccessException | NoSuchFieldException ex) {
                        ex.printStackTrace();
                        StringBuilder dump = new StringBuilder();
                        dump.append("Dumping reflection data:\n");
                        dump.append("Skull Class: ").append(skullMeta.getClass().getName()).append("\n");
                        dump.append("Available Fields:\n");
                        for(Field f : skullMeta.getClass().getDeclaredFields()) {
                            dump.append("Field: ").append(f.getName()).append("\n");
                            dump.append("Signature: ").append(f).append("\n");
                            if(Modifier.isStatic(f.getModifiers())) {
                                dump.append("Can access: ").append(f.canAccess(null)).append("\n");
                            }else {
                                dump.append("Can access: ").append(f.canAccess(skullMeta)).append("\n");
                            }
                            f.setAccessible(true);
                            try {
                                if(Modifier.isStatic(f.getModifiers())) {
                                    dump.append("Value: ").append(f.get(null)).append("\n");
                                }else {
                                    dump.append("Value: ").append(f.get(skullMeta)).append("\n");
                                }
                            } catch (IllegalAccessException e) {
                                dump.append("Error getting value: ").append(e.getMessage()).append("\n");
                            }
                        }
                        Bukkit.broadcast(dump.toString(), "jagil.debug");
                        JAGILLoader.getPlugin(JAGILLoader.class).getLogger().warning(dump.toString().trim());
                    }
                }
                if(!is.getEnchantments().isEmpty()) {
                    for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()) {
                        GUI.XmlEnchantment xmlEnchantment = new GUI.XmlEnchantment();
                        xmlEnchantment.enchantment = entry.getKey();
                        xmlEnchantment.level = entry.getValue();
                        xmlItem.enchantments.add(xmlEnchantment);
                    }
                }
                if(is.hasItemMeta()) {
                    xmlItem.name = is.getItemMeta().getDisplayName();
                    xmlItem.lore = is.getItemMeta().getLore();
                }else {
                    xmlItem.lore = new ArrayList<>();
                }
                xmlItem.amount = is.getAmount();
                xmlGui.items.put(xmlItem.slot, xmlItem);
            }
        }
        return xmlGui;
    }
}
