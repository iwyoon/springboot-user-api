package com.example.user_api.config;

import com.example.user_api.dto.MessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory("localhost", 6379);
	}

	// RateLimiter용 RedisTemplate (Long)
	@Bean(name = "rateLimiterRedisTemplate")
	public RedisTemplate<String, Long> rateLimiterRedisTemplate(LettuceConnectionFactory factory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}

	// 메시지 큐용 RedisTemplate (MessageRequest)
	@Bean(name = "messageRedisTemplate")
	public RedisTemplate<String, MessageRequest> messageRedisTemplate(LettuceConnectionFactory factory) {
		// ObjectMapper 설정
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Jackson2JsonRedisSerializer 생성자에 ObjectMapper 전달
		Jackson2JsonRedisSerializer<MessageRequest> serializer =
				new Jackson2JsonRedisSerializer<>(objectMapper, MessageRequest.class);

		RedisTemplate<String, MessageRequest> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.afterPropertiesSet();

		return template;
	}
}