package pl.aniedzwiedz.zaiuzbackendapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SensorRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.sensor.ApiKeyFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain sensorChain(HttpSecurity http, SensorRepository sensors, TokenHasher hasher) {
		return http.securityMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/measurements"))
				.csrf(AbstractHttpConfigurer::disable)
				.addFilterBefore(new ApiKeyFilter(sensors, hasher), UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests((auth) -> auth.anyRequest().authenticated())
				.build();
	}
}
