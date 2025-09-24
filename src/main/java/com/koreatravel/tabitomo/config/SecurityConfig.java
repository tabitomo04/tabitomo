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

        // CORS 설정
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
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                    "/api/**", 
                                                    "/h2-console/**",
                                                    "/auth/reset-password"
                                                ))
                                .headers(headers -> {
                                        // X-Frame-Options 설정 (동일 출처에서만 iframe 허용)
                                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin());

                                        // XSS 보호 설정 (Spring Security 6.x 스타일)
                                        headers.xssProtection(xss -> { });
                                })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false)
                                                .expiredUrl("/auth/login?expired"))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/error",
                                                                "/auth/reset-password"
                                                )
                                                .permitAll()
                                                .requestMatchers(
                                                                "/auth/**",
                                                                "/h2-console/**",
                                                                "/api/public/**",
                                                                "/api/email/**",
                                                                "/api/auth/send-verification"
                                                )
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login")
                                                .usernameParameter("email")
                                                .defaultSuccessUrl("/", true)
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/?logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "remember-me")
                                                .addLogoutHandler((request, response, authentication) -> {
                                                        // 세션 무효화
                                                        HttpSession session = request.getSession(false);
                                                        if (session != null) {
                                                                session.invalidate();
                                                        }
                                                        // SecurityContext 지우기
                                                        SecurityContextHolder.clearContext();
                                                }))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                    String requestURI = request.getRequestURI();
                                                    if (requestURI.startsWith("/api/") || isAjaxRequest(request)) {
                                                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\"}");
                                                    } else {
                                                        response.sendRedirect("/auth/login?error=unauthorized");
                                                    }
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                    String requestURI = request.getRequestURI();
                                                    if (requestURI.startsWith("/api/") || isAjaxRequest(request)) {
                                                        response.setStatus(HttpStatus.FORBIDDEN.value());
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                                                    } else {
                                                        response.sendRedirect("/auth/access-denied");
                                                    }
                                                }))
                                .userDetailsService(userDetailsService);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }
}
