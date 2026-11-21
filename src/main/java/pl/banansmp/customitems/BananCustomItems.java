package pl.banansmp.customitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BananCustomItems extends JavaPlugin implements TabExecutor {

    private ItemRegistry registry;
    private NamespacedKey idKey;

    @Override
    public void onEnable() {
        idKey = new NamespacedKey(this, "item_id");
        File file = new File(getDataFolder(), "items.yml");
        if (!file.exists()) {
            saveResource("items.yml", false);
        }
        reloadItems();
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getCommand("citems").setExecutor(this);
        getCommand("citems").setTabCompleter(this);
        getLogger().info("Zaladowano " + registry.size() + " customowych itemow.");
    }

    public void reloadItems() {
        File file = new File(getDataFolder(), "items.yml");
        registry = new ItemRegistry(this, idKey);
        registry.load(YamlConfiguration.loadConfiguration(file));
    }

    public ItemRegistry registry() {
        return registry;
    }

    public NamespacedKey idKey() {
        return idKey;
    }

    private static Component msg(String text, NamedTextColor color) {
        return Component.text(text, color);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(msg("Tylko dla graczy.", NamedTextColor.RED));
                return true;
            }
            if (!player.hasPermission("banansmp.customitems.use")) {
                player.sendMessage(msg("Brak uprawnien.", NamedTextColor.RED));
                return true;
            }
            player.openInventory(new ItemGui(this, ItemRegistry.ALL, 0).getInventory());
            return true;
        }

        if (!sender.hasPermission("banansmp.customitems.admin")) {
            sender.sendMessage(msg("Brak uprawnien.", NamedTextColor.RED));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadItems();
            sender.sendMessage(msg("Przeladowano " + registry.size() + " itemow.", NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length >= 3) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(msg("Nie ma takiego gracza online.", NamedTextColor.RED));
                return true;
            }
            ItemStack stack = registry.copy(args[2].toLowerCase(Locale.ROOT));
            if (stack == null) {
                sender.sendMessage(msg("Nieznany item: " + args[2], NamedTextColor.RED));
                return true;
            }
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
                } catch (NumberFormatException ignored) {
                }
            }
            stack.setAmount(amount);
            target.getInventory().addItem(stack).values()
                    .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
            sender.sendMessage(msg("Dano " + amount + "x " + args[2] + " graczowi " + target.getName() + ".", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(msg("Uzycie: /citems [reload|give <gracz> <id> [ilosc]]", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("banansmp.customitems.admin")) {
            return out;
        }
        if (args.length == 1) {
            out.addAll(List.of("open", "reload", "give"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            out.addAll(registry.ids());
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(prefix));
        return out;
    }
}
