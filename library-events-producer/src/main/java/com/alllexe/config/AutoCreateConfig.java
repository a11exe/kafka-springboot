/*
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 24.04.2020
 */
package com.alllexe.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("local")
public class AutoCreateConfig {

    @Bean
    public NewTopic libraryEvents() {
        return TopicBuilder.name("library-events")
//                .partitions(3)
//                .replicas(3)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
