package com.example.login_test.kakao;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KakaoUri {
    @Value("${kakao.api.key}")
    private String API_KEY; // REST API 키

    @Value("${kakao.redirect.uri}")
    private String REDIRECT_URI;
}
