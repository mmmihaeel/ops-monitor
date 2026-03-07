package com.example.opsmonitor.infrastructure.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher(EndpointRequest.toAnyEndpoint())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("MANAGEMENT"))
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/api/**", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/health", "/api/v1/health/readiness")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/audit-entries/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/services")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/health-snapshots")
                    .hasRole("OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/failed-jobs/*/retry")
                    .hasRole("OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/incidents")
                    .hasRole("OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/acknowledge")
                    .hasRole("OPERATOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/resolve")
                    .hasRole("OPERATOR")
                    .requestMatchers("/api/v1/**")
                    .hasAnyRole("VIEWER", "OPERATOR", "ADMIN")
                    .anyRequest()
                    .denyAll())
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
        .build();
  }

  @Bean
  public UserDetailsService userDetailsService(
      ManagementAuthProperties managementAuthProperties,
      ApiAuthProperties apiAuthProperties,
      PasswordEncoder passwordEncoder) {
    return new InMemoryUserDetailsManager(
        User.withUsername(managementAuthProperties.getUsername())
            .password(passwordEncoder.encode(managementAuthProperties.getPassword()))
            .roles("MANAGEMENT")
            .build(),
        User.withUsername(apiAuthProperties.getViewerUsername())
            .password(passwordEncoder.encode(apiAuthProperties.getViewerPassword()))
            .roles("VIEWER")
            .build(),
        User.withUsername(apiAuthProperties.getOperatorUsername())
            .password(passwordEncoder.encode(apiAuthProperties.getOperatorPassword()))
            .roles("VIEWER", "OPERATOR")
            .build(),
        User.withUsername(apiAuthProperties.getAdminUsername())
            .password(passwordEncoder.encode(apiAuthProperties.getAdminPassword()))
            .roles("VIEWER", "OPERATOR", "ADMIN")
            .build());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
