package pl.aniedzwiedz.zaiuzbackendapp.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.aniedzwiedz.zaiuzbackendapp.model.auth.User;
import pl.aniedzwiedz.zaiuzbackendapp.repository.auth.UserRepository;

@Component
class AdminUserBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminUserBootstrap.class);

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final String username;
	private final String password;

	AdminUserBootstrap(UserRepository users, PasswordEncoder encoder, @Value("${app.admin.username}") String username, @Value("${app.admin.password}") String password) {
		this.users = users;
		this.encoder = encoder;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (this.users.count() > 0) {
			return;
		}
		User admin = new User();
		admin.setUsername(this.username);
		admin.setPasswordHash(this.encoder.encode(this.password));
		this.users.save(admin);
		log.info("Utworzono konto administratora '{}'", this.username);
	}
}
