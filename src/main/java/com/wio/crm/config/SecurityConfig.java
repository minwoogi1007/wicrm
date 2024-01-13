package com.wio.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Autowired
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        //        .csrf(csrf -> csrf
        //                .disable()
        //        )   //초기 비밀번호 암호화를 위해 만들어 놓은 부분
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/general/**").hasAuthority("ROLE_GENERAL")
                        .requestMatchers("/company/**").hasAuthority("ROLE_COMPANY")
                        .requestMatchers("/employee/**").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/superuser/**").hasAuthority("ROLE_SUPERUSER")
                        .requestMatchers("/encrypt-passwords", "/encryption").permitAll() // 여기에 /encryption 추가
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("userId")  // 'userId'로 사용자 이름 파라미터 설정
                        .failureHandler(ajaxAuthenticationFailureHandler)  // 여기에 핸들러 추가
                        .permitAll())


                .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/assets/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
