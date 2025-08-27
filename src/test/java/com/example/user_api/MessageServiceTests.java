package com.example.user_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.user_api.service.MessageService;

@SpringBootTest
public class MessageServiceTests {

	@Autowired
	private MessageService messageService;

	@Test
	void testSend() {
		String phone = "01012345678";
		String message = "테스트";

		for (int i = 1; i <= 120; i++) {  // 120건 호출
			messageService.sendKakao("홍길동", phone, message + " " + i);
		}
	}
}