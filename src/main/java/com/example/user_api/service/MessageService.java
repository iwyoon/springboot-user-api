package com.example.user_api.service;

import com.example.user_api.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.example.user_api.dto.MessageRequest;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RateLimiterService rateLimiterService;

    @Qualifier("messageRedisTemplate")
    private final RedisTemplate<String, MessageRequest> redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 배치로 메시지 큐에 등록
     */
    public void enqueueBatch(List<User> users, String message) {
        users.forEach(user -> {
            MessageRequest req = new MessageRequest(user.getName(), user.getPhone(), message, LocalDateTime.now());
            redisTemplate.opsForList().rightPush("kakaoQueue", req);
        });
    }

    /**
     * 1초마다 큐 처리
     */
    @Scheduled(fixedRate = 1000)
    public void processQueue() {
        LocalDateTime now = LocalDateTime.now();
        int kakaoLimit = 100;
        int smsLimit = 500;

        for (int i = 0; i < kakaoLimit; i++) {
            MessageRequest req = redisTemplate.opsForList().leftPop("kakaoQueue");
            if (req == null) break;
            String key = "kakao:" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            if (rateLimiterService.tryAcquire(key, kakaoLimit, 60)) {
                sendKakao(req.getName(), req.getPhone(), req.getMessage());
            } else {
                redisTemplate.opsForList().rightPush("kakaoQueue", req);
                break;
            }
        }

        for (int i = 0; i < smsLimit; i++) {
            MessageRequest req = redisTemplate.opsForList().leftPop("smsQueue");
            if (req == null) break;
            String key = "sms:" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            if (rateLimiterService.tryAcquire(key, smsLimit, 60)) {
                sendSms(req.getName(), req.getPhone(), req.getMessage());
            } else {
                redisTemplate.opsForList().rightPush("smsQueue", req);
                break;
            }
        }
    }

    /**
     * 카카오톡 메시지 전송
     * - 전송 실패 시 SMS로 대체
     *
     * @param name 수신자 이름
     * @param phone 수신자 전화번호
     * @param message 메시지 내용
     */

    @Async
    public void sendKakao(String name, String phone, String message) {
        String content = name + "님, 안녕하세요. 현대 오토에버입니다. " + message;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("autoever", "1234");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("phone", phone, "message", content);
            restTemplate.postForEntity("http://localhost:8081/kakaotalk-messages",
                    new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            logger.error("[카카오톡 전송 실패] " + phone);
            enqueueSmsFallback(name, phone, content);
        }
    }

    private void enqueueSmsFallback(String name, String phone, String message) {
        MessageRequest req = new MessageRequest(name, phone, message, LocalDateTime.now());
        redisTemplate.opsForList().rightPush("smsQueue", req);
    }

    /**
     * SMS 전송
     *
     * @param name 수신자 이름
     * @param phone 수신자 전화번호
     * @param message 메시지 내용
     */
    @Async
    public void sendSms(String name, String phone, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("autoever", "5678");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("message", message);

            restTemplate.postForEntity("http://localhost:8082/sms?phone=" + phone,
                    new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            logger.error("[SMS 전송 실패] " + phone);
        }
    }
}