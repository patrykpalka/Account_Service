package account.config;

import account.event.CustomAccessDeniedHandler;
import account.event.CustomAuthenticationFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final OriginalRequestFilter requestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(requestFilter, BasicAuthenticationFilter.class)
                .exceptionHandling(this::configureExceptionHandling)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .authorizeRequests(this::configureAuthorizationRules)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> ex) {
        ex.authenticationEntryPoint(failureHandler::onAuthenticationFailure)
                .accessDeniedHandler(accessDeniedHandler);
    }

    private void configureAuthorizationRules(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers("/actuator/shutdown").permitAll()
                .requestMatchers("/api/auth/changepass").hasAnyRole("USER", "ACCOUNTANT", "ADMINISTRATOR")
                .requestMatchers("/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                .requestMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                .requestMatchers("/api/security/events/").hasRole("AUDITOR")
                .anyRequest().denyAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}
