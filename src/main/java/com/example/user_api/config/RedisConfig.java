package com.example.user_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory("localhost", 6379);
	}

	@Bean
	public RedisTemplate<String, Long> redisTemplate(LettuceConnectionFactory factory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		return template;
	}
}
