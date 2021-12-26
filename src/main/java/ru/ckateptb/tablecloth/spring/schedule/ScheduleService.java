package ru.ckateptb.tablecloth.spring.schedule;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.springframework.stereotype.Service;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.spring.schedule.api.AbstractScheduledExecutorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Собственный сервис для обработки аннотации {@link org.springframework.scheduling.annotation.Scheduled}
 * initialDelay - задержка перед первым вызовом
 * fixedRate - интервал вызовов
 */
@Service
public class ScheduleService extends AbstractScheduledExecutorService {
    private final List<ScheduleTask> schedulers = new ArrayList<>();

    public ScheduleService(Tablecloth tablecloth) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(tablecloth, this::tick, 0, 1);
    }

    public boolean remove(ScheduleTask task) {
        return schedulers.remove(task);
    }

    public void tick() {
        new CopyOnWriteArrayList<>(schedulers).forEach(ScheduleTask::tick);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduleTask scheduleTask = new ScheduleTask(this, task, initialDelay, period);
        schedulers.add(scheduleTask);
        return null;
    }

    @Getter
    private static class ScheduleTask {
        private final ScheduleService service;
        private final Runnable runnable;
        private final long rate;

        private long ticksLeft;

        private ScheduleTask(ScheduleService service, Runnable runnable, long delay, long rate) {
            this.service = service;
            this.runnable = runnable;
            this.ticksLeft = Math.max(delay, 0);
            this.rate = rate;
        }


        private void tick() {
            if (ticksLeft-- == 0) {
                runnable.run();
                if (rate <= 0) service.remove(this);
                else ticksLeft = rate;
            }
        }
    }
}
