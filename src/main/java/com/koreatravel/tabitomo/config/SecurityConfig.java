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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화 (API 요청에 대해서는 CSRF 보호 비활성화)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                // 세션 정책 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 허용
                        .requestMatchers(
                            "/", 
                            "/css/**", 
                            "/js/**", 
                            "/image/**", 
                            "/images/**", 
                            "/favicon.ico", 
                            "/error"
                        ).permitAll()
                        // API 및 인증 관련 경로 허용
                        .requestMatchers(
                            "/auth/**", 
                            "/api/**", 
                            "/member/api/**"
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login") // 로그인 처리 URL 명시적 설정
                        .usernameParameter("email") // 사용자 이름 파라미터 (이메일로 로그인)
                        .passwordParameter("password") // 비밀번호 파라미터
                        .defaultSuccessUrl("/", true) // 로그인 성공 후 리다이렉트 URL
                        .failureUrl("/auth/login?error=true") // 로그인 실패 시 URL
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
