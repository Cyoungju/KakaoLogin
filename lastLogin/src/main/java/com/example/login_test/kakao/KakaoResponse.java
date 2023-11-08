package com.example.login_test.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
public class KakaoResponse {
    private Long id;
    private String email;
    private String nickname;
    private String thumbnail_image;
    private String properties;
    private String provider;
}
