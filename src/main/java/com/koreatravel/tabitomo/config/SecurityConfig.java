package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;
import com.koreatravel.tabitomo.config.security.UserDetailsServiceImpl;
<<<<<<< Updated upstream
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
=======
import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

=======
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
>>>>>>> Stashed changes
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
<<<<<<< Updated upstream

        private final UserDetailsServiceImpl userDetailsService;
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
=======
    
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, 
                         CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }
>>>>>>> Stashed changes

        private boolean isAjaxRequest(HttpServletRequest request) {
                return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        }

<<<<<<< Updated upstream
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
=======
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 세션 관리 설정
        http.sessionManagement(session -> {
            // 동시 세션 제어 설정
            session.maximumSessions(1)
                   .maxSessionsPreventsLogin(false)
                   .expiredUrl("/auth/login?expired");
            
            // 세션 고정 보호 설정
            session.sessionFixation().newSession();
            
            // 세션 무효화 URL 설정
            session.invalidSessionUrl("/auth/login?expired");
        });
        
        // 보안 헤더 설정
        http.headers(headers -> {
            // X-Frame-Options 설정 (iframe 내에서의 페이지 로드 허용)
            headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
            
            // Content Security Policy 설정
            headers.contentSecurityPolicy(csp -> 
                csp.policyDirectives("script-src 'self'; object-src 'none';")
            );
        });

        // CSRF 보호 설정
        http.csrf(csrf -> 
            csrf.ignoringRequestMatchers("/api/**", "/h2-console/**")
        )
            // 인증 및 인가 설정
            .authorizeHttpRequests(authorize -> {
                // 정적 리소스 허용
                authorize.requestMatchers(
                    "/",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error"
                ).permitAll();
                
                // 인증 없이 접근 가능한 URL
                authorize.requestMatchers(
                    "/auth/**",
                    "/h2-console/**",
                    "/api/public/**"
                ).permitAll();
                
                // 나머지 요청은 인증 필요
                authorize.anyRequest().authenticated();
            })
            // 폼 로그인 설정
            // 폼 로그인 설정
            .formLogin(form -> {
                form.loginPage("/auth/login")
                    .loginProcessingUrl("/auth/login")
                    .usernameParameter("email")  // 이메일 파라미터 이름 명시
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)
                    .successHandler(customAuthenticationSuccessHandler)
                    .failureUrl("/auth/login?error=true")
                    .permitAll();
            })
            // 로그아웃 설정
            // 로그아웃 설정
            .logout(logout -> {
                logout.logoutUrl("/auth/logout")
                      .logoutSuccessUrl("/auth/login?logout")
                      .invalidateHttpSession(true)
                      .deleteCookies("JSESSIONID", "remember-me")
                      .clearAuthentication(true)
                      .addLogoutHandler((request, response, authentication) -> {
                          // 세션 무효화
                          HttpSession session = request.getSession(false);
                          if (session != null) {
                              session.invalidate();
                          }
                          // SecurityContext 지우기
                          SecurityContextHolder.clearContext();
                      });
            })
            // 예외 처리
            .exceptionHandling(exception -> {
                exception.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.sendRedirect("/auth/login?error=unauthorized");
                });
                
                exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.sendRedirect("/auth/access-denied");
                });
            })
                          .clearAuthentication(true)
                          .addLogoutHandler((request, response, authentication) -> {
                              // 세션 무효화
                              HttpSession session = request.getSession(false);
                              if (session != null) {
                                  session.invalidate();
                              }
                              // SecurityContext 지우기
                              SecurityContextHolder.clearContext();
                          });
                })
                
                // Session management is configured at the beginning of the filter chain
                // 자동 로그인 설정
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(1209600) // 2주
                        .userDetailsService(userDetailsService)
                        .rememberMeParameter("remember-me")
                )
                // 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (isAjaxRequest(request)) {
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                            } else {
                                String message = "로그인이 필요한 서비스입니다. 로그인 후 이용해주세요.";
                                response.sendRedirect("/auth/login?error=unauthorized&message=" +
                                        java.net.URLEncoder.encode(message, "UTF-8"));
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
                // 사용자 세션 관리
                .userDetailsService(userDetailsService);
>>>>>>> Stashed changes

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
