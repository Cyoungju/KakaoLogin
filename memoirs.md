# 회고록

#### 23.11.02
HttpConnection => RestTemplet으로 쓰는 방식으로 소스 수정 <br>
objectmapper에서 오류 이슈 발생 - 해결완료 <br><Br>
로그아웃 기능 작업중 KakaoController/KakaoService 에 메소드 구현 작업 진행중<br>
@RequestMapping / @GetMapping 차이점 사용 방법 알아보기<br><br><br>


#### 23.11.03
RestTemplet => webclient 로 소스 코드 수정 완료 <br>
(RestTemplate은 Spring 5부터 deprecated되었으며, WebClient는 Non-blocking I/O를 지원하고 더욱 유연한 API를 제공힘.)<br>
로그아웃 기능 구현 완료<br><br>
리다이렉트 주소와 mapping 주소의 불일치로 500에러뜸 <br>
로그아웃 기능 이슈 해결 완료 <Br><Br><Br>


#### 23.11.06
카카오 로그인 기능 구현완료<br>
사용자 정보 가져오기 완료<br>
받아온 사용자 정보를 가지고 회원가입 진행중 <br><Br>
h2 데이터를 연결완료<br>
h2데이터 연동 작업을 완료 했으나 entity에서 생성한 table을 불러올 수 없음<br>
spring boot 띄운 콘솔에서 Database available 'jdbc:h2:mem:'<br>
해당 코드를 잘 확인해서 JDBC URL에 주소 수정 -> table확인 성공!<br><br>
카카오 사용자 정보 가져와서 홈페이지 가입과 연동 하기 작업 진행할 예정
