package com.example.user_api;

import com.example.user_api.domain.User;
import com.example.user_api.dto.MessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.user_api.service.MessageService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class MessageServiceTests {

	@Autowired
	private MessageService messageService;

	@Autowired
	private RedisTemplate<String, MessageRequest> messageRedisTemplate;

	@BeforeEach
	void clearQueue() {
		messageRedisTemplate.delete("kakaoQueue");
		messageRedisTemplate.delete("smsQueue");
	}

	@Test
	void testBatchMessageEnqueue() {
		// 테스트용 임의 사용자 생성
		List<User> users = new ArrayList<>();
		for (int i = 1; i <= 1000; i++) { // 1000명 테스트
			User u = new User();
			u.setName("사용자" + i);
			u.setPhone("0100000" + String.format("%04d", i));
			u.setSsn("9001011" + String.format("%03d", i)); // 30대 예시
			users.add(u);
		}

		String message = "테스트 메시지";

		// 배치 등록
		messageService.enqueueBatch(users, message);

		// Redis에 정상 등록되었는지 확인
		Long queueSize = messageRedisTemplate.opsForList().size("kakaoQueue");
		System.out.println("kakaoQueue 등록 수: " + queueSize);
		assertEquals(users.size(), queueSize);
	}

	@Test
	void testQueueProcessing() throws InterruptedException {
		// 50명만 배치 등록
		List<User> users = new ArrayList<>();
		for (int i = 1; i <= 50; i++) {
			User u = new User();
			u.setName("사용자" + i);
			u.setPhone("0100000" + String.format("%04d", i));
			u.setSsn("9001011" + String.format("%03d", i));
			users.add(u);
		}
		messageService.enqueueBatch(users, "테스트 메시지");

		// processQueue를 수동으로 호출 (스케줄링 없이)
		for (int i = 0; i < 10; i++) {
			messageService.processQueue();
			Thread.sleep(1000); // 1초마다 호출 시뮬레이션
		}

		// 남은 큐 확인
		Long remaining = messageRedisTemplate.opsForList().size("kakaoQueue");
		System.out.println("남은 kakaoQueue: " + remaining);
	}
}