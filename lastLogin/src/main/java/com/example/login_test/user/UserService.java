package com.example.login_test.user;

import com.example.login_test.core.error.exception.Exception400;
import com.example.login_test.core.error.exception.Exception401;
import com.example.login_test.core.error.exception.Exception500;
import com.example.login_test.core.security.CustomUserDetails;
import com.example.login_test.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;


/* 메서드나 클래스에 적용가능.
 * Transactional
 * 어노테이션이 적용된 메서드가 호출되면, 새로운 트랜잭션이 시작됨.
 * 메서드 실행이 성공적으로 완료되면, 트랜잭션은 자동으로 커밋.
 * 메서드 실행 중에 예외가 발생하면, 트랜잭션은 자동으로 롤백.
 *
 ** readOnly = true : 이 설정은 해당 트랜잭션이 데이터를 변경하지 않고 읽기전용으로만 사용이 가능하다는것을 명시적으로 나타냄.
 * */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void join(UserRequest.JoinDTO requestDTO) {
        checkEmail(requestDTO.getEmail());
        //String encodedPassword = passwordEncoder.encode( requestDTO.getPassword());
        //requestDTO.setPassword(encodedPassword);
        try {
            userRepository.save(requestDTO.toEntity(passwordEncoder));
//          SignUpMessageSender.sendMessage("", requestDTO.getPhoneNumber()
//                ,"환영합니다. 회원가입이 완료되었습니다."); //회원가입 완료 문자 발송
        }catch (Exception e){
            throw new Exception500(e.getMessage());
        }
    }
    public String login(UserRequest.JoinDTO requestDTO) {
        // ** 인증 작업.
        try{
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getPassword());
            //사용자의 이메일과 패스워드를 포함한 인증 토큰을 생성합
            Authentication authentication =  authenticationManager.authenticate(
                    usernamePasswordAuthenticationToken
            );
            //사용자가 제공한 이메일과 비밀번호로 사용자를 인증하려고 하는것
            // ** 인증 완료 값을 받아온다.
            CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

            // ** 토큰 발급 - 이 JWT 토큰은 사용자 인증을 통해 확인된 사용자의 정보를 포함
            return JwtTokenProvider.create(customUserDetails.getUser());
        }catch (Exception e){
            // 401 반환.
            throw new Exception401("인증되지 않음.");
        }
    }


    public void findAll() {
        List<User> all = userRepository.findAll();
        for(User user : all){
            user.output();
        }
    }

    public void checkEmail(String email){
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmail(email);
        if(users.isPresent()) {
            throw new Exception400("이미 존재하는 이메일 입니다. : " + email);
        }
    }

    public UserRequest findByEmailAndProvider(String email, String provider){
        // 동일한 이메일이 있는지 확인.
        Optional<User> users = userRepository.findByEmailAndProvider(email, provider);
        if(users.isPresent()) {
            throw new Exception400("이미 존재하는 이메일 입니다. : " + email);
        }
        return null;
    }
}






