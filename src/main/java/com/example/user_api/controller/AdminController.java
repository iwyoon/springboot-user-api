package com.example.user_api.controller;

import com.example.user_api.domain.User;
import com.example.user_api.dto.SendRequest;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.service.AdminService;

import com.example.user_api.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;
	private final MessageService messageService;

	/**
	 * 회원 조회 (페이징)
	 *
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return 회원 목록
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<User>> getUsers(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(adminService.getUsers(page, size));
	}

	/**
	 * 회원 수정
	 *
	 * @param id 회원 ID
	 * @param request 회원 수정 DTO
	 * @return 수정된 User 객체
	 */
	@PutMapping("/users/{id}")
	public ResponseEntity<User> updateUser(
		@PathVariable("id") Long id,
		@RequestBody SignupRequest request) {
		return ResponseEntity.ok(adminService.updateUser(id, request));
	}

	/**
	 * 회원 삭제
	 *
	 * @param id 회원 ID
	 * @return 삭제 완료 메시지
	 */
	@DeleteMapping("/users/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
		adminService.deleteUser(id);
		return ResponseEntity.ok("삭제 완료");
	}

	/**
	 * 메시지 전송
	 *
	 * @param request 전송 요청 DTO
	 * @return 발송 시작 메시지
	 */
	@PostMapping("/messages/send")
	public ResponseEntity<String> sendMessages(@RequestBody SendRequest request) {
		String ageGroup = request.getAgeGroup();
		List<User> members = adminService.getUsersByAgeGroup(ageGroup);

		int batchSize = 10000;
		for (int i = 0; i < members.size(); i += batchSize) {
			List<User> batch = members.subList(i, Math.min(i + batchSize, members.size()));
			messageService.enqueueBatch(batch, request.getMessage());
		}

		return ResponseEntity.ok("발송 시작");
	}
}