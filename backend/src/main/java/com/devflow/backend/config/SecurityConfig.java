package com.devflow.backend.config;

import com.devflow.backend.mapper.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserMapper userMapper) {
        return username -> {
            var user = userMapper.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("用户名或密码错误");
            }

            return User.withUsername(user.username())
                .password(user.passwordHash())
                .roles("USER")
                .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) throws Exception {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        http.authenticationProvider(provider)
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/api/health", "/api/auth/csrf", "/api/auth/login", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .requestCache(cache -> cache.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint((request, response, exception) ->
                    response.setStatus(401)
                )
            )
            .formLogin(form ->
                form
                    .loginProcessingUrl("/api/auth/login")
                    .successHandler((request, response, authentication) -> response.setStatus(204))
                    .failureHandler((request, response, exception) -> response.setStatus(401))
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(204)
                    )
            );

        return http.build();
    }
}
