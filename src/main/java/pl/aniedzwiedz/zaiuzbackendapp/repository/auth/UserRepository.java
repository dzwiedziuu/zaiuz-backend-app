package pl.aniedzwiedz.zaiuzbackendapp.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.aniedzwiedz.zaiuzbackendapp.model.auth.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
}
