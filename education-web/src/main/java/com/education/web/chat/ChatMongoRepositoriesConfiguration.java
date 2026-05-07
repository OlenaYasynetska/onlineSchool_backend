package com.education.web.chat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnProperty(name = "education.chat.mongodb-enabled", havingValue = "true")
@EnableMongoRepositories(basePackages = "com.education.web.chat.repository")
public class ChatMongoRepositoriesConfiguration {
}
