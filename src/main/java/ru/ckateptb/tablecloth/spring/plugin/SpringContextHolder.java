package ru.ckateptb.tablecloth.spring.plugin;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public interface SpringContextHolder {
    AnnotationConfigApplicationContext getContext();

    void setContext(AnnotationConfigApplicationContext context);
}
