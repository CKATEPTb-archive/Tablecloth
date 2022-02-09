package ru.ckateptb.tablecloth.ioc;

import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import ru.ckateptb.tablecloth.ioc.annotation.*;
import ru.ckateptb.tablecloth.ioc.core.BeanContainer;
import ru.ckateptb.tablecloth.ioc.core.CircularDetector;
import ru.ckateptb.tablecloth.ioc.core.ImplementationContainer;
import ru.ckateptb.tablecloth.ioc.exception.IoCBeanNotFound;
import ru.ckateptb.tablecloth.ioc.exception.IoCCircularDepException;
import ru.ckateptb.tablecloth.ioc.exception.IoCException;
import ru.ckateptb.tablecloth.ioc.util.FinderUtil;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IoC<P extends Plugin> {
    private static final Map<Plugin, IoC<Plugin>> instances = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <P extends Plugin> IoC<P> getInstance(P plugin) {
        return (IoC<P>) instances.get(plugin);
    }

    public static <T> T get(Class<T> clazz) {
        for (IoC<?> ioC : instances.values()) {
            T bean = ioC.getBean(clazz);
            if (bean == null) continue;
            return bean;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, String qualifier) {
        for (IoC<?> ioC : instances.values()) {
            Object bean = null;
            try {
                bean = ioC._getBean(clazz, null, qualifier, false);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | IoCBeanNotFound | IoCCircularDepException e) {
                e.printStackTrace();
            }
            if (bean == null) continue;
            return (T) bean;
        }
        return null;
    }

    private static final BeanContainer beanContainer = new BeanContainer();
    private static final ImplementationContainer implementationContainer = new ImplementationContainer();
    private static final CircularDetector circularDetector = new CircularDetector();

    private final P plugin;
    private final ClassLoader classLoader;
    private final Predicate<String> filter;

    public static <P extends Plugin> IoC<P> init(P plugin, Object... predefinedBeans) {
        return init(plugin, packageName -> true, predefinedBeans);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Plugin> IoC<P> init(P plugin, Predicate<String> filter, Object... predefinedBeans) {
        try {
            Class<? extends Plugin> mainClass = plugin.getClass();
            IoC<P> instance = new IoC<>(plugin, mainClass.getClassLoader(), filter);
            instance.initWrapper(mainClass, predefinedBeans);
            instances.put(plugin, (IoC<Plugin>) instance);
            return instance;
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | IoCBeanNotFound | IoCCircularDepException | URISyntaxException e) {
            throw new IoCException(e);
        }
    }

    public <T> T getBean(Class<T> clazz) {
        try {
            return _getBean(clazz);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException | IoCBeanNotFound | IoCCircularDepException e) {
            throw new IoCException(e);
        }
    }

    private void initWrapper(Class<?> mainClass, Object[] predefinedBeans) throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, IoCBeanNotFound, IoCCircularDepException, URISyntaxException {
        if (predefinedBeans != null) {
            Set<Object> beans = Arrays.stream(predefinedBeans).collect(Collectors.toSet());
            beans.add(plugin);
            register(beans);
        }

        ComponentScan scan = mainClass.getAnnotation(ComponentScan.class);
        if (scan == null) {
            this.scan(mainClass.getPackage().getName());
        } else {
            this.scan(scan.value());
        }
    }

    @SneakyThrows
    public void scan(String... packages) {
        for (String packageName : packages) {
            init(packageName);
        }
    }

    public void register(Object... beans) {
        for (Object bean : beans) {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            if (interfaces.length == 0) {
                implementationContainer.putImplementationClass(bean.getClass(), bean.getClass());
            } else {
                for (Class<?> interfaceClass : interfaces) {
                    implementationContainer.putImplementationClass(bean.getClass(), interfaceClass);
                }
            }
            beanContainer.putBean(bean.getClass(), bean);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void init(String packageName) throws IOException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, IoCBeanNotFound, IoCCircularDepException {
        beanContainer.putBean(IoC.class, this);
        implementationContainer.putImplementationClass(IoC.class, IoC.class);
        Set<Class<?>> classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName).stream()
                .filter(classInfo -> filter.test(classInfo.getName()))
                .map(classInfo -> {
                    try {
                        return classInfo.load();
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(cl -> cl.isAnnotationPresent(Component.class) || cl.isAnnotationPresent(Configuration.class))
                .collect(Collectors.toSet());
        scanImplementations(classes);
        scanConfigurationClass(classes);
        scanComponentClasses(classes);
    }

    private void scanImplementations(Set<Class<?>> classes) {
        Set<Class<?>> componentClasses = classes.stream().filter(cl -> cl.isAnnotationPresent(Component.class)).collect(Collectors.toSet());
        for (Class<?> implementationClass : componentClasses) {
            Class<?>[] interfaces = implementationClass.getInterfaces();
            if (interfaces.length == 0) {
                implementationContainer.putImplementationClass(implementationClass, implementationClass);
            } else {
                for (Class<?> interfaceClass : interfaces) {
                    implementationContainer.putImplementationClass(implementationClass, interfaceClass);
                }
            }
        }
        Set<Class<?>> configurationClasses = classes.stream().filter(cl -> cl.isAnnotationPresent(Configuration.class)).collect(Collectors.toSet());
        for (Class<?> configurationClass : configurationClasses) {
            Set<Method> methods = FinderUtil.findMethods(configurationClass, Bean.class);
            for (Method method : methods) {
                Class<?> returnType = method.getReturnType();
                implementationContainer.putImplementationClass(returnType, returnType);
            }
        }
    }

    private void scanConfigurationClass(Set<Class<?>> classes) throws IoCCircularDepException, InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException {
        Deque<Class<?>> configurationClassesQ = new ArrayDeque<>(5);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Configuration.class)) {
                configurationClassesQ.add(clazz);
            }
        }
        while (!configurationClassesQ.isEmpty()) {
            Class<?> configurationClass = configurationClassesQ.removeFirst();
            try {
                Object instance = configurationClass.getConstructor().newInstance();
                circularDetector.detect(configurationClass);
                scanConfigurationBeans(configurationClass, instance);
            } catch (IoCBeanNotFound e) {
                configurationClassesQ.addLast(configurationClass);
            }
        }
    }

    private void scanComponentClasses(Set<Class<?>> classes) throws IoCCircularDepException, InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, IoCBeanNotFound {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                newInstanceWrapper(clazz);
            }
        }
    }

    private void scanConfigurationBeans(Class<?> clazz, Object classInstance) throws InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        Set<Method> methods = FinderUtil.findMethods(clazz, Bean.class);
        Set<Field> fields = FinderUtil.findFields(clazz, Autowired.class);

        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Qualifier.class) ? field.getAnnotation(Qualifier.class).value() : null;
            Object fieldInstance = _getBean(field.getType(), field.getName(), qualifier, false);
            field.set(classInstance, fieldInstance);
        }

        for (Method method : methods) {
            Class<?> beanType = method.getReturnType();
            Object beanInstance = method.invoke(classInstance);
            String name = method.getAnnotation(Bean.class).value();
            beanContainer.putBean(beanType, beanInstance, name);
        }
    }

    private Object newInstanceWrapper(Class<?> clazz) throws InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        if (beanContainer.containsBean(clazz)) {
            return beanContainer.getBean(clazz);
        }

        circularDetector.detect(clazz);

        Object instance = newInstance(clazz);
        beanContainer.putBean(clazz, instance);
        fieldInject(clazz, instance);
        setterInject(clazz, instance);

        if (instance instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        Set<Method> schedules = FinderUtil.findMethods(clazz, Scheduled.class);
        for (Method schedule : schedules) {
            if (schedule.getParameterCount() > 0) {
                new IoCException("Method " + schedule + " must not take parameters").printStackTrace();
                continue;
            }
            Scheduled scheduled = schedule.getAnnotation(Scheduled.class);
            Runnable runnable = () -> {
                try {
                    schedule.invoke(instance);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            };
            if (scheduled.async()) {
                scheduler.runTaskTimerAsynchronously(plugin, runnable, scheduled.delay(), scheduled.period());
            } else {
                scheduler.runTaskTimer(plugin, runnable, scheduled.delay(), scheduled.period());
            }
        }
        return instance;
    }

    private Object newInstance(Class<?> clazz) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException,
            IoCBeanNotFound, IoCCircularDepException {
        Constructor<?> defaultConstructor = FinderUtil.findAnnotatedConstructor(clazz, Autowired.class);
        if (defaultConstructor == null) {
            try {
                defaultConstructor = clazz.getConstructors()[0];
            } catch (Throwable throwable) {
                try {
                    defaultConstructor = clazz.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IoCException("There is no default constructor in class " + clazz.getName());
                }
            }
            defaultConstructor.setAccessible(true);
        }
        Object[] parameters = getParameters(defaultConstructor.getParameterCount(), defaultConstructor.getParameters(), defaultConstructor.getParameterTypes());
        Object instance = defaultConstructor.newInstance(parameters);
        FinderUtil.findMethods(clazz, PostConstruct.class).forEach(post -> {
            if (post.getParameterCount() > 0) {
                new IoCException("Method " + post + " must not take parameters").printStackTrace();
                return;
            }
            try {
                post.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return instance;
    }

    private Object[] getParameters(int parameterCount, Parameter[] params, Class<?>[] types) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IoCBeanNotFound, IoCCircularDepException {
        Object[] parameters = new Object[parameterCount];
        for (int i = 0; i < parameters.length; i++) {
            String qualifier = params[i].isAnnotationPresent(Qualifier.class) ?
                    params[i].getAnnotation(Qualifier.class).value() : null;
            Object depInstance = _getBean(types[i],
                    types[i].getName(), qualifier, true);
            parameters[i] = depInstance;
        }
        return parameters;
    }

    private void setterInject(Class<?> clazz, Object classInstance) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IoCBeanNotFound, IoCCircularDepException {
        Set<Method> methods = FinderUtil.findMethods(clazz, Autowired.class);
        for (Method method : methods) {
            Object[] parameters = getParameters(method.getParameterCount(), method.getParameters(), method.getParameterTypes());
            method.invoke(classInstance, parameters);
        }
    }

    private void fieldInject(Class<?> clazz, Object classInstance) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        Set<Field> fields = FinderUtil.findFields(clazz, Autowired.class);
        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Qualifier.class) ? field.getAnnotation(Qualifier.class).value() : null;
            Object fieldInstance = _getBean(field.getType(), field.getName(), qualifier, true);
            field.set(classInstance, fieldInstance);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T _getBean(Class<T> interfaceClass) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        return (T) _getBean(interfaceClass, null, null, false);
    }

    private <T> Object _getBean(Class<T> interfaceClass, String fieldName, String qualifier, boolean createIfNotFound) throws
            InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            IoCBeanNotFound, IoCCircularDepException {
        Class<?> implementationClass = interfaceClass.isInterface() ?
                implementationContainer.getImplementationClass(interfaceClass, fieldName, qualifier) : interfaceClass;
        if (beanContainer.containsBean(implementationClass)) {
            if (qualifier != null) {
                return beanContainer.getBean(implementationClass, qualifier);
            }
            return beanContainer.getBean(implementationClass);
        }
        if (createIfNotFound) {
            synchronized (beanContainer) {
                return newInstanceWrapper(implementationClass);
            }
        } else {
            throw new IoCBeanNotFound("Cannot found bean for " + interfaceClass.getName());
        }
    }
}
