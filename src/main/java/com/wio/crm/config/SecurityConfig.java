package com.wio.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    @Autowired
    private CustomSuccessHandler  customSuccessHandler ;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/logout"),  // "/logout"에 대한 CSRF 검증을 비활성화
                                new AntPathRequestMatcher("/api/**"),  // "/api/**" 경로에 대해 CSRF 보호를 비활성화
                                new AntPathRequestMatcher("/download/**"), // "/download/**" 경로에 대해 CSRF 보호를 비활성화
                                new AntPathRequestMatcher("/upload") // 추가된 업로드 경로
                        ).csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/consultations","/board/create/saveBoard").hasAuthority("ROLE_USER")
                        .requestMatchers("/empl").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/encrypt-passwords","/encrypt-password", "/encryption","/check-userid-availability","/apply-userid").permitAll()
                        .requestMatchers("/download/**", "/upload").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("userId")
                        .failureHandler(ajaxAuthenticationFailureHandler)
                        .successHandler(customSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll());

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
