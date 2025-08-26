package com.example.user_api.controller;

import com.example.user_api.domain.User;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.service.AdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	/**
	 * 회원 조회
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<User>> getUsers(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(adminService.getUsers(page, size));
	}

	/**
	 * 회원 수정
	 */
	@PutMapping("/users/{id}")
	public ResponseEntity<User> updateUser(
		@PathVariable("id") Long id,
		@RequestBody SignupRequest request) {
		return ResponseEntity.ok(adminService.updateUser(id, request));
	}

	/**
	 * 회원 삭제
	 */
	@DeleteMapping("/users/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
		adminService.deleteUser(id);
		return ResponseEntity.ok("삭제 완료");
	}
}