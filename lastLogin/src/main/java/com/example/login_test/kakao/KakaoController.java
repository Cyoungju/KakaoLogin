package com.example.login_test.kakao;

import com.example.login_test.core.security.JwtTokenProvider;
import com.example.login_test.core.utils.ApiUtils;
import com.example.login_test.user.UserRequest;
import com.example.login_test.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoController {
    private final Kakaoservice kakaoService;


    @GetMapping("/kakao/callback")
    public RedirectView callback(String code) {
        try {
            String jwt = kakaoService.joinOrLogin(code);
            log.info("JWT : {}", jwt);
            return new RedirectView("/");
        } catch (Exception e) {
            log.error("카카오 회원가입 또는 로그인 중 오류 발생 : {}", e.getMessage());
            // 오류 처리 또는 예외 메시지를 보여주는 페이지로 리다이렉트
            return new RedirectView("/error");
        }
    }
}