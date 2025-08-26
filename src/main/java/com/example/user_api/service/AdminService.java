package com.example.user_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_api.domain.User;
import com.example.user_api.dto.SignupRequest;
import com.example.user_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
}