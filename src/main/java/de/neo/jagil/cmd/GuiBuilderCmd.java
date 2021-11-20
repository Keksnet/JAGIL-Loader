package de.neo.jagil.cmd;

import com.mojang.authlib.GameProfile;
import de.neo.jagil.JAGILLoader;
import de.neo.jagil.gui.GUI;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class GuiBuilderCmd implements CommandExecutor {

    private class BuilderGui extends GUI {

        private final GUI.XmlGui xmlGui;
        private final Path file;

        public BuilderGui(String file, OfflinePlayer p) throws XMLStreamException, IOException {
            super(Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file), p);
            this.file = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class).getDataFolder().getAbsolutePath(), file);
            this.xmlGui = new GUI.XmlGui();
        }

        public BuilderGui(String name, int size, String file, OfflinePlayer p) {
            super(name, size, p);
            this.xmlGui = new GUI.XmlGui();
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
                    XmlItem xmlItem = new XmlItem();
                    xmlItem.slot = i;
                    xmlItem.material = is.getType();
                    if(xmlItem.material.equals(Material.PLAYER_HEAD)) {
                        try {
                            ((XmlHead)xmlItem).texture = ((GameProfile)((SkullMeta)is.getItemMeta()).getClass().getDeclaredField("profile")
                                    .get(is.getItemMeta())).getProperties().get("texture").iterator().next().getValue();
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        } catch (NoSuchFieldException ex) {
                            ex.printStackTrace();
                        }
                    }
                    for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()) {
                        XmlEnchantment xmlEnchantment = new XmlEnchantment();
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

    private void writeInventoryToFile(GUI.XmlGui json, Path saveFile) throws IOException, XMLStreamException {
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
        for(GUI.XmlItem item : json.items.values()) {
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

            for(String line : item.lore) {
                writer.writeStartElement("line");
                writer.writeCharacters(line);
                writer.writeEndElement();
            }

            writer.writeEndElement();

            if(item.material.equals(Material.PLAYER_HEAD)) {
                writer.writeStartElement("base64");
                writer.writeCharacters(((GUI.XmlHead)item).texture);
                writer.writeEndElement();
            }

            writer.writeStartElement("enchantments");

            for(GUI.XmlEnchantment enchantment : item.enchantments) {
                writer.writeStartElement("enchantment");

                writer.writeStartElement("enchantmentName");
                writer.writeCharacters(enchantment.enchantment.toString().toUpperCase());
                writer.writeEndElement();

                writer.writeStartElement("enchantmentLevel");
                writer.writeCharacters(String.valueOf(enchantment.level));
                writer.writeEndElement();

                writer.writeEndElement();
            }

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
