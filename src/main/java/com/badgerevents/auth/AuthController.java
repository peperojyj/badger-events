package com.badgerevents.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    private final SecurityContextHolderStrategy contextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy =
                sessionAuthenticationStrategy;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 미인증 authentication 만들기
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizedEmail,
                        request.password()
                );

        try {
            // 인증 진행. 여기서 manager 부름 (여기서 이미 다함. 인증된 auth return)
            Authentication authentication =
                    authenticationManager.authenticate(
                            authenticationRequest
                    );

            // 응답용 dto를 만든다. 인증된 authenticatoin에서 이름 가져옴
            AuthUserResponse userResponse =
                    authService.getCurrentUser(
                            authentication.getName()
                    );


            // 로그인 전 CSRF token을 받으면서 이미 Session이 만들어질 수 있다. therforea 로그인 성공후 sessionID 새 값으로 교체
            sessionAuthenticationStrategy.onAuthentication(
                    authentication,
                    httpRequest,
                    httpResponse
            );

            // 빈 security context 생성 - 현재의 인증정보
            SecurityContext context =
                    contextHolderStrategy.createEmptyContext();


             // Authentication을 Context에 넣기 -현재 요청에서 사용
            context.setAuthentication(authentication);
            // 그 상자를 현재 요청을 처리하는 실행 흐름에 연결함
            contextHolderStrategy.setContext(context);

            // 그 상자를 HttpSession에도 저장하여 다음 요청에서 복원할 수 있게 함
            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            return userResponse;
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }
    }

    @GetMapping("/me")
    public AuthUserResponse me(Authentication authentication) {
        return authService.getCurrentUser(
                authentication.getName()
        );
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return CsrfTokenResponse.from(csrfToken);
    }
}