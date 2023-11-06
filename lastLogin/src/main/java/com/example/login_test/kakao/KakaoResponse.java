package com.example.login_test.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoResponse {
    Long id;
    private String email;
    private String nickname;
    private String thumbnail_image;
    private String properties;
}
