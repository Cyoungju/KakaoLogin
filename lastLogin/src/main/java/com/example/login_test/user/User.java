package com.example.login_test.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity

@Table(name="usertb")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    /* @Column 어노테이션은 JPA(Java Persistence API)에서 사용됨
     * Entity 클래스의 필드가 매핑될 데이터베이스 컬럼의 세부 사항을 지정한다.
     * length = 100은 데이터베이스에서 이 컬럼의 최대 길이를 100으로 설정.
     * nullable = false: 이 컬럼은 NULL 값을 허용하지 않음.
     * unique = true: 이 컬럼의 값은 유일해야 함. (중복된 값이 들어갈 수 없음.)
     **/
    @Column(length = 100, nullable = false, unique = true)
    private String email;


    @Column(length = 256)
    private String password;

    @Column(length = 45, nullable = false)
    private String username;

    @Column(length = 100)
    private String provider;

    @Column(length = 16)
    private String phoneNumber;

    @Column(length = 30)
    @Convert(converter = StringArrayConverter.class)
    private List<String> roles = new ArrayList<>();
    // ** 사용자 권한을 저장한다.
    // ** ROLE_ADMIN
    // ** ROLE_MANAGER
    // ** ROLE_USER 등등...




    /* @Builder 어노테이션은 Lombok 라이브러리에서 제공.
     * 빌더 패턴을 쉽게 구현할 수 있게 도와준다.
     * 주로 생성자의 인자가 많거나, 인자를 선택적으로 지정해야하는 경우에 사용.
     * */
    @Builder
    public User(int id, String email, String password, String username, String provider, String phoneNumber, List<String> roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.provider = provider;
        this.phoneNumber = phoneNumber;
        this.roles = roles; //회원 권한
    }


    public void output(){
        System.out.println(id);
        System.out.println(email);
        System.out.println(password);
        System.out.println(username);
        System.out.println(roles);
    }
}
