package pl.banansmp.customitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ItemGui implements InventoryHolder {

    public static final int PER_PAGE = 45;
    public static final int SLOT_PREV = 45;
    public static final int SLOT_CATEGORY = 49;
    public static final int SLOT_NEXT = 53;

    private final Inventory inventory;
    private final String category;
    private final int page;
    private final int pages;

    public ItemGui(BananCustomItems plugin, String category, int requestedPage) {
        this.category = category;
        List<ItemRegistry.Entry> list = plugin.registry().entries(category);
        this.pages = Math.max(1, (int) Math.ceil(list.size() / (double) PER_PAGE));
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));

        this.inventory = Bukkit.createInventory(this, 54,
                Component.text("Customowe itemy (" + category + ") " + (page + 1) + "/" + pages));

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
            ItemStack display = list.get(start + i).stack().clone();
            ItemMeta meta = display.getItemMeta();
            List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(Component.empty());
            lore.add(Component.text("ID: ", NamedTextColor.GRAY)
                    .append(Component.text(list.get(start + i).id(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("LPM", NamedTextColor.YELLOW)
                    .append(Component.text(", aby wziąć 1 sztukę.", NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift + LPM", NamedTextColor.YELLOW)
                    .append(Component.text(", aby wziąć stack.", NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            display.setItemMeta(meta);
            inventory.setItem(i, display);
        }

        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
        for (int slot = PER_PAGE; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }
        if (this.page > 0) {
            inventory.setItem(SLOT_PREV, named(Material.ARROW,
                    Component.text("Poprzednia strona", NamedTextColor.YELLOW), null));
        }
        if (this.page < pages - 1) {
            inventory.setItem(SLOT_NEXT, named(Material.ARROW,
                    Component.text("Nastepna strona", NamedTextColor.YELLOW), null));
        }
        inventory.setItem(SLOT_CATEGORY, named(Material.CLOCK,
                Component.text("Kategoria: " + category, NamedTextColor.GOLD),
                Component.text("LPM / PPM - zmien kategorie", NamedTextColor.GRAY)));
    }

    private static ItemStack named(Material material, Component name, Component lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            meta.lore(List.of(lore.decoration(TextDecoration.ITALIC, false)));
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public String category() {
        return category;
    }

    public int page() {
        return page;
    }

    public int pages() {
        return pages;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
