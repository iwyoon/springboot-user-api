package com.example.user_api.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/kakaotalk-messages")
public class KakaoMockController {

    private static final Logger logger = LoggerFactory.getLogger(KakaoMockController.class);

    @PostMapping
    public ResponseEntity<Void> sendKakao(@RequestBody Map<String, String> body) {
        logger.info("[카톡 모킹] phone: " + body.get("phone") + ", message: " + body.get("message"));
        return ResponseEntity.ok().build(); // 200 OK 반환
    }
}
