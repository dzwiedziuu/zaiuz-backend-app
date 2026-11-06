package pl.aniedzwiedz.zaiuzbackendapp.service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.model.auth.Session;
import pl.aniedzwiedz.zaiuzbackendapp.model.auth.User;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.SessionRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.UserRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.TokenHasher;
import pl.aniedzwiedz.zaiuzbackendapp.security.user.UserPrincipal;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

	private final Duration EXPIRATION_TIME = Duration.ofMinutes(60);

	private final UserRepository users;
	private final SessionRepository sessions;
	private final PasswordEncoder encoder;
	private final TokenHasher hasher;

	public AuthService(UserRepository users, SessionRepository sessions, PasswordEncoder encoder, TokenHasher hasher) {
		this.users = users;
		this.sessions = sessions;
		this.encoder = encoder;
		this.hasher = hasher;
	}

	@Transactional
	public LoginResponse login(String username, String password) {
		User user = users.findByUsername(username).orElse(null);
		boolean matches = user != null && this.encoder.matches(password, user.getPasswordHash());
		if (!matches) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niepoprawny login lub hasło");
		}
		Instant now = Instant.now();
		this.sessions.deleteExpired(now);
		String token = UUID.randomUUID().toString();
		Session session = new Session();
		session.setUserId(user.getId());
		session.setTokenHash(this.hasher.hash(token));
		session.setExpiresAt(now.plus(EXPIRATION_TIME));
		this.sessions.save(session);
		return new LoginResponse(token, "Bearer", EXPIRATION_TIME.toSeconds());
	}

	@Transactional
	public void logout(UserPrincipal principal) {
		this.sessions.deleteById(principal.sessionId());
	}

	/** Po zmianie hasła pozostałe sesje tracą ważność; aktualna działa dalej. */
	@Transactional
	public void changePassword(UserPrincipal principal, String currentPassword, String newPassword) {
		User user = this.users.findById(principal.adminId()).orElseThrow();
		if (!this.encoder.matches(currentPassword, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bieżące hasło jest niepoprawne");
		}
		user.setPasswordHash(this.encoder.encode(newPassword));
		this.sessions.deleteOthers(principal.adminId(), principal.sessionId());
	}
}
