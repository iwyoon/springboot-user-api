package com.example.user_api;

import com.example.user_api.dto.LoginRequest;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.domain.User;
import com.example.user_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Base64;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void testSignup() throws Exception {
		SignupRequest request = new SignupRequest();
		request.setAccount("testuser");
		request.setPassword("password");
		request.setName("홍길동");
		request.setSsn("12345678901");
		request.setPhone("01012345678");
		request.setAddress("서울특별시 강남구");

		mockMvc.perform(post("/api/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.account").value("testuser"));
	}

	@Test
	void testLogin() throws Exception {
		User user = new User();
		user.setAccount("testuser");
		user.setPassword(new BCryptPasswordEncoder().encode("password"));
		user.setName("홍길동");
		user.setSsn("12345678901");
		user.setPhone("01012345678");
		user.setAddress("서울특별시 강남구");
		user.setRole("USER");
		userRepository.save(user);

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setAccount("testuser");
		loginRequest.setPassword("password");

		mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.notNullValue()));
	}

	@Test
	void testGetMe() throws Exception {
		User user = new User();
		user.setAccount("testuser");
		user.setPassword(new BCryptPasswordEncoder().encode("password"));
		user.setName("홍길동");
		user.setSsn("12345678901");
		user.setPhone("01012345678");
		user.setAddress("서울특별시 강남구");
		user.setRole("USER");
		userRepository.save(user);

		String userAuth = "Basic " + Base64.getEncoder()
			.encodeToString(("testuser:password").getBytes());

		mockMvc.perform(get("/api/users/me")
				.header("Authorization", userAuth)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.account").value("testuser"))
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.ssn").value("12345678901"))
			.andExpect(jsonPath("$.address").value("서울특별시"));
	}
}