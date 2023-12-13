//package com.example.login_test.kakao;
//
//import com.example.login_test.core.error.exception.Exception401;
//import com.example.login_test.core.security.JwtTokenProvider;
//import com.example.login_test.user.User;
//import com.example.login_test.user.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mno;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Collections;
//import java.util.Map;
//
//@Slf4j
//@Service
//public class KakaoService_back {
//
////    @Bean
////    public RestTemplate restTemplate(){
////        return new RestTemplate();
////    }
//
//    private final WebClient webClient;
//    private final KakaoUri kakaoUri;
//    private final UserRepository userRepository;
//    private final KakaoResponse kakaoResponse;
//
//    public KakaoService_back(WebClient.Builder webClientBuilder, KakaoUri kakaoUri, UserRepository userRepository, KakaoResponse kakaoResponse) {
//        this.webClient = webClientBuilder.baseUrl("https://kauth.kakao.com").build();
//        this.kakaoUri = kakaoUri;
//        this.userRepository = userRepository;
//        this.kakaoResponse = kakaoResponse;
//    }
//
//
//    @Transactional
//    public String kakaoLogin() {
//        try {
//
//            User user = userRepository.findByEmailAndProvider(kakaoResponse.getEmail(), kakaoResponse.getProvider())
//                    .orElseGet(() -> userRepository.save(User.builder()
//                            .email(kakaoResponse.getEmail())
//                            .username(kakaoResponse.getNickname())
//                            .provider("kakao")
//                            .roles(Collections.singletonList("ROLE_USER"))
//                            .build()));
//            //회원가입하고 저장
//            return JwtTokenProvider.create(user);
//
//        }catch (Exception e){
//            // 401 반환.
//            throw new Exception401("인증되지 않음.");
//        }
//    }
//
//    //인증코드로 token요청하기
//    public KakaoToken requestToken(String code) {
//        try {
//            String requestUrl = "/oauth/token"; //request를 보낼 주소
//
//
//            //body요청을 위해 MultiValueMap 객체생성
//            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//            //파라미터 추가
//            requestBody.add("grant_type", "authorization_code");
//            requestBody.add("client_id", kakaoUri.getAPI_KEY());
//            requestBody.add("redirect_uri", kakaoUri.getREDIRECT_URI());
//            requestBody.add("code", code);
//            requestBody.add("client_secret", kakaoUri.getSECRET_KEY());
//
//            KakaoToken kakaoToken = webClient.post()
//                    .uri(requestUrl)
//                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                    .body(BodyInserters.fromFormData(requestBody))
//                    .retrieve()
//                    .bodyToMono(KakaoToken.class)
//                    .block();
//
//            if(kakaoToken != null){
//                String accessToken = kakaoToken.getAccess_token();
//                String refreshToken = kakaoToken.getRefresh_token();
//
//                kakaoToken.setAccess_token(accessToken);
//                kakaoToken.setRefresh_token(refreshToken);
//                kakaoToken.setCode(code);
//
//                log.info("access_token = {}", accessToken);
//                log.info("refresh_token = {}", refreshToken);
//
//                log.info("카카오토큰 생성 완료 >>> {}", kakaoToken);
//            }
//                return kakaoToken; //Step2 -> 토큰 발급 완료
//
//        }catch (HttpClientErrorException ex){
//            HttpStatus statusCode = ex.getStatusCode();
//            if (statusCode != null) {
//                if (statusCode == HttpStatus.UNAUTHORIZED) {
//                    // 인증 오류 처리
//                    return null; // 또는 예외를 throw하여 클라이언트에게 전달
//                } else if (statusCode == HttpStatus.BAD_REQUEST) {
//                    // 요청이 잘못된 경우 처리
//                    return null; // 또는 예외를 throw하여 클라이언트에게 전달
//                }
//                // 다른 오류 상태 코드에 대한 처리
//            }
//            // 오류 응답에 대한 기타 처리
//            return null; // 또는 예외를 throw하여 클라이언트에게 전달
//        }
//    }
//
//
//    public Mono<KakaoResponse> requestUser(String accessToken, String refreshToken) {
//        WebClient client = WebClient.create();
//        String userUrl = "https://kapi.kakao.com/v2/user/me";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        return client.method(HttpMethod.GET)
//                .uri(userUrl)
//                .headers(h -> h.addAll(headers))
//                .retrieve()
//                .bodyToMono(KakaoResponse.class)
//                .onErrorResume(error -> {
//                    if (error instanceof WebClientResponseException.Unauthorized) {
//                        return refreshAccessToken(refreshToken)
//                                .flatMap(newAccessToken -> {
//                                    headers.setBearerAuth(newAccessToken);
//                                    return client.method(HttpMethod.GET)
//                                            .uri(userUrl)
//                                            .headers(h -> h.addAll(headers))
//                                            .retrieve()
//                                            .bodyToMono(KakaoResponse.class);
//                                });
//                    } else {
//                        return Mono.error(error);
//                    }
//                });
//    }
//
//
//    public Mono<String> refreshAccessToken(String refreshToken) {
//        WebClient client = WebClient.create();
//        String tokenUrl = "/oauth/token";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//        requestBody.add("grant_type", "refresh_token");
//        requestBody.add("client_id", kakaoUri.getAPI_KEY());
//        requestBody.add("refresh_token", refreshToken);
//
//        return client.method(HttpMethod.POST)
//                .uri(tokenUrl)
//                .headers(h -> h.addAll(headers))
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .map(responseBody -> responseBody.get("access_token").toString());
//    }
//    public void kakaoLogout(String accessToken) {
//
//        log.info("logout 시작");
//
//        String reqURL = "https://kapi.kakao.com/v1/user/logout";
//        try {
//            URL url = new URL(reqURL);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
//
//            int responseCode = conn.getResponseCode();
//            System.out.println("responseCode : " + responseCode);
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//
//            String result = "";
//            String line = "";
//
//            while ((line = br.readLine()) != null) {
//                result += line;
//            }
//            System.out.println(result);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//}