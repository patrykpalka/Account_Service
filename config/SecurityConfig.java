package account.config;

import account.event.CustomAccessDeniedHandler;
import account.event.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final OriginalRequestFilter originalRequestFilter;

    @Autowired
    public SecurityConfig(CustomAuthenticationFailureHandler customAuthenticationFailureHandler,
                          CustomAccessDeniedHandler customAccessDeniedHandler,
                          OriginalRequestFilter originalRequestFilter) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.originalRequestFilter = originalRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Enable basic authentication for HTTP requests
                .httpBasic(Customizer.withDefaults())

                // Add custom filter to capture original request URI
                .addFilterBefore(originalRequestFilter, BasicAuthenticationFilter.class)

                // Set up error handlers for unauthorized and forbidden access
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationFailureHandler::onAuthenticationFailure)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // Disable CSRF and default headers for stateless API
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)

                // Configure which endpoints are accessible based on user roles
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers("/api/auth/changepass").hasAnyRole("USER", "ACCOUNTANT", "ADMINISTRATOR")
                        .requestMatchers("/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                        .requestMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/security/events/").hasRole("AUDITOR")
                        .anyRequest().denyAll()) // Deny access to any other requests

                // Use stateless sessions (no session data is stored on the server)
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}
