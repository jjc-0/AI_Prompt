package com.ecommerce.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        RedisAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.ecommerce.agent.repository")
public class ECommerceAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceAgentApplication.class, args);
    }
}
