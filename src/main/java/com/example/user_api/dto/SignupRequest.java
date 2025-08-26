package com.example.user_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
	private String account;
	private String password;
	private String name;
	private String ssn;     // 주민등록번호
	private String phone;
	private String address;
}