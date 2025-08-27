package com.example.user_api.service;

import com.example.user_api.domain.User;
import com.example.user_api.dto.LoginRequest;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.dto.UserDetailResponse;
import com.example.user_api.repository.UserRepository;
import com.example.user_api.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입
	 *
	 * @param request 회원가입 요청 DTO
	 * @return 저장된 User 엔티티
	 * @throws RuntimeException 이미 사용 중인 계정 또는 주민등록번호가 존재할 경우
	 */
	public User signup(SignupRequest request) {
		if (userRepository.existsByAccount(request.getAccount())) {
			throw new RuntimeException("이미 사용 중인 계정입니다.");
		}
		if (userRepository.existsBySsn(request.getSsn())) {
			throw new RuntimeException("이미 등록된 주민등록번호입니다.");
		}

		User user = new User();
		user.setAccount(request.getAccount());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getName());
		user.setSsn(request.getSsn());
		user.setPhone(request.getPhone());
		user.setAddress(request.getAddress());
		user.setRole("USER");

		return userRepository.save(user);
	}

	/**
	 * 로그인
	 *
	 * @param request 로그인 요청 DTO
	 * @throws RuntimeException 계정이 없거나 비밀번호가 일치하지 않을 경우
	 */
	public void login(LoginRequest request) {
		User user = userRepository.findByAccount(request.getAccount())
			.orElseThrow(() -> new RuntimeException("존재하지 않는 계정입니다."));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}
	}

	/**
	 * 본인 상세 정보 조회
	 *
	 * @param authentication Spring Security 인증 객체
	 * @return 사용자 상세 응답 DTO (주소는 가장 큰 단위만 포함)
	 * @throws RuntimeException 인증 정보가 잘못되었거나 사용자가 없을 경우
	 */
	public UserDetailResponse getUserDetail(Authentication authentication) {
		Object principal = authentication.getPrincipal();

		if (!(principal instanceof CustomUserDetails userDetails)) {
			throw new RuntimeException("인증 정보가 올바르지 않습니다.");
		}

		// username(account)로 User 조회
		User user = userRepository.findByAccount(userDetails.getUsername())
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		UserDetailResponse response = new UserDetailResponse();
		response.setAccount(user.getAccount());
		response.setName(user.getName());
		response.setSsn(user.getSsn());
		response.setPhone(user.getPhone());
		response.setAddress(extractRegion(user.getAddress()));

		return response;
	}

	/**
	 * 주소에서 가장 큰 행정구역 단위 추출
	 *
	 * @param address 전체 주소 문자열
	 * @return 가장 큰 행정구역 단위
	 */
	private String extractRegion(String address) {
		if (address == null || address.isEmpty()) return "";
		return address.split(" ")[0];
	}
}