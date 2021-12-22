package ru.ckateptb.tablecloth.spring.plugin;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.management.InstanceAlreadyExistsException;

@Getter
public abstract class AbstractSpringContextHolder extends JavaPlugin implements SpringContextHolder {
    private AnnotationConfigApplicationContext context;

    @SneakyThrows
    public final void setContext(AnnotationConfigApplicationContext ctx) {
        if (context == null) context = ctx;
        else throw new InstanceAlreadyExistsException();
    }
}
