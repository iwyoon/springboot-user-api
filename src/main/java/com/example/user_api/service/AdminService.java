package com.example.user_api.service;

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

	public Page<User> getUsers(int page, int size) {
		return userRepository.findAll(PageRequest.of(page, size));
	}

	public User updateUser(Long id, SignupRequest request) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		if (request.getPassword() != null && !request.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (request.getAddress() != null && !request.getAddress().isEmpty()) {
			user.setAddress(request.getAddress());
		}

		return userRepository.save(user);
	}

	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("사용자를 찾을 수 없습니다.");
		}
		userRepository.deleteById(id);
	}

	public List<User> getUsersByAgeGroup(String ageGroup) {
		return userRepository.findAll()
				.stream()
				.filter(user -> isInAgeGroup(user.getSsn(), ageGroup))
				.toList();
	}

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