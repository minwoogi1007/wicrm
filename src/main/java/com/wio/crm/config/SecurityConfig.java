package com.wio.crm.config;

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
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import com.wio.crm.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    private final CustomSuccessHandler customSuccessHandler;
    private final LoginAttemptService loginAttemptService;
    
    // AJAX 요청을 판별하는 RequestMatcher
    private RequestMatcher ajaxRequestMatcher = request -> 
        "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 세션 요청 캐시 설정
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName("continue");
        
        http
                // CSRF 보호: CookieCsrfTokenRepository 사용 (XSRF-TOKEN 쿠키 기반)
                // CsrfTokenRequestAttributeHandler 사용으로 XOR 마스킹 비활성화 (Spring Security 6 호환)
                // 쿠키값을 그대로 X-XSRF-TOKEN 헤더로 전송하면 됨
                .csrf(csrf -> {
                        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                        csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/logout"),       // 로그아웃 (Spring Security 기본)
                                new AntPathRequestMatcher("/error"),        // 에러 페이지
                                new AntPathRequestMatcher("/api/log/**"),   // 사용자 액션 로깅 (빈번한 호출)
                                new AntPathRequestMatcher("/integrations/cafe24/oauth/callback") // 카페24 외부 redirect (CSRF 토큰 보유 불가)
                        )
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler);
                })
                // CSRF 쿠키가 모든 요청에서 반드시 설정되도록 필터 추가
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/.well-known/**").permitAll() // Chrome DevTools 요청 허용
                        .requestMatchers("/empl").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/encrypt-passwords","/encrypt-password", "/chat","/encryption","/check-userid-availability","/apply-userid").permitAll()
                        .requestMatchers("/download/**", "/upload","/board/uploadImage").permitAll()
                        .requestMatchers("/design-samples/**").permitAll() // 디자인 샘플 페이지는 로그인 없이 확인 가능
                        .requestMatchers("/project-plan/**").permitAll() // 프로젝트 모니터링 페이지 허용
                        .requestMatchers("/project-plan/monitor").permitAll() // 명시적 허용
                        .requestMatchers("/project-plan/api/**").permitAll() // API 경로 명시적 허용
                        .requestMatchers("/board/readBoard/comments").authenticated()  // 댓글 추가 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/board/create/saveBoard").authenticated()  // 게시글 저장 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/board/**").authenticated()  // 모든 게시판 관련 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/return/**").authenticated()  // 교환/반품 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/exchange/**").authenticated()  // 교환/반품 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/payment/**").authenticated()  // 입금 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/logistics/**").authenticated()  // 물류 관리 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/log/user-action").authenticated()  // 사용자 액션 로깅 API 접근 설정
                        .requestMatchers("/admin/banners/**").hasAuthority("ROLE_EMPLOYEE")  // 배너 관리 페이지는 내부 직원만 접근 가능
                        .requestMatchers("/integrations/cafe24/oauth/callback").permitAll()  // 카페24 콜백은 외부 redirect라 인증 강제 불가
                        .requestMatchers("/integrations/cafe24/**").authenticated()  // 카페24 연동 관리/승인 화면은 거래처 직원 (custCode 보유) 가드는 컨트롤러에서 수행
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // AJAX 요청에 대해서는 401 상태 코드 반환
                        .defaultAuthenticationEntryPointFor(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), 
                            ajaxRequestMatcher
                        )
                )
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .expiredUrl("/login?expired"))
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
                .headers(headers -> headers
                        .frameOptions().deny().contentTypeOptions().and()
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                                .preload(true))
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(), microphone=(), geolocation=()")))
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

    /**
     * CSRF 토큰을 매 요청마다 즉시 로딩하여 쿠키에 반드시 설정되도록 하는 필터.
     * Spring Security 6에서는 CSRF 토큰이 지연 로딩(deferred)되어,
     * 명시적으로 getToken()을 호출하지 않으면 쿠키가 설정되지 않을 수 있음.
     */
    private static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // getToken()을 호출하면 토큰이 즉시 로딩되고 쿠키에 설정됨
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
