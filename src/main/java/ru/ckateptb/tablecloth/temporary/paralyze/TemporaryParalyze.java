package ru.ckateptb.tablecloth.temporary.paralyze;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.config.TableclothConfig;
import ru.ckateptb.tablecloth.spring.SpringContext;
import ru.ckateptb.tablecloth.temporary.*;

import java.util.List;

public class TemporaryParalyze extends AbstractTemporary {
    public static final ProtocolManager protocolManager;
    static {
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(Tablecloth.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                    public void onPacketReceiving(PacketEvent e) {
                        if (e.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                            if (e.getPlayer().getEntityId() == e.getPacket().getIntegers().read(0))
                                e.setCancelled(true);
                        }
                    }
                }
        );
    }

    private final AnnotationConfigApplicationContext context;
    private final TemporaryService temporaryService;
    private final Tablecloth plugin;
    @Getter
    private final LivingEntity livingEntity;
    private TemporaryBossBar temporaryBossBar;
    private final long duration;
    private boolean hasAI;
    private AnvilGUI anvilGUI;

    public ArmorStand armorStand;
    private GameMode originalGameMode;

    public TemporaryParalyze(LivingEntity livingEntity, long duration) {
        this.livingEntity = livingEntity;
        this.duration = duration;
        this.context = SpringContext.getInstance();
        this.temporaryService = context.getBean(TemporaryService.class);
        this.plugin = Tablecloth.getInstance();
        if (this.livingEntity.hasMetadata("tablecloth:paralyze")) {
            List.copyOf(this.livingEntity.getMetadata("tablecloth:paralyze")).forEach(metadataValue -> {
                if (metadataValue.value() instanceof Temporary temporary) {
                    temporaryService.revert(temporary);
                }
            });
        }
        this.setRevertTime(duration + System.currentTimeMillis());
        this.register();
    }

    @Override
    @SneakyThrows
    public void init() {
        if (livingEntity instanceof Player player) {
            TableclothConfig config = context.getBean(TableclothConfig.class);
            String paralyzeName = config.getParalyzeName();
            this.livingEntity.setMetadata("tablecloth:paralyze", new FixedMetadataValue(this.plugin, this));
            ParalyzeType paralyzeType = config.getParalyzeType();
            if (paralyzeType == ParalyzeType.INVENTORY) {
                anvilGUI = new AnvilGUI.Builder()
                        .preventClose()
                        .title(paralyzeName)
                        .plugin(Tablecloth.getInstance())
                        .text(paralyzeName)
                        .onComplete((player1, text) -> AnvilGUI.Response.text(paralyzeName))
                        .itemLeft(new ItemStack(Material.BARRIER))
                        .itemRight(new ItemStack(Material.BARRIER))
                        .open(player);
            } else if (paralyzeType == ParalyzeType.ARMORSTAND) {
                this.originalGameMode = player.getGameMode();
                this.armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                this.armorStand.setGravity(true);
                this.armorStand.setCanPickupItems(false);
                this.armorStand.setMarker(false);
                this.armorStand.setVisible(false);
                player.setSneaking(false);
                changeGameModePacket(player, 3);
                setSpectatorEntity(player, armorStand);
            }
            temporaryBossBar = new TemporaryBossBar(paralyzeName, duration, player);
        } else {
            hasAI = livingEntity.hasAI();
            livingEntity.setAI(false);
        }
    }

    @Override
    public TemporaryUpdateState update() {
        return TemporaryUpdateState.CONTINUE;
    }

    private void changeGameModePacket(Player player, float value) {
        try {
            PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);
            packetContainer.getGameStateIDs().write(0, 3);
            packetContainer.getFloat().write(0, value);
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSpectatorEntity(Player player, Entity entity) {
        try {
            PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.CAMERA);
            packetContainer.getIntegers().write(0, entity.getEntityId());
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revert() {
        if (livingEntity.hasMetadata("tablecloth:paralyze")) livingEntity.removeMetadata("tablecloth:paralyze", plugin);
        if (anvilGUI != null) anvilGUI.closeInventory();
        if (armorStand != null) {
            this.armorStand.remove();
            if (livingEntity instanceof Player player) {
                changeGameModePacket(player, (byte) originalGameMode.getValue());
                setSpectatorEntity(player, player);
                player.setFlying(false);
                player.setSneaking(true);
                player.setSneaking(false);
            }
        }
        if (temporaryBossBar != null) temporaryService.revert(temporaryBossBar);
        else livingEntity.setAI(hasAI);
    }
}
