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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class GuiBuilderCmd implements CommandExecutor {

    private class BuilderGui extends GUI {

        private final GUI.DataGui xmlGui;
        private final Path file;

        public BuilderGui(String file, OfflinePlayer p) throws XMLStreamException, IOException {
            super(Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file), p);
            this.file = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file);
            this.xmlGui = new GUI.DataGui();
        }

        public BuilderGui(String name, int size, String file, OfflinePlayer p) {
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

        @Override
        public GUI handleClose(InventoryCloseEvent e) {
            for(int i = 0; i < getInventory().getContents().length; i++) {
                ItemStack is = getInventory().getContents()[i];
                if(is != null) {
                    XmlHead xmlItem = new XmlHead();
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
                                } catch (IllegalAccessException ex1) {
                                    dump.append("Error getting value: ").append(ex1.getMessage()).append("\n");
                                }
                            }
                            Bukkit.broadcast(dump.toString(), "jagil.debug");
                            JAGILLoader.getPlugin(JAGILLoader.class).getLogger().warning(dump.toString().trim());
                        }
                    }
                    for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()) {
                        GuiEnchantment xmlEnchantment = new GuiEnchantment();
                        xmlEnchantment.enchantment = entry.getKey();
                        xmlEnchantment.level = entry.getValue();
                        xmlItem.enchantments.add(xmlEnchantment);
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
            try {
                writeInventoryToFile(this.xmlGui, this.file);
            } catch (IOException | XMLStreamException ex) {
                ex.printStackTrace();
            }
            getPlayer().sendMessage("§aGUI saved.");
            return this;
        }

        @Override
        public boolean isCancelledByDefault() {
            return false;
        }
    }

    public static void writeInventoryToFile(GUI.DataGui json, Path saveFile) throws IOException, XMLStreamException {
        BufferedWriter osw = Files.newBufferedWriter(saveFile);
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(osw);
        writer.writeStartDocument();
        writer.writeStartElement("gui");
        writer.writeStartElement("name");
        writer.writeCharacters(json.name);
        writer.writeEndElement();
        writer.writeStartElement("size");
        writer.writeCharacters(String.valueOf(json.size));
        writer.writeEndElement();
        writer.writeStartElement("items");
        for(GUI.GuiItem item : json.items.values()) {
            writer.writeStartElement("item");

            writer.writeStartElement("id");
            writer.writeCharacters(item.id);
            writer.writeEndElement();

            writer.writeStartElement("slot");
            writer.writeCharacters(String.valueOf(item.slot));
            writer.writeEndElement();

            writer.writeStartElement("material");
            writer.writeCharacters(item.material.toString());
            writer.writeEndElement();

            writer.writeStartElement("name");
            writer.writeCharacters(item.name);
            writer.writeEndElement();

            writer.writeStartElement("amount");
            writer.writeCharacters(String.valueOf(item.amount));
            writer.writeEndElement();

            writer.writeStartElement("lore");

            if(item.lore != null) {
                for(String line : item.lore) {
                    writer.writeStartElement("line");
                    writer.writeCharacters(line);
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();

            if(item.material.equals(Material.PLAYER_HEAD)) {
                writer.writeStartElement("base64");
                writer.writeCharacters(((GUI.XmlHead)item).texture);
                writer.writeEndElement();
            }

            writer.writeStartElement("enchantments");

            if(item.enchantments != null) {
                for(GUI.GuiEnchantment enchantment : item.enchantments) {
                    writer.writeStartElement("enchantment");

                    writer.writeStartElement("enchantmentName");
                    writer.writeCharacters(enchantment.enchantment.toString().toUpperCase());
                    writer.writeEndElement();

                    writer.writeStartElement("enchantmentLevel");
                    writer.writeCharacters(String.valueOf(enchantment.level));
                    writer.writeEndElement();

                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();

            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p) {
            if(p.hasPermission("jagil.builder")) {
                if(args.length >= 3) {
                    try {
                        int size = Integer.parseInt(args[0]);
                        String file = args[1];
                        StringBuilder name = new StringBuilder();
                        for(int i = 2; i < args.length; i++) {
                            name.append(ChatColor.translateAlternateColorCodes('&', args[i])).append(" ");
                        }
                        new BuilderGui(name.toString(), size, file, p).show();
                    }catch(NumberFormatException e) {
                        e.printStackTrace();
                        p.sendMessage("§cPlease enter a valid number! (" + args[0] + " is invalid!)");
                    }
                }else if(args.length == 1) {
                    String file = args[0];
                    try {
                        new BuilderGui(file, p).show();
                    } catch (XMLStreamException | IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    p.sendMessage("§cUsage: /guibuilder <size> <file> <name>");
                }
            }else {
                p.sendMessage("§cYou do not have the permission jagil.builder");
            }
        }else {
            sender.sendMessage("Only a player can use this command!");
        }
        return false;
    }

}
