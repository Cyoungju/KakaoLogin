# 회고록

#### 23.11.02
HttpConnection => RestTemplet으로 쓰는 방식으로 소스 수정 <br>
objectmapper에서 오류 이슈 발생 - 해결완료 <br><Br>
로그아웃 기능 작업중 KakaoController/KakaoService 에 메소드 구현 작업 진행중<br>
@RequestMapping / @GetMapping 차이점 사용 방법 알아보기
<br><br><br>


#### 23.11.03
RestTemplet => webclient 로 소스 코드 수정 완료 <br>
(RestTemplate은 Spring 5부터 deprecated되었으며, WebClient는 Non-blocking I/O를 지원하고 더욱 유연한 API를 제공함.)<br>
로그아웃 기능 구현 완료<br><br>
리다이렉트 주소와 mapping 주소의 불일치로 500에러뜸 <br>
로그아웃 기능 이슈 해결 완료
<Br><Br><Br>


#### 23.11.06
카카오 로그인 기능 구현완료<br>
사용자 정보 가져오기 완료<br>
받아온 사용자 정보를 가지고 회원가입 진행중 <br><Br>
h2 데이터를 연결완료<br>
h2데이터 연동 작업을 완료 했으나 entity에서 생성한 table을 불러올 수 없음<br>
spring boot 띄운 콘솔에서 Database available 'jdbc:h2:mem:'<br>
해당 코드를 잘 확인해서 JDBC URL에 주소 수정 -> table확인 성공!<br><br>
카카오 사용자 정보 가져와서 홈페이지 가입과 연동 하기 작업 진행할 예정
<br><br><br>


#### 23.11.07
카카오 로그인후 받아온 사용자 정보를 가지고 홈페이지 회원가입 완료<br>

entity에서 provider 추가<br>
소셜 로그인 시 패스워드 불필요 - null허용<br>
service에서 소셜 로그인 함수 생성 <br><br><br>
기존의 join과는 다르기때문에 새로 생성해줌

카카오 로그인 - 코드 발급 - 발급 받은 코드로 assessToken 발급 - 발급 받은 토큰으로 사용자 정보 요청 - 요청한 정보 kakaoResponse에 넣어줌 - UserRequest.JoinDTO에 정보 넣기 (소셜 회원가입 완료) - h2에서 확인완료<br><br>

받아온 정보를 JoinDTO에 setter 해준뒤 소셜 로그인 함수 실행<br>
테이블에 넣어주기, h2 database 확인 완료
<br><br><br>

#### 23.11.10
refresh 토큰을 사용할수 있게 작업진행<br>
webclient 사용해보고 있었으나.. 코드 오류 잡지 못하고 restTemplate 빈으로 등록후 사용
<br><br><br>

#### 23.11.15
로그아웃 구현 실패.. 왜 안되는거임??
<br><br><br>

#### 23.11.16
session에 로그인시 저장된 access토큰 받아와서 로그아웃때 토큰 만료 완료<br>
-> access토큰 저장이 안되길래 왜 안불러와지나 했더니 저장된 KakaoToken에 객체 생성을 새롭게 계속 해서 안되는거였음..<br>
-> final 필드 값으로 지정후 사용 <br>
session에 저장하지 않고 jwt토큰을 지워서 로그아웃 기능실행하게 할수 없을까?<br>
-> 내일 작업해보기~ <br>
로그인 되어있을때 예외처리 하기! <br>
코드 리펙토링 작업 진행
<br><br><br>

#### 23.11.17
코드리팩토링 진행<br>
session에 저장함<br>
에러페이지 작업
