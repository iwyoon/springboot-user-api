package com.example.user_api.mock;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/sms")
public class SmsMockController {

    private static final Logger logger = LoggerFactory.getLogger(SmsMockController.class);

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> sendSms(
            @RequestParam("phone") String phone,
            @RequestParam MultiValueMap<String, String> formData
    ) {
        String message = formData.getFirst("message");
        if (message == null) message = "";

        logger.info("[SMS 모킹] phone: " + phone + ", message: " + message);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("result", "OK"));
    }
}
