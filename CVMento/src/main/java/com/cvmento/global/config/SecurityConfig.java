package com.cvmento.global.config;

import com.cvmento.global.security.JwtAuthenticationFilter;
import com.cvmento.global.security.OAuth2SuccessHandler;
import com.cvmento.global.security.InternalApiKeyFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final InternalApiKeyFilter internalApiKeyFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          CorsConfigurationSource corsConfigurationSource,
                          InternalApiKeyFilter internalApiKeyFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
        this.internalApiKeyFilter = internalApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/auth/test-login",
                                "/auth/login/google",
                                "/auth/google/login",
                                "/auth/refresh",
                                "/auth/status",
                                "/health",
                                "/error",
                                "/auth/google/url",
                                "/auth/quick-login/**", // 개발용
                                "/actuator/**",
                                "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers("/api/jobs/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers("/api/cover-letter-features/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers("/api/crawled-cover-letters/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers("/api/internal/**").authenticated() // API Key로 인증된 요청만 허용
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = String.format(
                    "{\"success\":false,\"errorCode\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\",\"canRetry\":false,\"timestamp\":%d}",
                    System.currentTimeMillis()
            );

            response.getWriter().write(jsonResponse);
        };
    }
}