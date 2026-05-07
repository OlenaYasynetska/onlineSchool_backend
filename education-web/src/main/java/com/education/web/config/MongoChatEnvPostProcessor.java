package com.education.web.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Якщо чат на Mongo вимкнено ({@code education.chat.mongodb-enabled=false}),
 * відключає auto-config Mongo — додаток стартує без Atlas / localhost:27017.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class MongoChatEnvPostProcessor implements EnvironmentPostProcessor {

    private static final String ENABLED_KEY = "education.chat.mongodb-enabled";

    private static final String MONGO_AUTO = "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration";
    private static final String MONGO_DATA = "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration";
    private static final String MONGO_REPOS = "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty(ENABLED_KEY, Boolean.class, Boolean.FALSE);
        if (Boolean.TRUE.equals(enabled)) {
            return;
        }

        String existing = environment.getProperty("spring.autoconfigure.exclude");
        Set<String> excludes = new LinkedHashSet<>();
        if (existing != null && !existing.isBlank()) {
            for (String s : existing.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) {
                    excludes.add(t);
                }
            }
        }
        excludes.add(MONGO_AUTO);
        excludes.add(MONGO_DATA);
        excludes.add(MONGO_REPOS);

        Map<String, Object> map = new HashMap<>();
        map.put("spring.autoconfigure.exclude", String.join(",", excludes));
        environment.getPropertySources().addFirst(new MapPropertySource("mongoChatDisabled", map));
    }
}
