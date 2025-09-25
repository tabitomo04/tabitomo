package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;
import com.koreatravel.tabitomo.config.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        // CORS 설정에 CSRF 관련 헤더 추가
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "X-CSRF-TOKEN",
            "X-Requested-With",
            "Content-Type"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        // CSRF 보호 설정 (필요한 엔드포인트만 제외)
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/api/favorites/**",
                "/api/translate/**",
                "/upload",
                "/chat/**",
                "/h2-console/**",
                "/auth/reset-password",
                "/api/email/**",
                "/tripselect/**",
                "/trip/**",
                "/trip/step3",
                "/trip/step4",
                "/trip/save",
                "/js/**",
                "/images/**", 
                "/image/**",
                "/fonts/**", 
                "/favicon.ico",
                "/css/**",
                "/trips/public", 
                "/tripinformation",
                "/tripinformation/places"
            )
        );
        
        // 보안 헤더 설정
        http.headers(headers -> {
            headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
                .xssProtection(xss -> xss
                    .headerValue(HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://code.jquery.com https://unpkg.com https://npmcdn.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://unpkg.com https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css; " +
                        "style-src-elem 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://unpkg.com https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css; " +
                        "img-src 'self' data: https: *.tile.openstreetmap.org; " +
                        "font-src 'self' https: data:; " +
                        "connect-src 'self' http://localhost:8080 https://cdn.jsdelivr.net;"
                    )
                );
        });
        
        // 세션 관리 설정
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                  .sessionFixation().migrateSession()
                  .maximumSessions(1)
                  .maxSessionsPreventsLogin(false)
                  .expiredUrl("/auth/login?expired");
            
            // Set invalid session URL separately
            session.invalidSessionUrl("/auth/login?expired");
        });

        // 인가 설정 - 모든 사용자에게 허용할 경로
        http.authorizeHttpRequests(authorize -> {
            // 정적 리소스
            authorize.requestMatchers(
                "/", "/index", "/index.html", "/home",
                "/css/**", "/js/**", "/images/**", "/image/**", 
                "/fonts/**", "/favicon.ico", "/error", 
                "/uploadedImages/**", "/h2-console/**"
            ).permitAll()
            
            // 공개 API 및 페이지
            .requestMatchers(
                "/trips/public", "/tripinformation/**", "/tripselect/**",
                "/about", "/contact", "/privacy", "/terms",
                "/api/translate/**", "/trip/**", "/api/favorites/status",
                "/main", "/main/**", "/api/public/**", "/api/places/**"
            ).permitAll()
            
            // 인증 관련
            .requestMatchers(
                "/auth/**", "/login", "/signup", 
                "/api/auth/**", "/api/email/**"
            ).permitAll()
            
            // 멤버 관련
            .requestMatchers(
                "/member/api/**",
                "/member/info/**",
                "/member/saved-spots/**"
            ).permitAll()
            
            // 스토리북
            .requestMatchers(
                "/storybook/list",
                "/storybook/detail/**"
            ).permitAll()
            
            // 채팅
            .requestMatchers(
                "/chatbot/intro",
                "/chat/**",
                "/api/send"
            ).permitAll()
            
            // 파일 업로드
            .requestMatchers("/upload").permitAll()
            
            // 보호된 리소스
            .requestMatchers(
                "/mypage/**",
                "/storybook/write",
                "/storybook/editor/**",
                "/storybook/save",
                "/storybook/tempsave"
            ).authenticated()
            
            // 나머지 모든 요청은 인증이 필요
            .anyRequest().authenticated();
        });
        
        // 폼 로그인 설정
        http.formLogin(form -> form
            .loginPage("/auth/login")
            .loginProcessingUrl("/auth/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .defaultSuccessUrl("/", true)
            .successHandler(customAuthenticationSuccessHandler)
            .failureHandler((request, response, exception) -> {
                String errorMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";
                if (exception.getMessage() != null && exception.getMessage().contains("비활성화된 계정")) {
                    errorMessage = "비활성화된 계정입니다. 관리자에게 문의해주세요.";
                }
                response.sendRedirect("/auth/login?error=true&message=" + 
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
            })
            .permitAll()
        );
        
        // 로그아웃 설정
        http.logout(logout -> {
            logout.logoutUrl("/auth/logout")
                  .logoutSuccessUrl("/?logout")
                  .invalidateHttpSession(true)
                  .deleteCookies("JSESSIONID", "remember-me")
                  .clearAuthentication(true)
                  .addLogoutHandler((request, response, authentication) -> {
                      // 세션 무효화
                      HttpSession session = request.getSession(false);
                      if (session != null) {
                          session.invalidate();
                      }
                      // SecurityContext 지우기
                      SecurityContextHolder.clearContext();
                  });
        });
        
        // 자동 로그인 설정
        http.rememberMe(remember -> remember
            .key("uniqueAndSecret")
            .tokenValiditySeconds(1209600) // 2주
            .userDetailsService(userDetailsService)
            .rememberMeParameter("remember-me")
        );
        
        // 예외 처리
        http.exceptionHandling(exception -> {
            // 인증 실패 시 처리
            exception.authenticationEntryPoint((request, response, authException) -> {
                if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\"}");
                } else {
                    String message = "로그인이 필요한 서비스입니다. 로그인 후 이용해주세요.";
                    response.sendRedirect("/auth/login?error=unauthorized&message=" +
                            URLEncoder.encode(message, StandardCharsets.UTF_8));
                }
            });
            
            // 인가 실패 시 처리
            exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                } else {
                    response.sendRedirect("/auth/access-denied.html");
                }
            });
        });

        // UserDetailsService 설정
        http.userDetailsService(userDetailsService);

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
