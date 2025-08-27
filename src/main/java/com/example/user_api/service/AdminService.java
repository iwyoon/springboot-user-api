package com.example.user_api.service;

import com.example.user_api.dto.UserDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_api.domain.User;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 페이징된 회원 조회
	 *
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return Page<User> 객체
	 */
	public Page<UserDetailResponse> getUsers(int page, int size) {
		Page<User> users = userRepository.findAll(PageRequest.of(page, size));

		return users.map(user -> {
			UserDetailResponse dto = new UserDetailResponse();
			dto.setAccount(user.getAccount());
			dto.setName(user.getName());
			dto.setSsn(user.getSsn());
			dto.setPhone(user.getPhone());
			dto.setAddress(user.getAddress()); // 필요 시 extractRegion(user.getAddress()) 적용
			return dto;
		});
	}

	/**
	 * 회원 수정
	 *
	 * @param id 회원 ID
	 * @param request 수정 정보 DTO
	 * @return 수정된 User 객체
	 * @throws RuntimeException 회원이 존재하지 않을 경우
	 */
	public UserDetailResponse updateUser(Long id, SignupRequest request) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		if (request.getPassword() != null && !request.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (request.getAddress() != null && !request.getAddress().isEmpty()) {
			user.setAddress(request.getAddress());
		}

		User updatedUser = userRepository.save(user);

		UserDetailResponse response = new UserDetailResponse();
		response.setAccount(updatedUser.getAccount());
		response.setName(updatedUser.getName());
		response.setSsn(updatedUser.getSsn());
		response.setPhone(updatedUser.getPhone());
		response.setAddress(updatedUser.getAddress());

		return response;
	}

	/**
	 * 회원 삭제
	 *
	 * @param id 회원 ID
	 * @throws RuntimeException 회원이 존재하지 않을 경우
	 */
	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("사용자를 찾을 수 없습니다.");
		}
		userRepository.deleteById(id);
	}

	/**
	 * 연령대 기준 회원 조회
	 *
	 * @param ageGroup "10s", "20s", ... , "all"
	 * @return 해당 연령대의 회원 리스트
	 */
	public List<User> getUsersByAgeGroup(String ageGroup) {
		return userRepository.findAll()
				.stream()
				.filter(user -> isInAgeGroup(user.getSsn(), ageGroup))
				.toList();
	}

	/**
	 * 주민등록번호 기반 연령대 판단
	 *
	 * @param ssn 주민등록번호
	 * @param ageGroup 연령대 문자열
	 * @return true: 해당 연령대, false: 아님
	 */
	public boolean isInAgeGroup(String ssn, String ageGroup) {
		int age = getAgeFromSsn(ssn);
		switch (ageGroup) {
			case "10s": return age >= 10 && age < 20;
			case "20s": return age >= 20 && age < 30;
			case "30s": return age >= 30 && age < 40;
			case "40s": return age >= 40 && age < 50;
			case "50s": return age >= 50 && age < 60;
			case "60s": return age >= 60 && age < 70;
			case "70s": return age >= 70 && age < 80;
			case "80s": return age >= 80 && age < 90;
			case "90s": return age >= 90 && age < 100;
			case "all": return true;
			default: return false;
		}
	}

	/**
	 * 주민등록번호에서 나이 계산
	 *
	 * @param ssn 주민등록번호
	 * @return 나이
	 */
	public int getAgeFromSsn(String ssn) {
		try {
			if (ssn == null || ssn.length() < 8) {
				return 0;
			}

			String birth = ssn.substring(0, 6);
			int year = Integer.parseInt(birth.substring(0, 2));
			int month = Integer.parseInt(birth.substring(2, 4));
			int day = Integer.parseInt(birth.substring(4, 6));

			char genderCode = ssn.charAt(6);
			int fullYear = (genderCode == '1' || genderCode == '2') ? 1900 + year : 2000 + year;

			LocalDate birthDate = LocalDate.of(fullYear, month, day);
			return Period.between(birthDate, LocalDate.now()).getYears();

		} catch (Exception e) {
			// 유효하지 않은 값이면 age 0 리턴
			return 0;
		}
	}
}