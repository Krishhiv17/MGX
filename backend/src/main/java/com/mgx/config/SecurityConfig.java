package com.mgx.config;

import com.mgx.apikey.security.ApiKeyAuthFilter;
import com.mgx.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    JwtAuthenticationFilter jwtFilter,
    ApiKeyAuthFilter apiKeyAuthFilter
  )
    throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> {})
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/v1/auth/**",
          "/v1/otp/**",
          "/actuator/**",
          "/swagger-ui/**",
          "/v3/api-docs/**"
        ).permitAll()
        .requestMatchers("/v1/private/**").hasAuthority("SCOPE_PRIVATE")
        .anyRequest().authenticated()
      )
      .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
