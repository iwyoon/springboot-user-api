package com.example.user_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailResponse {
	private String account;
	private String name;
	private String ssn;
	private String phone;
	private String address;
}