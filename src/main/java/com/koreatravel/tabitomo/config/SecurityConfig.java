package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.config.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    // AJAX 요청인지 확인하는 헬퍼 메서드
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화 (여행 추천 및 API 요청에 대해서는 CSRF 보호 비활성화)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/favorites/**", "/api/**", "/trip/**", "/api/translate/**", "/upload", "/storybook/save", "/storybook/tempsave")
                )
                // 세션 정책 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/auth/login?expired")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/auth/login?expired")
                )
                // X-Frame-Options 설정 (iframe 내에서의 로딩을 허용)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                .sameOrigin()
                        )
                )
                // Form Login 설정
                .formLogin(form -> form
                    .loginPage("/auth/login")
                    .loginProcessingUrl("/auth/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .successHandler(customAuthenticationSuccessHandler)
                    .failureHandler((request, response, exception) -> {
                        String errorMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";
                        if (exception.getMessage() != null && exception.getMessage().contains("비활성화된 계정")) {
                            errorMessage = "비활성화된 계정입니다. 관리자에게 문의해주세요.";
                        }
                        response.sendRedirect("/auth/login?error=true&message=" + 
                            java.net.URLEncoder.encode(errorMessage, "UTF-8"));
                    })
                    .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> {
                    logout.logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll();
                    // 로그아웃 성공 핸들러 추가
                    logout.logoutSuccessHandler((request, response, authentication) -> {
                        // 세션 무효화
                        request.getSession().invalidate();
                        // 홈페이지로 리다이렉트
                        response.sendRedirect("/");
                    });
                })

                // 예외 처리
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (isAjaxRequest(request)) {
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                        } else {
                            String message = "로그인이 필요한 서비스입니다. 로그인 후 이용해주세요.";
                            response.sendRedirect("/auth/login?error=unauthorized&message=" + 
                                java.net.URLEncoder.encode(message, "UTF-8"));
                        }
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        if (isAjaxRequest(request)) {
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                        } else {
                            response.sendRedirect("/auth/access-denied");
                        }
                    })
                )
                // 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 허용
                        .requestMatchers(
                            "/", 
                            "/css/**", 
                            "/js/**", 
                            "/image/**", 
                            "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/trips/public", "/tripinformation",
                                "/tripinformation/places", "/about", "/contact", "/privacy", "/terms",
                                "/api/favorites/status", "/api/translate/**",
                                "/trip/**" // 여행 추천 관련 경로는 모두 허용
                        ).permitAll()


                        // API 및 인증 관련 경로 허용
                        .requestMatchers(
                            "/auth/**", 
                            "/api/**", 
                            "/member/api/**"
                        ).permitAll()
                        // 공개적으로 접근 가능한 페이지들
                        .requestMatchers(
                            "/member/info/**",       // 다른 사용자 정보 페이지
                            "/member/saved-spots/**"  // 저장한 여행지 리스트
                        ).permitAll()
                        // 스토리북 관련 경로 설정
                        .requestMatchers(
                            "/storybook/list",        // 스토리북 목록
                            "/storybook/detail/**"    // 스토리북 상세 보기
                        ).permitAll()
                        // CKEditor 업로드 허용
                        .requestMatchers("/upload").permitAll()
                        // 인증이 필요한 경로
                        .requestMatchers(
                            "/mypage/**",            // 마이페이지
                            "/storybook/write",       // 스토리북 작성
                            "/storybook/editor/**",    // 스토리북 에디터
                            "/storybook/save",
                            "/storybook/tempsave",
                            "/uploadedImages/**"       // 이미지 가져오기
                        ).authenticated()

                        // 챗봇 관련 경로를 permitAll()로 설정**
                        .requestMatchers(
                                "/chatbot/intro",   // 챗봇 소개 페이지 경로
                                "/api/chat/**",  // 챗봇 관련 API 경로 (실제 사용하는 API 경로로 수정)
                                "/api/send"        // 예시로 챗봇 메시지 전송 API도 추가
                        ).permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 인증 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 로그인 페이지로 리다이렉트
                            response.sendRedirect("/auth/login?error=unauthorized");
                        })
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
                            response.sendRedirect("/auth/login?error=true");
                        })
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // 사용자 세션 관리
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
