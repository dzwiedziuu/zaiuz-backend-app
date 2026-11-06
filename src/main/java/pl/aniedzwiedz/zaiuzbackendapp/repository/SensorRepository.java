package pl.aniedzwiedz.zaiuzbackendapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.aniedzwiedz.zaiuzbackendapp.model.Sensor;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

	Optional<Sensor> findByKeyHash(String keyHash);
}
