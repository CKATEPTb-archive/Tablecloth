package ru.ckateptb.tablecloth.spring.plugin;


import lombok.SneakyThrows;
import ru.ckateptb.tablecloth.spring.SpringContext;

public abstract class SpringPlugin extends AbstractSpringContextHolder {
    @SneakyThrows
    public SpringPlugin() {
        SpringContext.register(this);
    }
}
