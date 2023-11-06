package com.example.login_test.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KakaoController {
    private final KakaoService kakaoService;


    //redirect 경로 mapping
    @GetMapping("/oauth/kakao")
    public String kakaoLogin(@RequestParam String code, HttpSession session){

        System.out.println("code = " + code);

        //추가됨: 카카오 토큰 요청
        KakaoToken kakaoToken = kakaoService.requestToken(code);
        log.info("kakoToken = {}", kakaoToken);

        //추가됨: 유저정보 요청
        KakaoResponse user = kakaoService.requestUser(kakaoToken.getAccess_token());
        log.info("user = {}",user);

//        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, kakaoToken.getAccess_token())
//                .body(ApiUtils.success(user));

//        ModelAndView modelAndView = new ModelAndView("LoginResult"); // "index"는 Thymeleaf 템플릿 파일명
//        modelAndView.addObject("user", user);
//
        session.setAttribute("access_token", kakaoToken.getAccess_token());

        log.info("토큰: " + String.valueOf(session.getAttribute("access_token")));

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