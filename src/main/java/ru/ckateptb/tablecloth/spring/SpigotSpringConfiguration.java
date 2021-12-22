package ru.ckateptb.tablecloth.spring;

import lombok.extern.log4j.Log4j2;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import ru.ckateptb.tablecloth.Tablecloth;
import ru.ckateptb.tablecloth.command.SimpleCommand;
import ru.ckateptb.tablecloth.spring.schedule.ScheduleService;

import java.util.stream.Collectors;

@Log4j2
@Configuration
public class SpigotSpringConfiguration implements SchedulingConfigurer, Listener {
    private final AnnotationConfigApplicationContext context;
    private final Tablecloth tablecloth;

    public SpigotSpringConfiguration(AnnotationConfigApplicationContext context, Tablecloth tablecloth) {
        this.context = context;
        this.tablecloth = tablecloth;
    }

    /**
     * Вызывается после инициализации контейнера в {@link SpringContext#getInstance()}
     * Регистрирует события Spigot в компонентах
     */
    @EventListener
    public void onStartup(ContextRefreshedEvent ignored) {
        PluginManager pluginManager = getPluginManager();
        context.getBeansOfType(Listener.class).values().forEach(listener -> pluginManager.registerEvents(listener, tablecloth));
        Tablecloth.getInstance().getSpringStartUpHandlerList().forEach(handler -> handler.handle(context));

        log.info("SpringBootstrap finished its initialization, you can see the bean list in debug mode");
        log.debug("Beans: \n\t{}", context.getBeansOfType(Object.class).values()
                .stream().map(Object::getClass).map(Class::toString).collect(Collectors.joining("\n\t")));
    }

    /**
     * @return сервер Bukkit
     */
    @Bean
    public Server getServer() {
        return Bukkit.getServer();
    }

    /**
     * @return менеджер плагинов Bukkit
     */
    @Bean
    public PluginManager getPluginManager() {
        return Bukkit.getPluginManager();
    }

    /**
     * Регистрируем собственный сервис для обработки аннотации {@link org.springframework.scheduling.annotation.Scheduled}
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(context.getBean(ScheduleService.class));
    }
}
