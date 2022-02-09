package ru.ckateptb.tablecloth.temporary.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.ckateptb.tablecloth.event.ArmorEquipEvent;
import ru.ckateptb.tablecloth.ioc.annotation.Component;
import ru.ckateptb.tablecloth.temporary.Temporary;
import ru.ckateptb.tablecloth.temporary.TemporaryService;

@Component
public class ArmorHandler implements Listener {
    private final TemporaryService temporaryService;

    public ArmorHandler(TemporaryService temporaryService) {
        this.temporaryService = temporaryService;
    }

    @EventHandler(ignoreCancelled = true)
    public void on(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("tablecloth:armor")) {
            player.getMetadata("tablecloth:armor").forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasMetadata("tablecloth:armor")) {
            entity.getMetadata("tablecloth:armor").forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    EntityEquipment equipment = entity.getEquipment();
                    if (equipment != null) {
                        for (ItemStack armor : equipment.getArmorContents()) {
                            if (armor != null) event.getDrops().remove(armor);
                        }
                    }
                    temporaryService.revert(temporary);
                    for (ItemStack armor : entity.getEquipment().getArmorContents()) {
                        if (armor != null) event.getDrops().add(armor);
                    }
                }
            });
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player entity = event.getPlayer();
        if (entity.hasMetadata("tablecloth:armor")) {
            entity.getMetadata("tablecloth:armor").forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    temporaryService.revert(temporary);
                }
            });
        }
    }
}
