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

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/favorites/**", "/api/**", "/trip/**", "/api/translate/**", "/upload", "/storybook/save", "/storybook/tempsave")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/auth/login?expired")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/auth/login?expired")
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                            "/", "/css/**", "/js/**", "/image/**", "/error", "/uploadedImages/**"
                        ).permitAll()
                        .requestMatchers(
                                "/trips/public", "/tripinformation",
                                "/tripinformation/places", "/about", "/contact", "/privacy", "/terms",
                                "/api/favorites/status", "/api/translate/**",
                                "/trip/**"
                        ).permitAll()
                        .requestMatchers(
                            "/auth/**", 
                            "/api/**", 
                            "/member/api/**"
                        ).permitAll()
                        .requestMatchers(
                            "/member/info/**",
                            "/member/saved-spots/**"
                        ).permitAll()
                        .requestMatchers(
                            "/storybook/list",
                            "/storybook/detail/**"
                        ).permitAll()
                        .requestMatchers("/upload").permitAll()
                        .requestMatchers(
                            "/mypage/**",
                            "/storybook/write",
                            "/storybook/editor/**",
                            "/storybook/save",
                            "/storybook/tempsave"
                        ).authenticated()
                        .requestMatchers(
                                "/chatbot/intro",
                                "/api/chat/**",
                                "/api/send"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
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
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (isAjaxRequest(request)) {
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                        } else {
                            response.sendRedirect("/auth/login?error=unauthorized");
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
