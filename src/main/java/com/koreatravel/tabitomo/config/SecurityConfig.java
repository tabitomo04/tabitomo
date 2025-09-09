package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.config.security.MemberDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberDetailsService memberDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index*",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/error",
                    "/h2-console/**",
                    "/favicon.ico"
                ).permitAll()
                // Allow access to test endpoints
                .requestMatchers("/test/**").permitAll()
                // Explicitly allow access to auth endpoints
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
                    PathConstants.CHECK_NICKNAME + "/**",
                    
                    // Allow access to question pages without authentication
                    PathConstants.QUESTION_START,
                    PathConstants.QUESTION_START + "/**",
                    PathConstants.QUESTION_FORM,
                    PathConstants.QUESTION_FORM + "/**",
                    PathConstants.QUESTION_SUBMIT,
                    PathConstants.QUESTION_COMPLETE,
                    
                    // Allow access to member question pages without authentication
                    PathConstants.MEMBER_QUESTION,
                    PathConstants.MEMBER_QUESTION + "/**",
                    PathConstants.MEMBER_QUESTION_START,
                    PathConstants.MEMBER_QUESTION_START + "/**",
                    PathConstants.MEMBER_QUESTION_FORM,
                    PathConstants.MEMBER_QUESTION_FORM + "/**",
                    PathConstants.MEMBER_QUESTION_COMPLETE
                ).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(PathConstants.LOGIN)
                .loginProcessingUrl(PathConstants.LOGIN)
                .defaultSuccessUrl("/mypage", true)
                .usernameParameter("email")
                .permitAll()
            )
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
