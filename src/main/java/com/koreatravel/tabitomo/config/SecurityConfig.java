package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;
import com.koreatravel.tabitomo.config.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        // CSRF 보호 설정 (API 및 h2-console 제외)
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/api/**",
                "/h2-console/**",
                "/auth/reset-password"
            )
        );
        
        // 보안 헤더 설정
        http.headers(headers -> {
            headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
            
            // XSS 보호 설정
            headers.xssProtection(xss -> { });
            
            // Content Security Policy (CSP) 설정 (Spring Security 6.1+)
            headers.addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://code.jquery.com; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                "font-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com data:; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' https://cdn.jsdelivr.net https://code.jquery.com wss:; " +
                "object-src 'none';"
            ));
        });
        
        // 세션 관리 설정
        http.sessionManagement(session -> {
            session.maximumSessions(1)
                   .maxSessionsPreventsLogin(false)
                   .expiredUrl("/auth/login?expired");
            
            // 세션 고정 보호 설정
            session.sessionFixation().newSession();
            
            // 세션 무효화 URL 설정
            session.invalidSessionUrl("/auth/login?expired");
            
            // 세션 생성 정책
            session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        });
        
        // 인증 및 인가 설정
        http.authorizeHttpRequests(authorize -> {
            // 정적 리소스 허용
            authorize.requestMatchers(
                "/",
                "/css/**",
                "/js/**",
                "/image/**",
                "/error",
                "/h2-console/**"
            ).permitAll();
            
            // 인증 없이 접근 가능한 URL
            authorize.requestMatchers(
                "/auth/**",
                "/api/public/**",
                "/api/email/**",
                "/api/auth/send-verification"
            ).permitAll();
            
            // 나머지 요청은 인증 필요
            authorize.anyRequest().authenticated();
        });
        
        // 폼 로그인 설정
        http.formLogin(form -> {
            form.loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll();
        });
        
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
                            java.net.URLEncoder.encode(message, "UTF-8"));
                }
            });
            
            // 인가 실패 시 처리
            exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                } else {
                    response.sendRedirect("/auth/access-denied");
                }
            });
        });
        
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