package de.neo.jagil.cmd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class GuiBuilderCmd implements CommandExecutor {

    private class BuilderGui extends GUI {

        private final Path file;

        public BuilderGui(GuiTypes.DataGui gui, Path path, OfflinePlayer p) {
            super(gui, p);
            this.file = path;
        }

        public BuilderGui(String name, int size, String file, OfflinePlayer p) {
            super(name, size, p);
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
        public void handleClose(InventoryCloseEvent e) {
            for(int i = 0; i < getInventory().getContents().length; i++) {
                ItemStack is = getInventory().getContents()[i];
                if(is != null) {
                    GuiTypes.GuiItem guiItem = new GuiTypes.GuiItem();
                    guiItem.slot = i;
                    guiItem.material = is.getType();
                    if(guiItem.material.equals(Material.PLAYER_HEAD)) {
                        SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
                        try {
                            Field profileField = skullMeta.getClass().getDeclaredField("profile");
                            profileField.setAccessible(true);
                            GameProfile gp = (GameProfile) profileField.get(skullMeta);
                            guiItem.texture = gp.getProperties().get("textures").iterator().next().getValue();
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
                        GuiTypes.GuiEnchantment guiEnchantment = new GuiTypes.GuiEnchantment();
                        guiEnchantment.enchantment = entry.getKey();
                        guiEnchantment.level = entry.getValue();
                        guiItem.enchantments.add(guiEnchantment);
                    }
                    if(is.hasItemMeta()) {
                        guiItem.name = is.getItemMeta().getDisplayName();
                        guiItem.lore = is.getItemMeta().getLore();
                    }else {
                        guiItem.lore = new ArrayList<>();
                    }
                    guiItem.amount = is.getAmount();
                    getGuiData().items.put(guiItem.slot, guiItem);
                }
            }
            try {
                writeInventoryToFile(getGuiData(), this.file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            getPlayer().sendMessage("§aGUI saved.");
        }

        @Override
        public boolean isCancelledByDefault() {
            return false;
        }
    }

    public static void writeInventoryToFile(GuiTypes.DataGui gui, Path saveFile) throws IOException {
        JsonObject jsonGui = new JsonObject();
        jsonGui.addProperty("name", gui.name);
        jsonGui.addProperty("size", gui.size);
        if(gui.animationMod != 0) jsonGui.addProperty("animationTick", gui.animationMod);

        JsonArray items = new JsonArray();

        for(GuiTypes.GuiItem guiItem : gui.items.values()) {
            JsonObject item = new JsonObject();

            if(!guiItem.id.isEmpty()) item.addProperty("id", guiItem.id);
            item.addProperty("slot", guiItem.slot);
            item.addProperty("material", guiItem.material.name());
            if(!guiItem.name.isEmpty()) item.addProperty("name", guiItem.name);
            if(guiItem.amount != 1) item.addProperty("amount", guiItem.amount);
            if(!guiItem.lore.isEmpty()) {
                JsonArray lore = new JsonArray();
                for(String line : guiItem.lore) {
                    lore.add(line);
                }
                item.add("lore", lore);
            }
            if(!guiItem.enchantments.isEmpty()) {
                JsonArray enchantments = new JsonArray();
                for(GuiTypes.GuiEnchantment enchantment : guiItem.enchantments) {
                    JsonObject ench = new JsonObject();
                    ench.addProperty("name", enchantment.enchantment.toString().toUpperCase());
                    ench.addProperty("level", enchantment.level);
                    enchantments.add(ench);
                }
                item.add("enchantments", enchantments);
            }
            if(!guiItem.texture.isEmpty()) item.addProperty("base64", guiItem.texture);
            if(guiItem.customModelData != 0) item.addProperty("modelData", guiItem.customModelData);
            if(!guiItem.animationFrames.isEmpty()) {
                JsonArray frames = new JsonArray();
                for(String frameId : guiItem.animationFrames) {
                    frames.add(frameId);
                }
                item.add("animationFrames", frames);
            }

            items.add(item);
        }

        jsonGui.add("items", items);

        try(OutputStream out = Files.newOutputStream(saveFile)) {
            out.write(jsonGui.toString().getBytes(StandardCharsets.UTF_8));
        }
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
                        Path path = Paths.get(JAGILLoader.getPlugin(JAGILLoader.class)
                                .getDataFolder().getAbsolutePath(), file);
                        GuiTypes.DataGui gui = GuiReaderManager.getInstance().readFile(path);
                        new BuilderGui(gui, path, p).show();
                    } catch (IOException e) {
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
