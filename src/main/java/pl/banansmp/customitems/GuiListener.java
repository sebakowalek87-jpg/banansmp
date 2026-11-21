package pl.banansmp.customitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class GuiListener implements Listener {

    private final BananCustomItems plugin;

    public GuiListener(BananCustomItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof ItemGui) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof ItemGui gui)) {
            return;
        }
        e.setCancelled(true);

        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = e.getSlot();

        if (slot == ItemGui.SLOT_PREV && gui.page() > 0) {
            player.openInventory(new ItemGui(plugin, gui.category(), gui.page() - 1).getInventory());
            return;
        }
        if (slot == ItemGui.SLOT_NEXT && gui.page() < gui.pages() - 1) {
            player.openInventory(new ItemGui(plugin, gui.category(), gui.page() + 1).getInventory());
            return;
        }
        if (slot == ItemGui.SLOT_CATEGORY) {
            List<String> cats = plugin.registry().categories();
            int idx = cats.indexOf(gui.category());
            int step = e.getClick() == ClickType.RIGHT ? -1 : 1;
            String next = cats.get(Math.floorMod(idx + step, cats.size()));
            player.openInventory(new ItemGui(plugin, next, 0).getInventory());
            return;
        }
        if (slot >= ItemGui.PER_PAGE) {
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        String id = clicked.getItemMeta().getPersistentDataContainer()
                .get(plugin.idKey(), PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        if (!player.hasPermission("banansmp.customitems.take")) {
            player.sendMessage(Component.text("Nie masz uprawnien do odbierania itemow.", NamedTextColor.RED));
            return;
        }
        ItemStack give = plugin.registry().copy(id);
        if (give == null) {
            return;
        }
        if (e.isShiftClick()) {
            give.setAmount(give.getMaxStackSize());
        }
        player.getInventory().addItem(give).values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }
}
