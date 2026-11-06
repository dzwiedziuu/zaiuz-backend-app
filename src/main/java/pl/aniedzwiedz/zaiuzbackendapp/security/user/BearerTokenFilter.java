package pl.aniedzwiedz.zaiuzbackendapp.security.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.SessionRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.TokenHasher;

import java.io.IOException;
import java.time.Instant;

public class BearerTokenFilter extends OncePerRequestFilter {

	private static final String PREFIX = "Bearer ";

	private final SessionRepository sessions;
	private final TokenHasher hasher;

	public BearerTokenFilter(SessionRepository sessions, TokenHasher hasher) {
		this.sessions = sessions;
		this.hasher = hasher;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(PREFIX)) {
			sessions.findByTokenHashAndExpiresAtAfter(this.hasher.hash(header.substring(PREFIX.length())), Instant.now())
				.ifPresent((session) -> {
					UserPrincipal principal = new UserPrincipal(session.getUserId(), session.getId());
					SecurityContext context = SecurityContextHolder.createEmptyContext();
					context.setAuthentication(new UserTokenAuthentication(principal));
					SecurityContextHolder.setContext(context);
				});
		}
		chain.doFilter(request, response);
	}
}
