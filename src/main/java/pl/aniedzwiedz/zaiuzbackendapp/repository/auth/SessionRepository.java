package pl.aniedzwiedz.zaiuzbackendapp.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.aniedzwiedz.zaiuzbackendapp.model.auth.Session;

import java.time.Instant;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

	Optional<Session> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

	@Modifying
	@Query("delete from Session s where s.expiresAt <= :now")
	void deleteExpired(@Param("now") Instant now);

	@Modifying
	@Query("delete from Session s where s.userId = :userId and s.id <> :keepId")
	void deleteOthers(@Param("userId") long userId, @Param("keepId") long keepId);
}
