package ru.ckateptb.tablecloth.spring;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bukkit.craftbukkit.libs.org.apache.http.util.Asserts;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import ru.ckateptb.tablecloth.spring.plugin.SpringContextHolder;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringContext {
    private static AnnotationConfigApplicationContext context;

    private static final ArrayListMultimap<SpringContextHolder, Class<?>> classesMultimap = ArrayListMultimap.create();

    /**
     * Регистрирует плагин, сканируя все компоненты для их добавления в ioc
     *
     * @param holder Главный класс плагина
     */
    public static void register(SpringContextHolder holder) throws IOException {
        register(holder, null);
    }

    /**
     * Регистрирует плагин, сканируя все компоненты для их добавления в ioc
     *
     * @param holder      Главный класс плагина
     * @param basePackage родительский пакет, в котором рекурсивно будут сканироваться компоненты
     * @param filters     фильтр по полному имени класса (пример: ru.ckateptb.tablecloth.awesomepacket.AvesomeClass) для возможности отключения сканирования в этих классах/пакетах
     */
    @SafeVarargs
    public static void register(SpringContextHolder holder, String basePackage, Predicate<String>... filters) throws IOException {
        Class<?> clazz = holder.getClass();
        Asserts.check(ClassUtils.isAssignable(SpringContextHolder.class, clazz), "%s is not SpringPlugin main class", clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        log.info("Found a plugin that requires Spring: {} ", clazz);
        String finalBasePackage = basePackage == null ? clazz.getPackage().getName() : basePackage;
        log.info("Spring scans all components located in {} and beyond", finalBasePackage);
        Set<Class<?>> classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(finalBasePackage).stream()
                .filter(classInfo -> {
                    boolean result = true;
                    for(Predicate<String> filter : filters) {
                        result = filter.test(classInfo.getName());
                        if(!result) break;
                    }
                    return result;
                })
                .map(classInfo -> {
                    try {
                        return classInfo.load();
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(cl -> AnnotatedElementUtils.isAnnotated(cl, Component.class))
                .collect(Collectors.toSet());
        log.info("Spring Scan completed successfully ({} beans)!", classes.size());
        classesMultimap.putAll(holder, classes);
    }

    /**
     * @return ioc контейнер. В случае, если тот не создан, создает его и регистрируем в нем все компоненты,
     * тем самым является методом авторизации
     */
    public static AnnotationConfigApplicationContext getInstance() {
        if (context != null) return context;
        context = new AnnotationConfigApplicationContext();
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        context.register(classesMultimap.values().toArray(new Class<?>[0]));
        classesMultimap.keySet().forEach(key -> {
            key.setContext(context);
            beanFactory.registerResolvableDependency(key.getClass(), key);
        });
        context.refresh();
        return context;
    }
}
