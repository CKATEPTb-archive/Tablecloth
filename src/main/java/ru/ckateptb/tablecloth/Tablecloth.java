package ru.ckateptb.tablecloth;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ckateptb.tablecloth.ioc.IoC;

public final class Tablecloth extends JavaPlugin {
    @Getter
    private static Tablecloth instance;
    @Getter
    private static IoC<Tablecloth> ioC;

    @SneakyThrows
    public Tablecloth() {
        Tablecloth.instance = this;
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        ioC = IoC.init(this, s -> !s.contains("ru.ckateptb.tablecloth.gui.anvil") && !s.contains("ru.ckateptb.tablecloth.storage"));
    }
}
