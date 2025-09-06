package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.config.security.MemberDetailsService;
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
                    PathConstants.LOGIN,
                    PathConstants.SIGNUP,
                    "/css/**",
                    "/js/**",
                    "/image/**",
                    "/error",
                    "/api/**",
                    "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(PathConstants.LOGIN)
                .loginProcessingUrl(PathConstants.LOGIN)
                .defaultSuccessUrl("/mypage", true)
                .usernameParameter("email") // 이메일을 사용자 이름으로 사용
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(PathConstants.LOGOUT)
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .userDetailsService(memberDetailsService)
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            )
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
