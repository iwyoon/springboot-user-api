package com.example.user_api;

import com.example.user_api.domain.User;
import com.example.user_api.dto.MessageRequest;
import com.example.user_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private String basicAuth;

	@BeforeEach
	void setUp() {
		User user = new User();
		user.setAccount("testuser");
		user.setPassword(new BCryptPasswordEncoder().encode("password"));
		user.setName("홍길동");
		user.setSsn("0102033456789");
		user.setPhone("01012345678");
		user.setAddress("서울특별시 강남구");
		user.setRole("USER");
		userRepository.save(user);

		// Basic Auth 헤더 (admin 계정)
		basicAuth = "Basic " + Base64.getEncoder().encodeToString("admin:1212".getBytes());
	}

	@Test
	void testGetUsers() throws Exception {
		mockMvc.perform(get("/api/admin/users")
				.header("Authorization", basicAuth)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].account").value("admin"))
			.andExpect(jsonPath("$.content[1].account").value("testuser"));
	}

	@Test
	void testUpdateUser() throws Exception {
		Long userId = userRepository.findAll().stream()
			.filter(u -> !"admin".equals(u.getAccount()))
			.findFirst()
			.orElseThrow()
			.getId();

		String payload = "{ \"password\": \"newpass\", \"address\": \"경기도 성남시\" }";

		mockMvc.perform(put("/api/admin/users/" + userId)
				.header("Authorization", basicAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.address").value("경기도 성남시"));
	}

	@Test
	void testDeleteUser() throws Exception {
		Long userId = userRepository.findAll().stream()
			.filter(u -> !"admin".equals(u.getAccount()))
			.findFirst()
			.orElseThrow()
			.getId();

		// 삭제 요청
		mockMvc.perform(delete("/api/admin/users/" + userId)
				.header("Authorization", basicAuth))
			.andExpect(status().isOk())
			.andExpect(content().string("삭제 완료"));

		// 삭제 후 사용자 없음
		mockMvc.perform(get("/api/admin/users")
				.header("Authorization", basicAuth)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].account").value("admin"));
	}

	@Test
	void testSendMessages() throws Exception {
		MessageRequest request = new MessageRequest();
		request.setAgeGroup("20s");  // 20대만 필터링
		request.setMessage("이번 주 이벤트 안내");

		String json = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/admin/messages/send")
						.header("Authorization", basicAuth)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(content().string("발송 시작"));
	}
}