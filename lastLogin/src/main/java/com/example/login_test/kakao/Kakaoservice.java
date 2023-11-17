package com.example.login_test.kakao;


import com.example.login_test.core.security.JwtTokenProvider;
import com.example.login_test.user.User;
import com.example.login_test.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class Kakaoservice {
    private final UserRepository userRepository;
    private final KakaoUri kakaoUri;
    private final KakaoResponse.KakaoToken kakaoToken;


    //카카오 통신을 위해 RestTemplate 스프링부트 Bean으로 등록
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    //로그인 핸들러
    public void handleLoginCallback(HttpSession session, String code) {
        // 이미 로그인한 사용자의 경우 예외 처리
        if (session.getAttribute("accessToken") != null) {
            log.info("이미 로그인된 사용자입니다.");
            throw new RuntimeException("이미 로그인된 사용자입니다.");
        }

        String jwt = joinOrLogin(code);
        log.info("JWT : {}", jwt);

        String accessToken = kakaoToken.getAccess_token();
        LocalDateTime expiresAt = calculateTokenExpiry(kakaoToken.getExpires_in());
        log.info(accessToken);

        // 토큰 및 만료 시간을 세션에 저장
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("tokenExpiry", expiresAt);
    }

    //로그아웃 핸들러
    public void handleLogout(HttpSession session) {
        // 이미 로그아웃한 사용자의 경우 예외 처리
        if (session.getAttribute("accessToken") == null) {
            log.info("이미 로그아웃한 사용자입니다.");
            throw new RuntimeException("이미 로그아웃한 사용자입니다.");
        }

        // 세션에서 토큰 가져오기
        String accessToken = (String) session.getAttribute("accessToken");
        log.info(accessToken);

        if (accessToken != null) {
            // 토큰 만료 확인
            if (isTokenExpired(session)) {
                log.info("토큰 이미 만료됨");
            } else {
                kakaoLogOut(accessToken);
                log.info("로그아웃 완료");
            }

            // 세션 속성 지우기
            session.removeAttribute("accessToken");
            session.removeAttribute("tokenExpiry");
        }
    }

    // 토큰 만료 시간을 계산하는 메서드
    private LocalDateTime calculateTokenExpiry(Integer expiresIn) {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 만료 시간을 초로 변환하여 현재 시간에 더하기
        LocalDateTime expiryTime = now.plusSeconds(expiresIn);

        return expiryTime;
    }


    // 토큰이 만료되었는지 확인하는 메서드
    private boolean isTokenExpired(HttpSession session) {
        LocalDateTime tokenExpiry = (LocalDateTime) session.getAttribute("tokenExpiry");
        return LocalDateTime.now().isAfter(tokenExpiry);
    }


    //카카오 로그아웃 - 해당 소스코드 그냥 로그아웃이랑 합쳐야함
    public void kakaoLogOut(String accessToken) {
        try {
            // 카카오 로그아웃을 위해 액세스 토큰을 사용하여 요청
            HttpHeaders headers = createHeaders(accessToken);
            headers.set("Authorization", "Bearer " + accessToken);

            String requestUrl = "https://kapi.kakao.com/v1/user/logout";

            HttpEntity<KakaoResponse> entity = new HttpEntity<>(headers);
            ResponseEntity<KakaoResponse> responseEntity = restTemplate().exchange(requestUrl, HttpMethod.POST, entity, KakaoResponse.class);

            // 로그아웃이 성공하면 응답을 반환
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // 여기서 해당 토큰을 만료시키는 작업을 수행
                JwtTokenProvider.revoke(accessToken);
            } else {
                handleException("카카오 로그아웃 실패", new RuntimeException("HTTP 상태 코드: " + responseEntity.getStatusCodeValue()));
            }
        } catch (Exception e) {
            handleException("카카오 로그아웃 중 오류 발생", e);
        }
    }


    //회원가입 및 로그인
    public String joinOrLogin(String code) {
        try {
            KakaoResponse.KakaoToken token = getKakaoTokenInfo(code);
            String accessToken = token.getAccess_token();
            String refreshToken = token.getRefresh_token();
            Integer expiresIn = token.getExpires_in();

            kakaoToken.setAccess_token(accessToken);
            kakaoToken.setRefresh_token(refreshToken);
            kakaoToken.setExpires_in(expiresIn);

            log.info("accessToken : {}", accessToken);
            log.info("refreshToken : {}", refreshToken);
            log.info("expiresIn : {}", expiresIn);

            KakaoResponse kakaoResponse = getKakaoProfile(accessToken, refreshToken);
            KakaoResponse.KakaoAccount account = kakaoResponse.getKakao_account();

            log.info("kakaoResponse : {}", kakaoResponse);
            log.info("account : {}", account);


            User existingUser = userRepository.findByEmailAndProvider(account.getEmail(), "kakao").orElse(null);
            if (existingUser != null) {
                // User already exists, perform login
                return JwtTokenProvider.create(existingUser);
            }

            User user = userRepository.save(User.builder()
                    .email(account.getEmail())
                    .username(account.getName())
                    .phoneNumber(account.getPhone_number())
                    .provider("kakao")
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build());

            return JwtTokenProvider.create(user);
        }catch (Exception e){
            handleException("로그인 중 오류 발생", new RuntimeException("다시 시도해주세요", e));
            return null;
        }

    }

    //토큰 정보
    public KakaoResponse.KakaoToken getKakaoTokenInfo(String code) {
        try {
            String requestUrl = "https://kauth.kakao.com/oauth/token";
            HttpHeaders headers = createHeaders(null);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoUri.getAPI_KEY());
            params.add("redirect_uri", kakaoUri.getREDIRECT_URI());
            params.add("code", code);


            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            //postForEntity 메소드 사용 카카오 API의 토큰 요청 엔드포인트에 POST 요정 보냄
            ResponseEntity<KakaoResponse.KakaoToken> responseEntity = restTemplate().postForEntity(requestUrl, request, KakaoResponse.KakaoToken.class);
            //ResponseEntity<KakaoResponse.KakaoToken> 형태로 받아옴

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                //KakaoResponse.KakaoToken 객체로 매핑하여 반환 - > 자바 객체로 변환하여 반환
                // :: 객체로 매핑하면 응답 데이터의 각 필드를 객체의 속성으로 접근할 수 있음
                return responseEntity.getBody();
            } else {
                return handleResponse(responseEntity, "Kakao 토큰 요청 실패");
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            handleException("Kakao 토큰 요청이 권한 없음으로 실패했습니다.", e);
            return null;
        } catch (Exception e) {
            handleException("Kakao 액세스 토큰을 가져오는 중에 오류가 발생했습니다", e);
            return null;
        }
    }

    //사용자 정보
    public KakaoResponse getKakaoProfile(String KakaoAccessToken, String KakaoRefreshToken) {
        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = createHeaders(KakaoAccessToken);

        HttpEntity<KakaoResponse> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoResponse> responseEntity;
        try {
            responseEntity = restTemplate().exchange(requestUrl, HttpMethod.GET, entity, KakaoResponse.class);
            KakaoResponse kakaoResponse = responseEntity.getBody();
            return kakaoResponse;

        } catch (HttpClientErrorException.Unauthorized e) {
            // 액세스 토큰이 만료되었을 경우
            KakaoAccessToken = refreshAccessToken(KakaoRefreshToken);
            headers.set("Authorization", "Bearer " + KakaoAccessToken);
            entity = new HttpEntity<>(headers);
            responseEntity = restTemplate().exchange(requestUrl, HttpMethod.GET, entity, KakaoResponse.class);

            KakaoResponse kakaoResponse = responseEntity.getBody();
            return kakaoResponse;

        }catch (Exception e) {
            handleException("Kakao 프로필 조회 중 오류 발생", e);
            return null;
        }

    }

    //카카오 엑세스 토큰 갱신
    private String refreshAccessToken(String refreshToken) {
        try {
            String url = "https://kauth.kakao.com/oauth/token";

            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("client_id", kakaoUri.getAPI_KEY());
            params.put("refresh_token", refreshToken);

            ResponseEntity<Map> responseEntity = restTemplate().postForEntity(url, params, Map.class);

            if(responseEntity.getStatusCode() == HttpStatus.OK){
                return responseEntity.getBody().get("access_token").toString();
            }else{
                handleException("카카오 토큰 갱신 실패", new RuntimeException("HTTP 상태 코드: " + responseEntity.getStatusCodeValue()));
                return null;
            }

        }catch (Exception e){
            handleException("카카오 토큰 갱신 중 오류 발생", e);
            return null;
        }

    }



    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (accessToken != null) {
            headers.set("Authorization", "Bearer " + accessToken);
        }
        return headers;
    }

    private <T> T handleResponse(ResponseEntity<T> responseEntity, String errorMessage) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            log.error("{} 응답: {}", errorMessage, responseEntity.getBody());
            throw new RuntimeException(errorMessage + ". HTTP 상태 코드: " + responseEntity.getStatusCodeValue());
        }
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }
}
