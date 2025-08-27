package com.example.user_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.user_api.dto.MessageRequest;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RateLimiterService rateLimiterService;
    private final RestTemplate restTemplate = new RestTemplate();

    // 재시도 큐
    private final Queue<MessageRequest> kakaoRetryQueue = new ConcurrentLinkedQueue<>();
    private final Queue<MessageRequest> smsRetryQueue = new ConcurrentLinkedQueue<>();

    /**
     * 재시도 큐를 1초마다 처리
     * - 카톡: 1분 최대 100건
     * - SMS: 1분 최대 500건
     */
    @Scheduled(fixedRate = 1000) // 1초마다 체크
    public void processRetryQueue() {
        LocalDateTime now = LocalDateTime.now();

        // 카톡 재시도 처리
        int kakaoRemaining = Math.max(0, 100 - (int) rateLimiterService.getCount(
            "kakao:" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))));
        for (int i = 0; i < kakaoRemaining; i++) {
            MessageRequest req = kakaoRetryQueue.peek();
            if (req == null || req.getRetryAt().isAfter(now)) break;
            kakaoRetryQueue.poll();
            sendKakao(req.getName(), req.getPhone(), req.getMessage());
        }

        // SMS 재시도 처리
        int smsRemaining = Math.max(0, 500 - (int) rateLimiterService.getCount(
            "sms:" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))));
        for (int i = 0; i < smsRemaining; i++) {
            MessageRequest req = smsRetryQueue.peek();
            if (req == null || req.getRetryAt().isAfter(now)) break;
            smsRetryQueue.poll();
            sendSms(req.getName(), req.getPhone(), req.getMessage());
        }

        System.out.println("[큐 상태] 카톡: " + kakaoRetryQueue.size() + ", SMS: " + smsRetryQueue.size());
    }

    /**
     * 카카오톡 메시지 전송
     * - Rate Limit 초과 시 재시도 큐에 적재
     * - 전송 실패 시 SMS로 대체
     *
     * @param name 수신자 이름
     * @param phone 수신자 전화번호
     * @param message 메시지 내용
     */
    @Async
    public void sendKakao(String name, String phone, String message) {
        String content = name + "님, 안녕하세요. 현대 오토에버입니다. " + message;

        // 카톡 Rate Limit 체크
        String kakaoKey = "kakao:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        boolean kakaoAllowed = rateLimiterService.tryAcquire(kakaoKey, 100, 60);

        if (kakaoAllowed) {
            try {
                callKakaoApi(phone, content);
            } catch (Exception e) {
                System.out.println("[카톡 실패 - API 오류] " + phone);
                // 카톡 실패 시 SMS 시도
                sendSms(name, phone, content);
            }
        } else {
            // 카톡 Rate Limit 초과 → 재시도 큐에 추가
            kakaoRetryQueue.add(new MessageRequest(name, phone, content, LocalDateTime.now().plusMinutes(1)));
            System.out.println("[카톡 Rate Limit 초과] 재시도 큐에 추가: " + phone);
        }
    }

    private void callKakaoApi(String phone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("autoever", "1234");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("phone", phone, "message", message);
        restTemplate.postForEntity("http://localhost:8081/kakaotalk-messages",
            new HttpEntity<>(body, headers), Void.class);
    }

    /**
     * SMS 전송
     * - Rate Limit 초과 시 재시도 큐에 적재
     *
     * @param name 수신자 이름
     * @param phone 수신자 전화번호
     * @param message 메시지 내용
     */
    private void sendSms(String name, String phone, String message) {
        String smsKey = "sms:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        boolean smsAllowed = rateLimiterService.tryAcquire(smsKey, 500, 60);

        if (smsAllowed) {
            try {
                callSmsApi(phone, message);
            } catch (Exception e) {
                System.out.println("[SMS 실패 - API 오류] " + phone);
            }
        } else {
            // SMS Rate Limit 초과 → 재시도 큐에 추가
            smsRetryQueue.add(new MessageRequest(name, phone, message, LocalDateTime.now().plusMinutes(1)));
            System.out.println("[SMS Rate Limit 초과] 재시도 큐에 추가: " + phone);
        }
    }

    private void callSmsApi(String phone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("autoever", "5678");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("message", message);

        restTemplate.postForEntity("http://localhost:8082/sms?phone=" + phone,
            new HttpEntity<>(body, headers), Map.class);
    }
}