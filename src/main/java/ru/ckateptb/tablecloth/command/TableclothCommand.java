package ru.ckateptb.tablecloth.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;
import ru.ckateptb.tablecloth.collision.collider.AxisAlignedBoundingBoxCollider;
import ru.ckateptb.tablecloth.collision.collider.DiskCollider;
import ru.ckateptb.tablecloth.collision.collider.OrientedBoundingBoxCollider;
import ru.ckateptb.tablecloth.collision.collider.SphereCollider;
import ru.ckateptb.tablecloth.collision.debug.DebugColliderService;
import ru.ckateptb.tablecloth.config.TableclothConfig;
import ru.ckateptb.tablecloth.math.ImmutableVector;
import ru.ckateptb.tablecloth.spring.SpringContext;
import ru.ckateptb.tablecloth.temporary.TemporaryBossBar;
import ru.ckateptb.tablecloth.temporary.paralyze.TemporaryParalyze;

import java.util.Arrays;

@Component
public class TableclothCommand {
    public TableclothCommand(DebugColliderService debugColliderService) {
        new CommandAPICommand("tablecloth")
                .withPermission("tablecloth.admin")
                .withSubcommand(
                        new CommandAPICommand("reload")
                                .executes((sender, args) -> {
                                    SpringContext.getInstance().getBean(TableclothConfig.class).load();
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("paralyze")
                                .withArguments(new PlayerArgument("target"))
                                .withArguments(new LongArgument("duration"))
                                .executes((sender, args) -> {
                                    Player player = (Player) args[0];
                                    long duration = (long) args[1];
                                    new TemporaryParalyze(player, duration);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("bossbar")
                                .withArguments(new PlayerArgument("target"))
                                .withArguments(new TextArgument("title"))
                                .withArguments(new LongArgument("duration"))
                                .executes((sender, args) -> {
                                    Player player = (Player) args[0];
                                    String title = (String) args[1];
                                    long duration = (long) args[2];
                                    new TemporaryBossBar(title, duration, player);
                                })
                ).withSubcommand(
                new CommandAPICommand("collider")
                        .withSubcommand(
                                new CommandAPICommand("add")
                                        .withSubcommand(
                                                new CommandAPICommand("aabb")
                                                        .withSubcommand(
                                                                new CommandAPICommand("player")
                                                                        .executesPlayer((sender, args) -> {
                                                                            debugColliderService.addCollider(new AxisAlignedBoundingBoxCollider(sender).at(sender.getLocation().toVector()));
                                                                        })
                                                        )
                                                        .withSubcommand(
                                                                new CommandAPICommand("block")
                                                                        .executesPlayer((sender, args) -> {
                                                                            debugColliderService.addCollider(new AxisAlignedBoundingBoxCollider(sender.getWorld(), ImmutableVector.ZERO, ImmutableVector.ONE).at(sender.getLocation().toVector()));
                                                                        })
                                                        )
                                        )
                                        .withSubcommand(
                                                new CommandAPICommand("sphere")
                                                        .withArguments(new DoubleArgument("radius", 0.1, 5))
                                                        .executesPlayer((sender, args) -> {
                                                            double radius = 2;
                                                            if (args != null && args.length > 0) {
                                                                radius = (double) args[0];
                                                            }
                                                            debugColliderService.addCollider(new SphereCollider(sender.getWorld(), sender.getLocation().toVector(), radius));
                                                        })
                                        )
                                        .withSubcommand(
                                                new CommandAPICommand("obb")
                                                        .withArguments(
                                                                new DoubleArgument("minX"),
                                                                new DoubleArgument("minY"),
                                                                new DoubleArgument("minZ"),
                                                                new DoubleArgument("maxX"),
                                                                new DoubleArgument("maxY"),
                                                                new DoubleArgument("maxZ"),
                                                                new DoubleArgument("axisX"),
                                                                new DoubleArgument("axisY"),
                                                                new DoubleArgument("axisZ"),
                                                                new DoubleArgument("rotation")
                                                        )
                                                        .executesPlayer((sender, args) -> {
                                                            if (args == null || args.length < 10) return;
                                                            double[] v = Arrays.stream(args).mapToDouble(arg -> (double) arg).toArray();
                                                            ImmutableVector min = new ImmutableVector(v[0], v[1], v[2]);
                                                            ImmutableVector max = new ImmutableVector(v[3], v[4], v[5]);
                                                            ImmutableVector axis = new ImmutableVector(v[6], v[7], v[8]);
                                                            double radians = Math.toRadians(v[9]);
                                                            AxisAlignedBoundingBoxCollider axisAlignedBoundingBoxCollider = new AxisAlignedBoundingBoxCollider(sender.getWorld(), min, max);
                                                            debugColliderService.addCollider(new OrientedBoundingBoxCollider(axisAlignedBoundingBoxCollider, axis, radians).at(sender.getLocation().toVector()));
                                                        })
                                        )
                                        .withSubcommand(
                                                new CommandAPICommand("disk")
                                                        .withArguments(
                                                                new DoubleArgument("minX"),
                                                                new DoubleArgument("minY"),
                                                                new DoubleArgument("minZ"),
                                                                new DoubleArgument("maxX"),
                                                                new DoubleArgument("maxY"),
                                                                new DoubleArgument("maxZ"),
                                                                new DoubleArgument("axisX"),
                                                                new DoubleArgument("axisY"),
                                                                new DoubleArgument("axisZ"),
                                                                new DoubleArgument("rotation"),
                                                                new DoubleArgument("radius")
                                                        )
                                                        .executesPlayer((sender, args) -> {
                                                            if (args == null || args.length < 11) return;
                                                            double[] v = Arrays.stream(args).mapToDouble(arg -> (double) arg).toArray();
                                                            ImmutableVector min = new ImmutableVector(v[0], v[1], v[2]);
                                                            ImmutableVector max = new ImmutableVector(v[3], v[4], v[5]);
                                                            ImmutableVector axis = new ImmutableVector(v[6], v[7], v[8]);
                                                            double radians = Math.toRadians(v[9]);
                                                            double radius = v[10];
                                                            AxisAlignedBoundingBoxCollider axisAlignedBoundingBoxCollider = new AxisAlignedBoundingBoxCollider(sender.getWorld(), min, max);
                                                            OrientedBoundingBoxCollider orientedBoundingBoxCollider = new OrientedBoundingBoxCollider(axisAlignedBoundingBoxCollider, axis, radians);
                                                            SphereCollider sphereCollider = new SphereCollider(sender.getWorld(), radius);
                                                            debugColliderService.addCollider(new DiskCollider(sender.getWorld(), orientedBoundingBoxCollider, sphereCollider).at(sender.getLocation().toVector()));
                                                        })
                                        )

                        )
                        .withSubcommand(
                                new CommandAPICommand("clear")
                                        .executesPlayer((sender, args) -> {
                                            debugColliderService.clearColliders();
                                        })
                        )
        ).register();

    }
}
