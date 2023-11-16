package com.example.login_test.kakao;

import com.example.login_test.core.security.JwtTokenProvider;
import com.example.login_test.core.utils.ApiUtils;
import com.example.login_test.user.UserRequest;
import com.example.login_test.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoController {
    private final Kakaoservice kakaoService;
    private final KakaoResponse.KakaoToken kakaoToken;


    @GetMapping("/kakao/callback")
    public RedirectView callback(String code, HttpSession session) {
        try {
            String jwt = kakaoService.joinOrLogin(code);

            log.info("JWT : {}", jwt);

            String accessToken= kakaoToken.getAccess_token();
            log.info(accessToken);
            // 세션에 토큰 저장 - 로그아웃을 위해
            session.setAttribute("accessToken", accessToken);

            return new RedirectView("/");
        } catch (Exception e) {
            log.error("카카오 회원가입 또는 로그인 중 오류 발생 : {}", e.getMessage());
            // 오류 처리 또는 예외 메시지를 보여주는 페이지로 리다이렉트
            return new RedirectView("/error");
        }
    }

    @GetMapping("/kakao/logout")
    public RedirectView logOut(HttpSession session) {
        try {
            // 세션에서 토큰 가져오기
            String accessToken = (String) session.getAttribute("accessToken");
            log.info(accessToken);

            if (accessToken != null) {
                // 로그아웃 처리 및 토큰 만료
                kakaoService.kakaoLogOut(accessToken);
                session.removeAttribute("accessToken"); // 세션에서 토큰 제거

                log.info("로그아웃 완료");
            }

        } catch (Exception e) {
            // 오류 처리 또는 예외 메시지를 보여주는 페이지로 리다이렉트
            log.error("카카오 로그아웃 중 오류 발생 : {}", e.getMessage());
            return new RedirectView("/error");
        }

        return new RedirectView("/");
    }

}











