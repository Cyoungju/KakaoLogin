package com.example.login_test.user;

import com.example.login_test.kakao.KakaoUri;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final KakaoUri kakaoUri;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("api", kakaoUri.getAPI_KEY());
        model.addAttribute("logOutRedirect", kakaoUri.getLOGOUT_REDIRECT_URI());
        return "index";
    }
    @GetMapping("/join")
    public String join(Model model) {
        model.addAttribute("api", kakaoUri.getAPI_KEY());
        model.addAttribute("redirect", kakaoUri.getREDIRECT_URI());
        return "join"; // "join.html" 파일을 렌더링
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // "login.html" 파일을 렌더링
    }

}
