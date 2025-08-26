package com.example.user_api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String account;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(unique = true, nullable = false, length = 13)
	private String ssn;

	@Column(nullable = false, length = 11)
	private String phone;

	@Column
	private String address;

	@Column(nullable = false)
	private String role;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}