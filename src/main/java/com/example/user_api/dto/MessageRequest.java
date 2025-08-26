package com.example.user_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String ageGroup;
    private String message;
}
