package ru.ckateptb.tablecloth;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.ckateptb.tablecloth.spring.SpringContext;
import ru.ckateptb.tablecloth.spring.plugin.AbstractSpringContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Tablecloth extends AbstractSpringContextHolder {
    @Getter
    private static Tablecloth instance;
    private final List<SpringStartUpHandler> springStartUpHandlerList = new ArrayList<>();

    @SneakyThrows
    public Tablecloth() {
        Tablecloth.instance = this;
        SpringContext.register(this, "ru.ckateptb.tablecloth"
                , s -> !s.contains("ru.ckateptb.tablecloth.gui.anvil")
                , s -> !s.contains("ru.ckateptb.tablecloth.storage")
        );
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        SpringContext.getInstance();
    }

    @Override
    public void onDisable() {
        AnnotationConfigApplicationContext context = this.getContext();
        context.close();
    }

    /**
     * Зарегистрировать событие, которое будет выполнено, после загрузки всех компонентов
     *
     * @param handler событие, которое будет выполнено после загрузки всех компонентов
     */
    public void registerSpringStartUpHandler(SpringStartUpHandler handler) {
        springStartUpHandlerList.add(handler);
    }

    public List<SpringStartUpHandler> getSpringStartUpHandlerList() {
        return Collections.unmodifiableList(springStartUpHandlerList);
    }

    public interface SpringStartUpHandler {
        void handle(AnnotationConfigApplicationContext context);
    }
}
