package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.PathConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 공개 접근 허용 경로
                .requestMatchers(
                    "/",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/h2-console/**",
                    PathConstants.JOIN,
                    PathConstants.LOGIN
                ).permitAll()
                // 인증된 사용자만 접근 가능한 경로
                .requestMatchers(
                    PathConstants.MEMBER_INFO,
                    PathConstants.MEMBER_QUESTION,
                    PathConstants.MEMBER_QUESTION + "/**",
                    PathConstants.TRAVEL + "/**"
                ).authenticated()
                // 관리자만 접근 가능한 경로 (필요시 추가)
                // .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(PathConstants.LOGIN)
                .defaultSuccessUrl(PathConstants.TRAVEL_LIST, true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(PathConstants.LOGOUT)
                .logoutSuccessUrl(PathConstants.LOGIN + "?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin() // H2 콘솔 사용을 위해
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
