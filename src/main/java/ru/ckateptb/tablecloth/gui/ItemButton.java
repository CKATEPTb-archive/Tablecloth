package ru.ckateptb.tablecloth.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemButton {
    private final ItemStack stack;
    public ButtonClickHandler handle = event -> event.setCancelled(true);

    public ItemButton(ItemStack stack) {
        this.stack = stack;
        setHandle(event -> event.setCancelled(true));
    }

    public ItemStack get() {
        return stack;
    }

    public ItemButton setHandle(final ButtonClickHandler handle) {
        this.handle = handle;
        return this;
    }

    public void onClick(InventoryClickEvent event) {
        this.handle.onClick(event);
    }

    public interface ButtonClickHandler {
        void onClick(InventoryClickEvent event);
    }

}
