package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.config.security.CustomAuthenticationSuccessHandler;
import com.koreatravel.tabitomo.config.security.UserDetailsServiceImpl;
import com.koreatravel.tabitomo.service.member.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.koreatravel.tabitomo.config.security.CustomJdbcTokenRepositoryImpl;
import javax.sql.DataSource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.security.web.session.SessionManagementFilter;
import lombok.extern.slf4j.Slf4j;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final DataSource dataSource;
    private final MemberService memberService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, 
                         CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                         DataSource dataSource,
                         MemberService memberService) {
        this.userDetailsService = userDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.dataSource = dataSource;
        this.memberService = memberService;
    }

    private boolean isAjaxRequest(jakarta.servlet.ServletRequest request) {
        if (request instanceof jakarta.servlet.http.HttpServletRequest) {
            return "XMLHttpRequest"
                .equals(((jakarta.servlet.http.HttpServletRequest) request)
                .getHeader("X-Requested-With"));
        }
        return false;
    }
    
    private boolean isPublicPage(String requestURI) {
        // 공개 페이지 URI 패턴 정의 - authorizeHttpRequests의 permitAll()과 일치시킴
        String[] publicPatterns = {
            "/", 
            "/index", 
            "/index.html",
            "/home",
            "/main", 
            "/main/",
            "/about", 
            "/contact",
            "/privacy",
            "/terms",
            "/error",
            "/public/",
            "/css/", 
            "/js/", 
            "/images/",
            "/image/",
            "/fonts/",
            "/h2-console/",
            "/uploadedImages/",
            "/trips/public",
            "/tripinformation/",
            "/tripselect/",
            "/api/translate/",
            "/trip/",
            "/api/favorites/status",
            "/api/public/",
            "/api/places/",
            "/auth/login",
            "/auth/signup",
            "/api/auth/",
            "/api/email/",
            "/member/api/",
            "/member/info/",
            "/member/saved-spots/",
            "/storybook/list",
            "/storybook/detail/",
            "/chatbot/intro",
            "/chat/",
            "/api/",
            "/upload",
            "/question/start",
            "/question/form",
            "/question/complete",
            "/member/api/**"
        };
        
        // API 문서, 스웨거 등 개발 환경에서의 공개 엔드포인트
        if (requestURI.startsWith("/v3/api-docs") || 
            requestURI.startsWith("/swagger") ||
            requestURI.startsWith("/webjars")) {
            return true;
        }
        
        // 정적 리소스는 항상 허용
        if (requestURI.endsWith(".css") || 
            requestURI.endsWith(".js") || 
            requestURI.endsWith(".png") || 
            requestURI.endsWith(".jpg") || 
            requestURI.endsWith(".jpeg") || 
            requestURI.endsWith(".gif") ||
            requestURI.endsWith(".ico") ||
            requestURI.endsWith(".woff") ||
            requestURI.endsWith(".woff2") ||
            requestURI.endsWith(".ttf") ||
            requestURI.endsWith(".svg")) {
            return true;
        }
        
        // 공개 패턴과 일치하는지 확인
        for (String pattern : publicPatterns) {
            if (requestURI.startsWith(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        // CORS 설정에 노출할 헤더들
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Disposition", 
            "X-Auth-Token", 
            "Authorization", 
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials",
            "X-CSRF-TOKEN",
            "X-Requested-With",
            "Content-Type"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        CustomJdbcTokenRepositoryImpl tokenRepository = new CustomJdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // 테이블이 없으면 자동 생성 (개발 환경에서만 사용)
        // tokenRepository.setCreateTableOnStartup(true);
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        // CSRF 설정
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers("/question/submit") // CSRF 검사에서 제외
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
        
        // 인가 설정 - 모든 경로에 대한 권한 설정을 한 곳에서 관리
        http.authorizeHttpRequests(authorize -> {
            // 1. 정적 리소스 (모두 허용)
            authorize.requestMatchers(
                "/", "/index", "/index.html", "/home",
                "/css/**", "/js/**", "/images/**", "/image/**",
                "/fonts/**", "/favicon.ico", "/error",
                "/uploadedImages/**", "/h2-console/**"
            ).permitAll();
            
            // 2. 공개 API 및 페이지 (모두 허용)
            authorize.requestMatchers(
                "/trips/public", "/tripinformation/**", "/tripselect/**",
                "/about", "/contact", "/privacy", "/terms",
                "/api/translate/**", "/trip/**", "/api/favorites/status",
                "/main", "/main/**", "/api/public/**", "/api/places/**",
                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
                "/question/start", "/question/form", "/question/complete",
                "/webjars/**", "/public/**"
            ).permitAll();
            
            // 3. 인증 관련 (모두 허용)
            authorize.requestMatchers(
                "/auth/**",
                "/api/auth/**", "/api/email/**",
                "/auth/reset-password"
            ).permitAll();
            
            // 4. 멤버 관련 (모두 허용)
            authorize.requestMatchers(
                "/member/api/**",
                "/member/info/**",
                "/member/saved-spots/**"
            ).permitAll();
            
            // 5. 스토리북 (목록 및 상세는 허용, 나머지는 인증 필요)
            authorize.requestMatchers(
                "/storybook/list",
                "/storybook/detail/**"
            ).permitAll();
            
            // 6. 채팅 (모두 허용)
            authorize.requestMatchers(
                "/chatbot/intro",
                "/chat/**",
                "/api/**"
            ).permitAll();
            
            // 7. 파일 업로드 (허용)
            authorize.requestMatchers("/upload").permitAll();
            
            // 8. 보호된 리소스 (인증 필요)
            authorize.requestMatchers(
                "/mypage/**",
                "/storybook/write",
                "/storybook/editor/**",
                "/storybook/save",
                "/storybook/tempsave",
                "/storybook/update/**"
            ).authenticated();
            
            // 9. 나머지 모든 요청은 인증 필요
            authorize.anyRequest().authenticated();
        });
        
        // CSRF 보호 설정 (필요한 엔드포인트만 제외)
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/api/favorites/**",
                "/api/translate/**",
                "/upload",
                "/api/**",
                "/chat/**",
                "/h2-console/**",
                "/auth/reset-password",
                "/api/email/**",
                "/tripselect/**",
                "/trip/**",
                "/trip/step3",
                "/trip/step4",
                "/trip/save",
                "/js/**",
                "/image/**",
                "/fonts/**",
                "/favicon.ico",
                "/css/**",
                "/trips/public",
                "/tripinformation",
                "/tripinformation/places",
                "/auth/signup",
                "/storybook/save",
                "/storybook/tempsave",
                "/question/start",
                "/question/form",
                "/question/complete"
            )
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
        
        // CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        
        // 보안 헤더 설정
        http.headers(headers -> {
            headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://code.jquery.com https://unpkg.com https://npmcdn.com https://cdn.tailwindcss.com https://cdn.ckeditor.com https://cdn.ckbox.io; " +
                        "style-src 'self' 'unsafe-inline' https: https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://unpkg.com https://cdn.ckeditor.com https://fonts.googleapis.com; " +
                        "img-src 'self' data: https: *.tile.openstreetmap.org; " +
                        "font-src 'self' https: https://fonts.gstatic.com data:; " +
                        "connect-src 'self' http://localhost:8080 https://cdn.jsdelivr.net https://cdn.tailwindcss.com https://cdn.ckeditor.com https://cdn.ckbox.io https://proxy-event.ckeditor.com; " +
                        "frame-src 'self' https://www.youtube.com https://www.youtube-nocookie.com;"
                    )
                );
        });
        
        // 모든 요청에 대한 세션 검증 필터 추가 - 공개 페이지는 검증하지 않음
                http.addFilterAfter((jakarta.servlet.Filter) (request, response, chain) -> {
                    String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                    
                    // 공개 페이지 또는 정적 리소스인 경우 세션 검증 제외
                    if (isPublicPage(requestURI)) {
                        chain.doFilter(request, response);
                        return;
                    }
                    
                    jakarta.servlet.http.HttpSession session = ((jakarta.servlet.http.HttpServletRequest) request).getSession(false);
                    if (session != null && session.getAttribute("memberProfile") != null) {
                        // Remember-Me 인증이 아닌 경우에만 세션 검증
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && !(auth.getPrincipal() instanceof String) && 
                            auth.getAuthorities().stream().noneMatch(g -> g.getAuthority().equals("ROLE_REMEMBER"))) {
                
                            // 세션에 memberProfile이 있지만 DB에 사용자 정보가 없는 경우 로그아웃 처리
                            try {
                                MemberProfileDTO profile = (MemberProfileDTO) session.getAttribute("memberProfile");
                                if (profile != null && profile.getId() != null) {
                                    memberService.findById(profile.getId()); // 사용자 정보가 없으면 예외 발생
                                } else {
                                    throw new IllegalStateException("Invalid member profile in session");
                                }
                            } catch (Exception e) {
                                session.invalidate();
                                SecurityContextHolder.clearContext();
                                
                                // AJAX 요청인 경우 401 에러 반환
                                if (isAjaxRequest(request)) {
                                    jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;
                                    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                                    httpResponse.setContentType("application/json");
                                    httpResponse.getWriter().write("{\"error\":\"session-expired\"}");
                                } else {
                                    // 일반 요청인 경우 로그인 페이지로 리다이렉트
                                    ((jakarta.servlet.http.HttpServletResponse) response).sendRedirect(
                                        "/auth/login?error=session-expired&redirect=" + 
                                        URLEncoder.encode(requestURI, StandardCharsets.UTF_8)
                                    );
                                }
                                return;
                            }
                        }
                    }
                    chain.doFilter(request, response);
        }, SessionManagementFilter.class);
        
        // 폼 로그인 설정
        http.formLogin(form -> form
            .loginPage("/auth/login")
            .loginProcessingUrl("/auth/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .defaultSuccessUrl("/", true)
            .successHandler(customAuthenticationSuccessHandler)
            .failureHandler((request, response, exception) -> {
                String errorMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";
                if (exception.getMessage() != null && exception.getMessage().contains("비활성화된 계정")) {
                    errorMessage = "비활성화된 계정입니다. 관리자에게 문의해주세요.";
                }
                response.sendRedirect("/auth/login?error=true&message=" + 
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
            })
            .permitAll()
        );
        
        // 로그아웃 설정
        http.logout(logout -> {
            logout.logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .addLogoutHandler((request, response, authentication) -> {
                    // SecurityContext 지우기
                    SecurityContextHolder.clearContext();
                })
                .permitAll();
        });
        
        // Remember Me 설정
        http.rememberMe(remember -> remember
            .key("uniqueAndSecretKey") // 안전한 키
            .tokenValiditySeconds(1209600) // 2주
            .userDetailsService(userDetailsService)
            .tokenRepository(persistentTokenRepository())
            .rememberMeParameter("remember-me")
            .rememberMeCookieName("REMEMBER_ME_COOKIE")
            .useSecureCookie(true) // HTTPS 사용 시
        );
        
        // 인가 설정처리
        http.exceptionHandling(exception -> {
            // 인증 실패 시 처리
            exception.authenticationEntryPoint((request, response, authException) -> {
                String requestURI = request.getRequestURI();
                log.error("Authentication failed for URI: {}", requestURI);
                log.error("Authentication exception: {}", authException.getMessage());
                
                // AJAX 요청인 경우 JSON 응답 반환
                if (isAjaxRequest(request)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"인증에 실패했습니다. 다시 로그인해주세요.\",\"code\":\"UNAUTHORIZED\"}"
                    );
                } else {
                    // 일반 요청인 경우 로그인 페이지로 리다이렉트
                    String redirectUrl = "/auth/login?redirect=" + 
                        URLEncoder.encode(
                            requestURI + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                            StandardCharsets.UTF_8
                        );
                    response.sendRedirect(redirectUrl);
                }
                if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\"}");
                } else {
                    String message = "로그인이 필요한 서비스입니다. 로그인 후 이용해주세요.";
                    response.sendRedirect("/auth/login?error=unauthorized&message=" +
                            URLEncoder.encode(message, StandardCharsets.UTF_8));
                }


            });
            
            // 인가 실패 시 처리
            exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                if (isAjaxRequest(request) || request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                } else {
                    response.sendRedirect("/auth/access-denied");
                }
            });
        });

        // UserDetailsService 설정
        http.userDetailsService(userDetailsService);

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
