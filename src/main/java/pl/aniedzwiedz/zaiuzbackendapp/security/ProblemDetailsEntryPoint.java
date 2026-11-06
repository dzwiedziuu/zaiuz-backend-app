package pl.aniedzwiedz.zaiuzbackendapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Odpowiedź 401 dla żądań bez ważnego uwierzytelnienia, w formacie Problem Details (RFC 9457).
 * Błąd jest przekazywany do obsługi wyjątków Spring MVC (ApiExceptionHandler), dzięki czemu
 * ma ten sam format co pozostałe błędy API.
 */
public class ProblemDetailsEntryPoint implements AuthenticationEntryPoint {

	private final HandlerExceptionResolver resolver;

	private final String detail;

	public ProblemDetailsEntryPoint(HandlerExceptionResolver resolver, String detail) {
		this.resolver = resolver;
		this.detail = detail;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		this.resolver.resolveException(request, response, null, new ResponseStatusException(HttpStatus.UNAUTHORIZED, this.detail));
	}
}
