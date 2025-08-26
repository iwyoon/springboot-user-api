package com.example.user_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RateLimiterService rateLimiterService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 회원에게 메시지 발송
     * @param name 회원 이름
     * @param phone 회원 전화번호
     * @param message 메시지 내용
     */
    @Async
    public void sendMessage(String name, String phone, String message) {
        String content = name + "님, 안녕하세요. 현대 오토에버입니다. " + message;

        // 카카오톡 rate limit: 100/min
        String kakaoKey = "kakao:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        boolean kakaoAllowed = rateLimiterService.tryAcquire(kakaoKey, 100, 60);

        boolean sent = false;
        if (kakaoAllowed) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("autoever", "1234");
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, String> body = Map.of("phone", phone, "message", content);
                restTemplate.postForEntity("http://localhost:8081/kakaotalk-messages",
                        new HttpEntity<>(body, headers), Void.class);
                sent = true;
            } catch (Exception e) {
                System.out.println("[카톡 발송 실패] " + phone + " / " + e.getMessage());
            }
        }

        if (!sent) {
            // SMS rate limit: 500/min
            String smsKey = "sms:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            boolean smsAllowed = rateLimiterService.tryAcquire(smsKey, 500, 60);
            if (smsAllowed) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBasicAuth("autoever", "5678");
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                    body.add("message", content);

                    HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(body, headers);

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            "http://localhost:8082/sms?phone=" + phone,
                            request,
                            Map.class
                    );

                    System.out.println("응답: " + response.getBody());
                } catch (Exception e) {
                    System.out.println("[SMS 발송 실패] " + phone + " / " + e.getMessage());
                }
            }
        }
    }
}
