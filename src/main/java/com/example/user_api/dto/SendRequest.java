package com.example.user_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendRequest {
    private String ageGroup;
    private String message;
}