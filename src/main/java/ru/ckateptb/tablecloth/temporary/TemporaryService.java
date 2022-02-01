package ru.ckateptb.tablecloth.temporary;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.ioc.annotation.Component;
import ru.ckateptb.tablecloth.ioc.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class TemporaryService implements Listener {
    private final List<Temporary> temporaryList = new ArrayList<>();

    /**
     * Регистрирует временный объект в сервисе, для дальнейшей работы с ним
     *
     * @param temporary объект, который необходимо зарегистрировать в этом сервисе
     */
    public void register(Temporary temporary) {
        temporary.init();
        temporaryList.add(temporary);
    }

    /**
     * Каждый тик вызывает {@link Temporary#update()} и возвращает объект в исходное состояние в случае необходимости
     */
    @Scheduled(period = 1)
    public void updateAll() {
        List<Temporary> revertList = new ArrayList<>();
        temporaryList.forEach(temporary -> {
            long revertTime = temporary.getRevertTime();
            if ((revertTime != -1 && System.currentTimeMillis() >= revertTime) || temporary.update() == TemporaryUpdateState.REVERT) {
                revertList.add(temporary);
            }

        });
        revertList.forEach(this::revert);
        revertList.clear();
    }

    /**
     * Возвращает указанные временный объект в исходное состояние
     *
     * @param temporary - временный объект, который необходимо вернуть в исходное состояние
     */
    public void revert(Temporary temporary) {
        if(temporaryList.remove(temporary)) {
            Temporary.FinalHandler finalHandler = temporary.getFinalHandler();
            if (finalHandler != null) finalHandler.on();
            temporary.revert();
        }
    }

    /**
     * Возвращает все временные объекты в исходное состояние
     */
    public void revertAll() {
        Iterator<Temporary> iterator = temporaryList.iterator();
        while (iterator.hasNext()) {
            Temporary temporary = iterator.next();
            iterator.remove();
            Temporary.FinalHandler finalHandler = temporary.getFinalHandler();
            if (finalHandler != null) finalHandler.on();
            temporary.revert();
        }
    }

    public List<Temporary> getTemporaryList() {
        return List.copyOf(temporaryList);
    }

    @EventHandler
    public void on(PluginDisableEvent event) {
        if(event.getPlugin().equals(Tablecloth.getInstance())) {
            log.info("Reverting all Temporaries");
            this.revertAll();
        }
    }
}
