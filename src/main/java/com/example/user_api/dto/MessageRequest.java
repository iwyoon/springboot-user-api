package com.example.user_api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest implements Serializable {
    private String name;
    private String phone;
    private String message;
    private LocalDateTime retryAt; // 재시도 시각
}