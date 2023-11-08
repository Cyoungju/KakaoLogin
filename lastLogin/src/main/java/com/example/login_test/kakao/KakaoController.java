package com.example.login_test.kakao;

import com.example.login_test.core.error.exception.Exception400;
import com.example.login_test.core.error.exception.Exception500;
import com.example.login_test.user.User;
import com.example.login_test.user.UserRepository;
import com.example.login_test.user.UserRequest;
import com.example.login_test.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoController {
    private final KakaoService kakaoService;

    private final UserService userService;

    //redirect 경로 mapping
    @GetMapping("/oauth/kakao")
    public String kakaoLogin(@RequestParam String code, HttpSession session){

        System.out.println("code = " + code);

        //추가됨: 카카오 토큰 요청
        KakaoToken kakaoToken = kakaoService.requestToken(code);
        log.info("kakoToken = {}", kakaoToken);

        //추가됨: 유저정보 요청
        KakaoResponse kakaoResponse = kakaoService.requestUser(kakaoToken.getAccess_token());

        log.info("user = {}",kakaoResponse);

        session.setAttribute("access_token", kakaoToken.getAccess_token());

        log.info("토큰: " + String.valueOf(session.getAttribute("access_token")));

        kakaoService.kakaoLogin();


        return "redirect:/";
    }

    @RequestMapping(value="/kakao/logout")
    public RedirectView logout(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        kakaoService.kakaoLogout(accessToken);
        session.invalidate(); //>> 로그아웃

        return new RedirectView("http://localhost:8080/");
    }

}