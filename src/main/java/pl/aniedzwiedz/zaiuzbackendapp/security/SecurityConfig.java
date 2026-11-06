package pl.aniedzwiedz.zaiuzbackendapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SensorRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.SessionRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.sensor.ApiKeyFilter;
import pl.aniedzwiedz.zaiuzbackendapp.security.user.BearerTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain sensorChain(HttpSecurity http, SensorRepository sensors, TokenHasher hasher,
                                    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        AuthenticationEntryPoint problem = new ProblemDetailsEntryPoint(resolver, "Wymagany ważny klucz API czujnika w nagłówku X-API-Key");
        AuthenticationEntryPoint entryPoint = (request, response, ex) -> {
            String error = request.getHeader("X-API-Key") == null ? "brak klucza API czujnika" : "niepoprawny lub wyrejestrowany klucz API czujnika";
            log.warn("Odrzucono wynik pomiaru z {}: {}", request.getRemoteAddr(), error);
            problem.commence(request, response, ex);
        };
        return http.securityMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/measurements"))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((e) -> e.authenticationEntryPoint(entryPoint))
                .addFilterBefore(new ApiKeyFilter(sensors, hasher), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((auth) -> auth.anyRequest().authenticated())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiChain(HttpSecurity http, SessionRepository sessions, TokenHasher hasher,
                                 @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        return http.addFilterBefore(new BearerTokenFilter(sessions, hasher), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((e) -> e.authenticationEntryPoint(new ProblemDetailsEntryPoint(resolver, "Wymagane zalogowanie: nagłówek Authorization: Bearer <token>")))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/series", "/api/series/*", "/api/measurements", "/api/measurements/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login")
                        .permitAll()
                        .requestMatchers("/api/measurements", "/api/measurements/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .build();
    }
}
