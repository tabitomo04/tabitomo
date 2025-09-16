package com.koreatravel.tabitomo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스는 시큐리티 필터를 거치지 않도록 설정
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/image/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (개발 편의를 위해)
                .csrf(csrf -> csrf.disable())
                // 모든 요청을 인증 없이 허용 (개발 편의를 위해)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                // 폼 로그인 기능은 다시 활성화
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 사용자 정의 로그인 페이지
                        .defaultSuccessUrl("/") // 로그인 성공 후 이동할 페이지
                        .permitAll() // 로그인 페이지 자체는 모두 접근 가능
                )
                // 로그아웃 기능도 다시 활성화
                .logout(logout -> logout
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
