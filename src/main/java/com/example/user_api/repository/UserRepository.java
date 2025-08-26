package com.example.user_api.repository;

import com.example.user_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByAccount(String account);
	boolean existsByAccount(String account);
	boolean existsBySsn(String ssn);
}