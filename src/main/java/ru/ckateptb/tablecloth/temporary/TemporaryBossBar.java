package ru.ckateptb.tablecloth.temporary;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

@Getter
@Setter
public class TemporaryBossBar extends AbstractTemporary {
    private BossBar bossBar;
    private Player[] players;
    private long duration;
    private long startTime;

    public TemporaryBossBar(String title, long duration, Player... players) {
        this(title, BarColor.BLUE, BarStyle.SOLID, duration, players);
    }

    public TemporaryBossBar(String title, BarColor color, BarStyle style, long duration, Player... players) {
        this.bossBar = Bukkit.createBossBar(title, color, style);
        this.duration = duration;
        this.players = players;
        this.startTime = System.currentTimeMillis();
        this.register();
    }

    @Override
    public void init() {
        Validate.notNull(players);
        this.bossBar.setProgress(1);
        for (Player player : players) {
            this.bossBar.addPlayer(player);
        }
    }

    @Override
    public TemporaryUpdateState update() {
        if (players.length < 1) return TemporaryUpdateState.REVERT;
        if (duration > 0) {
            double spendMs = System.currentTimeMillis() - this.startTime;
            double subtract = spendMs / this.duration;
            double progress = Math.max(0, 1 - subtract);
            this.bossBar.setProgress(progress);
            if (progress == 0) return TemporaryUpdateState.REVERT;
        }
        return TemporaryUpdateState.CONTINUE;
    }

    @Override
    public void revert() {
        this.bossBar.removeAll();
    }
}
