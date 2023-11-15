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
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class Kakaoservice {

    private final UserRepository userRepository;
    private final KakaoUri kakaoUri;

    //카카오 통신을 위해 RestTemplate 스프링부트 Bean으로 등록
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    public String joinOrLogin(String code) {
        try {
            KakaoResponse.KakaoToken token = getKakaoTokenInfo(code);
            String accessToken = token.getAccess_token();
            String refreshToken = token.getRefresh_token();

            KakaoResponse kakaoResponse = getKakaoProfile(accessToken, refreshToken);
            KakaoResponse.KakaoAccount account = kakaoResponse.getKakao_account();
            //KakaoResponse.KakaoToken kakaoToken = kakaoResponse.getKakao_Token();
            //KakaoResponse.Properties properties = kakaoResponse.getProperties();

            log.info("kakaoResponse : {}", kakaoResponse);
            log.info("account : {}", account);
            //log.info("kakaoToken :{}", kakaoToken);

            // Check if a user with the provided email and provider already exists
            User existingUser = userRepository.findByEmailAndProvider(account.getEmail(), "kakao").orElse(null);
            if (existingUser != null) {
                // User already exists, perform login
                return JwtTokenProvider.create(existingUser);
            }
            // User does not exist, proceed with registration
            User user = userRepository.save(User.builder()
                    .email(account.getEmail())
                    .username(account.getName())
                    .phoneNumber(account.getPhone_number())
                    .provider("kakao")
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build());

            return JwtTokenProvider.create(user);
        }catch (Exception e){
            log.error("로그인 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException("로그인에 실패 했습니다. 다시 시도해주세요, e");
        }

    }

    public KakaoResponse.KakaoToken getKakaoTokenInfo(String code) {
        try {
            String requestUrl = "https://kauth.kakao.com/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoUri.getAPI_KEY());
            params.add("redirect_uri", kakaoUri.getREDIRECT_URI());
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<KakaoResponse.KakaoToken> responseEntity = restTemplate().postForEntity(requestUrl, request, KakaoResponse.KakaoToken.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                return responseEntity.getBody();
            } else {
                log.error("Kakao 토큰 요청 실패. 응답: {}", responseEntity.getBody());
                throw new RuntimeException("Kakao 액세스 토큰을 가져오지 못했습니다. HTTP 상태 코드: " + responseEntity.getStatusCodeValue());
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Kakao 토큰 요청이 권한 없음으로 실패했습니다. 응답: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Kakao 액세스 토큰을 가져오지 못했습니다. 권한이 없습니다.", e);
        } catch (Exception e) {
            log.error("Kakao 액세스 토큰을 가져오는 중에 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("Kakao 액세스 토큰을 가져오지 못했습니다", e);
        }
    }

    public KakaoResponse getKakaoProfile(String KakaoAccessToken, String KakaoRefreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + KakaoAccessToken);

        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        HttpEntity<KakaoResponse> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoResponse> responseEntity;
        try {
            responseEntity = restTemplate().exchange(requestUrl, HttpMethod.GET, entity, KakaoResponse.class);
            KakaoResponse kakaoResponse = responseEntity.getBody();

//            if (kakaoResponse != null && kakaoResponse.getKakao_Token() == null) {
//
//                KakaoResponse.KakaoToken kakaoToken = new KakaoResponse.KakaoToken();
//                kakaoToken.setToken_type(KakaoAccessToken);
//                kakaoToken.setAccess_token(KakaoAccessToken);
//                kakaoToken.setRefresh_token(KakaoRefreshToken);
//                kakaoResponse.setKaksao_Token(kakaoToken);
//            }
            return kakaoResponse;

        } catch (HttpClientErrorException.Unauthorized e) {
            // 액세스 토큰이 만료되었을 경우
            KakaoAccessToken = refreshAccessToken(KakaoRefreshToken);
            headers.set("Authorization", "Bearer " + KakaoAccessToken);
            entity = new HttpEntity<>(headers);
            responseEntity = restTemplate().exchange(requestUrl, HttpMethod.GET, entity, KakaoResponse.class);


            KakaoResponse kakaoResponse = responseEntity.getBody();
//            if (kakaoResponse != null && kakaoResponse.getKakao_Token() == null) {
//
//                KakaoResponse.KakaoToken kakaoToken = new KakaoResponse.KakaoToken();
//                kakaoToken.setToken_type(KakaoAccessToken);
//                kakaoToken.setAccess_token(KakaoAccessToken);
//                kakaoToken.setRefresh_token(KakaoRefreshToken);
//                kakaoResponse.setKakao_Token(kakaoToken);
//            }
            return kakaoResponse;
        }

    }

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
                log.error("카카오 토큰 갱신 실패. 응답: {}", responseEntity.getBody());
                throw new RuntimeException("카카오 토큰을 갱신하는 데 실패 했습니다");
            }

        }catch (Exception e){
            log.error("카카오 토큰 갱신 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("카카오 토큰을 갱신하는 데 실패 했습니다. 다시 시도해 주세요.", e);
        }

    }
}
