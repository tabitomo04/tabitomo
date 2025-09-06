package com.koreatravel.tabitomo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스, 홈페이지, 로그인, 회원가입 페이지는 누구나 접근 가능
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/image/**").permitAll()
                        // 그 외의 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 사용자 정의 로그인 페이지 URL
                        .loginProcessingUrl("/login") // 로그인 처리 URL (Spring Security가 처리)
                        .defaultSuccessUrl("/mypage", true) // 로그인 성공 후 이동할 페이지
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // 로그아웃 성공 후 이동할 페이지
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
