package ru.ckateptb.tablecloth.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;
import ru.ckateptb.tablecloth.config.TableclothConfig;
import ru.ckateptb.tablecloth.spring.SpringContext;
import ru.ckateptb.tablecloth.temporary.TemporaryBossBar;
import ru.ckateptb.tablecloth.temporary.paralyze.TemporaryParalyze;

@Component
public class TableclothCommand {
    public TableclothCommand() {
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
                )
                .register();
    }
}
