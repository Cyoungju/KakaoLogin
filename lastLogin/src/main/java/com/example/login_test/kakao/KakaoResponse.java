package com.example.login_test.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KakaoResponse {
    private Long id;
    private Properties properties;
    private KakaoAccount kakao_account;

    @Getter
    @ToString
    public static class KakaoAccount{
        private String email;
        private String name;
        private String phone_number;
    }

    @Getter
    @ToString
    public static class Properties{
        private String nickname;
    }
}
