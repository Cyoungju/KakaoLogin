package com.example.login_test.kakao;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter @Setter @ToString @Component
public class KakaoResponse {
    private Long id;
    private KakaoToken kakao_Token;
    private KakaoAccount kakao_account;

    @Getter @Setter @AllArgsConstructor @ToString @NoArgsConstructor
    public static class KakaoAccount{
        private String email;
        private String name;
        private String phone_number;
    }

    @Getter @Setter @AllArgsConstructor @ToString @NoArgsConstructor
    public static class KakaoToken {
        String token_type;
        String access_token;
        Integer expires_in; //액세스 토큰 만료 시간(초)
        String refresh_token;
        Integer refresh_token_expires_in;
        String scope;
        String code;
    }

}
