package com.wio.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    @Autowired
    private CustomSuccessHandler  customSuccessHandler;
    
    // AJAX 요청을 판별하는 RequestMatcher
    private RequestMatcher ajaxRequestMatcher = request -> 
        "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 세션 요청 캐시 설정
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName("continue");
        
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/logout"),  // "/logout"에 대한 CSRF 검증을 비활성화
                                new AntPathRequestMatcher("/api/**"),  // "/api/**" 경로에 대해 CSRF 보호를 비활성화
                                new AntPathRequestMatcher("/download/**"), // "/download/**" 경로에 대해 CSRF 보호를 비활성화
                                new AntPathRequestMatcher("/upload"), // 추가된 업로드 경로
                                new AntPathRequestMatcher("/board/update"), // 게시판 업데이트 경로 추가
                                new AntPathRequestMatcher("/board/uploadImage"),  // 이 줄을 추가
                                new AntPathRequestMatcher("/board/readBoard/comments"),  // 댓글 추가 경로
                                new AntPathRequestMatcher("/board/create/saveBoard"),  // 게시글 저장 경로 추가
                                new AntPathRequestMatcher("/consulting/**"),  // 상담 관련 모든 경로에 대해 CSRF 보호 비활성화
                                new AntPathRequestMatcher("/stat/**"), // 일일 운영 현황 통계 경로 CSRF 보호 비활성화
                                new AntPathRequestMatcher("/return/**"),  // 교환/반품 관리 경로 추가
                                new AntPathRequestMatcher("/exchange/**"),  // 교환/반품 관리 경로 추가
                                new AntPathRequestMatcher("/payment/**"),  // 입금 관리 경로 추가
                                new AntPathRequestMatcher("/admin/banners/**"),  // 배너 관리 경로 추가
                                new AntPathRequestMatcher("/project-plan/**"),  // 프로젝트 모니터링 경로 추가
                                new AntPathRequestMatcher("/error")  // 에러 페이지 CSRF 보호 비활성화
                        ).csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/.well-known/**").permitAll() // Chrome DevTools 요청 허용
                        .requestMatchers("/empl").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/encrypt-passwords","/encrypt-password", "/chat","/encryption","/check-userid-availability","/apply-userid").permitAll()
                        .requestMatchers("/download/**", "/upload","/board/uploadImage").permitAll()
                        .requestMatchers("/project-plan/**").permitAll() // 프로젝트 모니터링 페이지 허용
                        .requestMatchers("/project-plan/monitor").permitAll() // 명시적 허용
                        .requestMatchers("/project-plan/api/**").permitAll() // API 경로 명시적 허용
                        .requestMatchers("/board/readBoard/comments").authenticated()  // 댓글 추가 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/board/create/saveBoard").authenticated()  // 게시글 저장 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/board/**").authenticated()  // 모든 게시판 관련 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/return/**").authenticated()  // 교환/반품 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/exchange/**").authenticated()  // 교환/반품 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/payment/**").authenticated()  // 입금 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/log/user-action").authenticated()  // 사용자 액션 로깅 API 접근 설정
                        .requestMatchers("/admin/banners/**").hasAuthority("ROLE_EMPLOYEE")  // 배너 관리 페이지는 내부 직원만 접근 가능
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // AJAX 요청에 대해서는 401 상태 코드 반환
                        .defaultAuthenticationEntryPointFor(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), 
                            ajaxRequestMatcher
                        )
                )
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
                        .permitAll())
                .requestCache(cache -> cache
                        .requestCache(requestCache));

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
