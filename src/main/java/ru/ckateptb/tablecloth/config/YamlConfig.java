package ru.ckateptb.tablecloth.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class YamlConfig {
    @PostConstruct
    public void init() {
        this.load();
        this.save();
    }
    /**
     * @return Название файла конфигурации с расширением yml
     */
    public String getName() {
        return "config.yml";
    }

    /**
     * @return Комментарий в начале файла
     */
    public String getHeader() {
        return String.format("File configuration for %s plugin", getPlugin().getName());
    }

    /**
     * Сохранить объект в файл конфигурации, сохраняя все переменные с аннотацией {@link ConfigField}
     */
    @SneakyThrows
    public void save() {
        Class<? extends YamlConfig> configurationClass = getClass();
        Field[] fields = configurationClass.getDeclaredFields();
        YamlConfiguration config = new YamlConfiguration();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);
            ConfigField configField = field.getAnnotation(ConfigField.class);
            String configFieldName = configField.name().trim();
            String fieldName = configFieldName.isEmpty() ? field.getName() : configFieldName;
            String configFieldComment = configField.comment().trim();
            if (!configFieldComment.isEmpty()) {
                config.set(fieldName + "_COMMENT", configFieldComment);
            }
            config.set(fieldName, field.get(this));
        }
        String stringConfig = config.saveToString();
        String header = this.getHeader();
        if (!header.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : header.split("\n")) {
                stringBuilder.append("# ").append(line).append("\n");
            }
            stringConfig = stringBuilder + stringConfig;
        }
        Matcher matcher = Pattern.compile("(?:[A-Za-z0-9]*?)_COMMENT: ?(.*?)(\\n[^:\\n]*?:)", Pattern.DOTALL).matcher(stringConfig);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            String comm = matcher.group(1);
            comm = "# " + Pattern.compile("\n( *)").matcher(comm).replaceAll("\n$1# ");
            comm += matcher.group(2);
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(comm));
        }
        matcher.appendTail(stringBuilder);
        stringConfig = stringBuilder.toString();

        File configFile = new File(getPlugin().getDataFolder(), getName());
        FileUtils.makeParentDirs(configFile);
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
        fileWriter.write(stringConfig);
        fileWriter.close();
    }

    /**
     * Загрузить объект из файл конфигурации, обновляя все переменные с аннотацией {@link ConfigField}
     * Если файл конфигурации отсутствует - применяется значение по-умолчанию
     */
    @SneakyThrows
    public void load() {
        File configFile = new File(getPlugin().getDataFolder(), getName());
        String stringConfig = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            stringConfig = Pattern.compile("\\r?\\n? *?#[^\\r\\n]*").matcher(stringBuilder.toString()).replaceAll("");
            while (stringConfig.startsWith("\n")) {
                stringConfig = stringConfig.substring(1);
            }
            stringConfig = stringConfig.replaceAll("\n+", "\n");
        } catch (IOException ignored) { }
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(stringConfig);

        Class<? extends YamlConfig> configurationClass = getClass();
        Field[] fields = configurationClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);
            ConfigField configField = field.getAnnotation(ConfigField.class);
            String configFieldName = configField.name().trim();
            String fieldName = configFieldName.isEmpty() ? field.getName() : configFieldName;
            field.set(this, config.get(fieldName, field.get(this)));
        }
    }

    /**
     * @return Плагин, в папке которого будет находится файл с конфигурацией
     */
    abstract public Plugin getPlugin();
}
