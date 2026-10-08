package com.devflow.backend.controller;

import com.devflow.backend.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(
                token.getHeaderName(),
                token.getToken()
        );
    }

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication) {
        var user = userMapper.findByUsername(authentication.getName());

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return new CurrentUser(
                user.id(),
                user.username(),
                user.displayName()
        );
    }

    public record CsrfResponse(String headerName, String token) {
    }

    public record CurrentUser(
            Long id,
            String username,
            String displayName
    ) {
    }
}