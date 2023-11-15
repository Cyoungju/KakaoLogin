package com.example.login_test.user;

import com.example.login_test.core.security.JwtTokenProvider;
import com.example.login_test.core.utils.ApiUtils;
import com.example.login_test.kakao.KakaoUri;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final KakaoUri kakaoUri;
    @GetMapping("/join")
    public String join(Model model) {
        model.addAttribute("api", kakaoUri.getAPI_KEY());
        model.addAttribute("redirect", kakaoUri.getREDIRECT_URI());
        return "join"; // "join.html" 파일을 렌더링
    }
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDTO requestDTO, Error error) {
        userService.join(requestDTO);
        return ResponseEntity.ok( ApiUtils.success(null) );
    }
    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody @Valid UserRequest.JoinDTO requestDTO, Error error) {
        userService.checkEmail(requestDTO.getEmail());
        return ResponseEntity.ok( ApiUtils.success(null) );
    }
    @GetMapping("/login")
    public String login() {
        return "login"; // "login.html" 파일을 렌더링
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.JoinDTO requestDTO, Error error) {
        String jwt = userService.login(requestDTO);
        return ResponseEntity.ok().header(JwtTokenProvider.HEADER, jwt)
                .body(ApiUtils.success(null));
    }


}