package ru.ckateptb.tablecloth.gui.chest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import ru.ckateptb.tablecloth.ioc.annotation.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ChestGuiService implements Listener {
    private final Map<Inventory, ChestGui> instances = new HashMap<>();

    public void register(ChestGui chestGui) {
        instances.put(chestGui.getInventory(), chestGui);
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (instances.containsKey(inventory)) {
            instances.get(inventory).getButton(event.getSlot()).ifPresent(itemButton -> itemButton.onClick(event));
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        Optional.ofNullable(instances.remove(event.getInventory())).ifPresent(chestGui -> {
            if (!chestGui.isIgnoreCloseEvent()) chestGui.getInventoryCloseHandler().handle(event);
        });
    }

}
