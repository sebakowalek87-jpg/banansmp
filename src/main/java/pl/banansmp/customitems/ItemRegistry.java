package pl.banansmp.customitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ItemRegistry {

    public static final String ALL = "Wszystkie";

    public record Entry(String id, String category, ItemStack stack) {
    }

    private final BananCustomItems plugin;
    private final NamespacedKey idKey;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final List<String> categories = new ArrayList<>(List.of(ALL));
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ItemRegistry(BananCustomItems plugin, NamespacedKey idKey) {
        this.plugin = plugin;
        this.idKey = idKey;
    }

    public void load(YamlConfiguration cfg) {
        ConfigurationSection root = cfg.getConfigurationSection("items");
        if (root == null) {
            plugin.getLogger().warning("items.yml nie ma sekcji 'items'.");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            Material material = Material.matchMaterial(s.getString("material", "STONE"));
            if (material == null || !material.isItem()) {
                plugin.getLogger().warning("Item '" + id + "': zly material, pomijam.");
                continue;
            }
            String category = s.getString("category", "Inne");
            ItemStack stack = build(id, material, s);
            entries.put(id.toLowerCase(Locale.ROOT), new Entry(id.toLowerCase(Locale.ROOT), category, stack));
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
    }

    private Component plain(String miniMessage) {
        return mm.deserialize(miniMessage).decoration(TextDecoration.ITALIC, false);
    }

    private ItemStack build(String id, Material material, ConfigurationSection s) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(plain(s.getString("name", id)));

        List<Component> lore = new ArrayList<>();
        for (String line : s.getStringList("lore")) {
            lore.add(plain(line));
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }

        ConfigurationSection ench = s.getConfigurationSection("enchants");
        if (ench != null) {
            for (String key : ench.getKeys(false)) {
                Enchantment e = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
                if (e != null) {
                    meta.addEnchant(e, ench.getInt(key), true);
                } else {
                    plugin.getLogger().warning("Item '" + id + "': nieznane zaklecie " + key);
                }
            }
        }

        if (s.getBoolean("glow", false)) {
            meta.setEnchantmentGlintOverride(true);
        }
        if (s.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }

        String color = s.getString("color");
        if (color != null && meta instanceof LeatherArmorMeta leather) {
            try {
                leather.setColor(Color.fromRGB(Integer.parseInt(color.replace("#", ""), 16)));
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("Item '" + id + "': zly kolor " + color);
            }
        }

        String model = s.getString("item-model");
        if (model != null) {
            NamespacedKey key = NamespacedKey.fromString(model);
            if (key != null) {
                meta.setItemModel(key);
            }
        }

        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id.toLowerCase(Locale.ROOT));
        stack.setItemMeta(meta);
        return stack;
    }

    public int size() {
        return entries.size();
    }

    public List<String> ids() {
        return new ArrayList<>(entries.keySet());
    }

    public List<String> categories() {
        return categories;
    }

    public List<Entry> entries(String category) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : entries.values()) {
            if (ALL.equals(category) || e.category().equals(category)) {
                out.add(e);
            }
        }
        return out;
    }

    public ItemStack copy(String id) {
        Entry e = entries.get(id);
        return e == null ? null : e.stack().clone();
    }
}
