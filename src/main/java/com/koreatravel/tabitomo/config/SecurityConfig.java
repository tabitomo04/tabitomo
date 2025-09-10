package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.config.security.MemberDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberDetailsService memberDetailsService;
    
    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
            )
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스에 대한 접근 허용
                // Public endpoints
                .requestMatchers(
                    "/",
                    "/index*",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/image/**",
                    "/webjars/**",
                    "/error",
                    "/h2-console/**",
                    "/favicon.ico"
                ).permitAll()
                // Test endpoints
                .requestMatchers("/test/**").permitAll()
                // Auth endpoints
                .requestMatchers(
                    PathConstants.SIGNUP,
                    PathConstants.SIGNUP + "/**",
                    PathConstants.LOGIN,
                    PathConstants.LOGIN + "/**",
                    PathConstants.FORGOT_PASSWORD,
                    PathConstants.FORGOT_PASSWORD + "/**",
                    PathConstants.VERIFY_EMAIL,
                    PathConstants.VERIFY_EMAIL + "/**",
                    PathConstants.RESET_PASSWORD,
                    PathConstants.RESET_PASSWORD + "/**",
                    PathConstants.CHECK_EMAIL,
                    PathConstants.CHECK_EMAIL + "/**",
                    PathConstants.CHECK_NICKNAME,
                    PathConstants.CHECK_NICKNAME + "/**"
                ).permitAll()
                // Question endpoints
                .requestMatchers(
                    PathConstants.QUESTION_START,
                    PathConstants.QUESTION_START + "/**",
                    PathConstants.QUESTION_FORM,
                    PathConstants.QUESTION_FORM + "/**",
                    PathConstants.QUESTION_SUBMIT,
                    PathConstants.QUESTION_COMPLETE,
                    PathConstants.MEMBER_QUESTION,
                    PathConstants.MEMBER_QUESTION + "/**",
                    PathConstants.MEMBER_QUESTION_START,
                    PathConstants.MEMBER_QUESTION_START + "/**",
                    PathConstants.MEMBER_QUESTION_FORM,
                    PathConstants.MEMBER_QUESTION_FORM + "/**",
                    PathConstants.MEMBER_QUESTION_COMPLETE
                ).permitAll()
                // Trip endpoints
                .requestMatchers(
                    "/trip",
                    "/trip/**",
                    "/api/trip",
                    "/api/trip/**"
                ).permitAll()
                // API endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/password/reset-request").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/password/reset").permitAll()
                .requestMatchers("/api/email/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(PathConstants.LOGIN)
                .loginProcessingUrl(PathConstants.LOGIN)
                .defaultSuccessUrl("/mypage", true)
                .usernameParameter("email")
                .successHandler((request, response, authentication) -> {
                    // Custom success handler for API login
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":\"success\", \"message\":\"Login successful\"}");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    // Custom failure handler for API login
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":\"error\", \"message\":\"" + exception.getMessage() + "\"}");
                    }
                })
                .permitAll()
            )
            // H2 콘솔을 위한 헤더 설정
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    } else {
                        response.sendRedirect(PathConstants.LOGIN);
                    }
                })
                .accessDeniedPage("/access-denied")
            )
            .logout(logout -> logout
                .logoutUrl(PathConstants.LOGOUT)
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .logoutSuccessHandler((request, response, authentication) -> {
                    // Custom success handler for API logout
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":\"success\", \"message\":\"Logout successful\"}");
                    } else {
                        response.sendRedirect("/?logout");
                    }
                })
                .permitAll()
            )
            .userDetailsService(memberDetailsService)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // H2 콘솔을 위한 설정
            );

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
