package ru.ckateptb.tablecloth.temporary.flight;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.stereotype.Component;
import ru.ckateptb.tablecloth.temporary.Temporary;
import ru.ckateptb.tablecloth.temporary.TemporaryService;

@Component
public class FlightHandler implements Listener {
    private final TemporaryService temporaryService;

    public FlightHandler(TemporaryService temporaryService) {
        this.temporaryService = temporaryService;
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getEntity() instanceof LivingEntity livingEntity && livingEntity.hasMetadata("tablecloth:flight")) {
                livingEntity.getMetadata("tablecloth:flight").forEach(metadataValue -> {
                    if (metadataValue.value() instanceof TemporaryFlight flight) {
                        if (flight.isPreventFallDamage()) event.setCancelled(true);
                    }
                });
            }
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player entity = event.getPlayer();
        if (entity.hasMetadata("tablecloth:flight")) {
            entity.getMetadata("tablecloth:flight").forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    temporaryService.revert(temporary);
                }
            });
        }
    }
}
