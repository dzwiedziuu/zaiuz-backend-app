package pl.aniedzwiedz.zaiuzbackendapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SensorRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.SessionRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.sensor.ApiKeyFilter;
import pl.aniedzwiedz.zaiuzbackendapp.security.user.BearerTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain sensorChain(HttpSecurity http, SensorRepository sensors, TokenHasher hasher) {
        return http.securityMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/measurements"))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((e) -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new ApiKeyFilter(sensors, hasher), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((auth) -> auth.anyRequest().authenticated())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiChain(HttpSecurity http, SessionRepository sessions, TokenHasher hasher) {
        return http.addFilterBefore(new BearerTokenFilter(sessions, hasher), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((e) -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
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
