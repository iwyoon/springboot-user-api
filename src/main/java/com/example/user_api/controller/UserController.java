package com.example.user_api.controller;

import com.example.user_api.domain.User;
import com.example.user_api.dto.LoginRequest;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.dto.UserDetailResponse;
import com.example.user_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * 회원가입
	 */
	@PostMapping("/signup")
	public ResponseEntity<User> signup(@RequestBody SignupRequest request) {
		User user = userService.signup(request);
		return ResponseEntity.ok(user);
	}

	/**
	 * 로그인
	 */
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody LoginRequest request) {
		userService.login(request);
		return ResponseEntity.ok("로그인 성공");
	}

	/**
	 * 본인 상세정보 조회
	 */
	@GetMapping("/me")
	public ResponseEntity<UserDetailResponse> getMyInfo(Authentication authentication) {
		UserDetailResponse response = userService.getUserDetail(authentication);
		return ResponseEntity.ok(response);
	}
}