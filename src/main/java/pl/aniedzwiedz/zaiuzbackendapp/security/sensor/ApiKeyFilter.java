package pl.aniedzwiedz.zaiuzbackendapp.security.sensor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SensorRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.TokenHasher;

import java.io.IOException;

public class ApiKeyFilter extends OncePerRequestFilter {

	private final SensorRepository sensors;

	private final TokenHasher hasher;

	public ApiKeyFilter(SensorRepository sensors, TokenHasher hasher) {
		this.sensors = sensors;
		this.hasher = hasher;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String key = request.getHeader("X-API-Key");
		if (key != null) {
			sensors.findByKeyHash(this.hasher.hash(key)).ifPresent((sensor) -> {
				SensorPrincipal principal = new SensorPrincipal(sensor.getId(), sensor.getSeriesId());
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(new ApiKeyAuthentication(principal));
				SecurityContextHolder.setContext(context);
			});
		}
		chain.doFilter(request, response);
	}
}
