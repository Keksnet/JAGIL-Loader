package de.neo.jagil.cmd;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GuiExportCmd implements CommandExecutor {

    /*
    public static GuiTypes.DataGui convert(Inventory inv) {
        GuiTypes.DataGui xmlGui = new GuiTypes.DataGui();

        IntStream.range(0, inv.getContents().length)
                .mapToObj(x -> new Pair<>(x, inv.getItem(x)))
                .filter(x -> x.getValue() != null)
                .forEach(x -> {
                    ItemStack stack = x.getValue();
                    GuiTypes.GuiItem guiItem = new GuiTypes.GuiItem();
                    guiItem.slot = x.getKey();
                    if (stack.getType().equals(Material.PLAYER_HEAD)) {
                        SkullMeta skullMeta = (SkullMeta) stack.getItemMeta();
                        guiItem.texture = skullMeta.getPlayerProfile().getTextures().
                    }
                });

        for(int i = 0; i < inv.getContents().length; i++) {
            ItemStack is = inv.getContents()[i];
            if(is != null) {
                ItemBuilder itemBuilder = new ItemBuilder(is.getType());
                if (is.getType().equals(Material.PLAYER_HEAD)) {
                    SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
                    itemBuilder.setSkullProfile(skullMeta.getPlayerProfile());
                }
                if(!is.getEnchantments().isEmpty()) {
                    for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()) {
                        itemBuilder.addEnchantment(entry.getKey(), entry.getValue());
                    }
                }
                if(is.hasItemMeta()) {
                    ItemMeta meta = is.getItemMeta();
                    itemBuilder.setName(meta.displayName());

                    List<Component> itemLore = meta.lore();
                    if (itemLore != null) {
                        itemLore.forEach(itemBuilder::addLore);
                    }
                }

                itemBuilder.setAmount(is.getAmount());
                xmlGui.items.put(i, itemBuilder.);
            }
        }
        return xmlGui;
    }
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(Component.text("Not yet supported by JAGIL v4"));
        return true;

        /*
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
                        GuiTypes.DataGui gui = convert(container.getInventory());
                        gui.size = size;
                        gui.name = name;
                        try {
                            GuiBuilderCmd.writeInventoryToFile(gui, file);
                        } catch (IOException e) {
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
         */
    }
}
