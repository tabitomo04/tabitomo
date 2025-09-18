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
                        // 공개적으로 접근 가능한 페이지들
                        .requestMatchers(
                            "/member/info/**",       // 다른 사용자 정보 페이지
                            "/member/saved-spots/**"  // 저장한 여행지 리스트
                        ).permitAll()
                        // 스토리북 관련 경로 설정
                        .requestMatchers(
                            "/storybook/list",        // 스토리북 목록
                            "/storybook/detail/**"    // 스토리북 상세 보기
                        ).permitAll()
                        // 인증이 필요한 경로
                        .requestMatchers(
                            "/mypage/**",            // 마이페이지
                            "/storybook/write",       // 스토리북 작성
                            "/storybook/editor/**"    // 스토리북 에디터
                        ).authenticated()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 인증 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 로그인 페이지로 리다이렉트
                            response.sendRedirect("/auth/login?error=unauthorized");
                        })
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
                            response.sendRedirect("/auth/login?error=true");
                        })
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
