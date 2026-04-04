package it.nova.novamed.config;

import it.nova.novamed.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // NECESSARIO PER H2
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/me").permitAll()
                        .requestMatchers("/api/auth/change-password").permitAll()

                        // IMPORTANTISSIMO
                        .requestMatchers("/api/patients/me").permitAll()
                        .requestMatchers("/api/doctors/me").permitAll()

                        // ADMIN
                        .requestMatchers("/api/admin/**")
                        .access((authentication, context) ->
                                roleDecision(context, Role.ADMIN)
                        )

                        // DOCTORS LIST
                        .requestMatchers(HttpMethod.GET, "/api/doctors").authenticated()

                        // DOCTOR
                        .requestMatchers("/api/doctors/*/slots").permitAll()
                        .requestMatchers("/api/doctors/**")
                        .access((authentication, context) ->
                                roleDecision(context, Role.DOCTOR)
                        )

                        // PATIENT
                        .requestMatchers("/api/patients/**")
                        .access((authentication, context) ->
                                roleDecision(context, Role.PATIENT)
                        )

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AuthorizationDecision roleDecision(RequestAuthorizationContext context, Role requiredRole) {

        Object raw = Optional.ofNullable(context)
                .map(RequestAuthorizationContext::getRequest)
                .map(req -> req.getSession(false))
                .map(session -> session.getAttribute("role"))
                .orElse(null);

        Role sessionRole = null;

        if (raw instanceof Role r) {
            sessionRole = r;
        } else if (raw instanceof String s) {
            try {
                sessionRole = Role.valueOf(s);
            } catch (IllegalArgumentException ignored) {}
        }

        return new AuthorizationDecision(requiredRole.equals(sessionRole));
    }
}