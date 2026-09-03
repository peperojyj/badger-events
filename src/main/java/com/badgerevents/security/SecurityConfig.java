package com.badgerevents.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository
    ) throws Exception {
        http
                // 로그인된 auth를 httpSession에 저장하고, 이후 요청에도 다시 읽어라
                .securityContext(context -> context
                        .securityContextRepository(
                                securityContextRepository
                        )
                )
                // 어떤 요청을 누가 사용할 수 있는가!
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/error",
                                "/api/hello",
                                "/api/auth/csrf",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/events",
                                "/api/events/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/auth/me")
                        .authenticated()
                        .anyRequest()
                        .authenticated()
                )
                // 앞의 에러등 401&403 표기
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                authenticationEntryPoint()
                        )
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // 전부 필요한데서 로그아웃할 때 제거
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((
                                request,
                                response,
                                authentication
                        ) -> response.setStatus(
                                HttpStatus.NO_CONTENT.value()
                        ))
                )
                //Spring Security의 기본 HTML 로그인 페이지와 HTTP Basic 인증을 끄기
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // 매니저
    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    // SecurityContext를 HttpSession에 저장하고 다시 조회
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // 로그인 성공 시 기존 Session ID를 새 ID로 변경해 Session Fixation 공격을 방어
    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) ->
                response.setStatus(
                        HttpStatus.UNAUTHORIZED.value()
                );
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) ->
                response.setStatus(
                        HttpStatus.FORBIDDEN.value()
                );
    }
}