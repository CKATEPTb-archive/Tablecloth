package ru.ckateptb.tablecloth.spring.schedule;

import org.bukkit.Bukkit;
import org.springframework.stereotype.Service;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.spring.schedule.api.AbstractScheduledExecutorService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Собственный сервис для обработки аннотации {@link org.springframework.scheduling.annotation.Scheduled}
 * initialDelay - задержка перед первым вызовом
 * fixedRate - интервал вызовов
 */
@Service
public class ScheduleService extends AbstractScheduledExecutorService {
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period, TimeUnit unit) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Tablecloth.getInstance(), task, delay, period);
        return null;
    }
}
