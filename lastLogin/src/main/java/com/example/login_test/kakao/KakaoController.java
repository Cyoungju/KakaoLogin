package com.example.login_test.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoController {
    private final Kakaoservice kakaoService;

    @GetMapping("/kakao/callback")
    public RedirectView callback(String code, HttpSession session) {
        try {
            kakaoService.handleLoginCallback(session, code);
            return new RedirectView("/");
        } catch (Exception e) {
            log.error("카카오 회원가입 또는 로그인 중 오류 발생 : {}", e.getMessage());
            return new RedirectView("/error");
        }
    }

    @GetMapping("/kakao/logout")
    public RedirectView logOut(HttpSession session) {
        try {
            kakaoService.handleLogout(session);
        } catch (Exception e) {
            log.error("카카오 로그아웃 중 오류 발생 : {}", e.getMessage());
            return new RedirectView("/error");
        }

        return new RedirectView("/");
    }

}
