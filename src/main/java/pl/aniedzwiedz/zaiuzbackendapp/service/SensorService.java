package pl.aniedzwiedz.zaiuzbackendapp.service;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.model.Sensor;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SensorRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SeriesRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.TokenHasher;
import pl.aniedzwiedz.zaiuzbackendapp.validation.EntityValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class SensorService {

	private final SensorRepository sensors;

	private final SeriesRepository series;

	private final EntityValidator validator;

	private final TokenHasher hasher;

	SensorService(SensorRepository sensors, SeriesRepository series, EntityValidator validator, TokenHasher hasher) {
		this.sensors = sensors;
		this.series = series;
		this.validator = validator;
		this.hasher = hasher;
	}

	public List<Sensor> list() {
		return this.sensors.findAll(Sort.by("id"));
	}

	public Sensor get(long id) {
		return this.sensors.findById(id).orElseThrow(SensorService::notFound);
	}

	@Transactional
	public String create(Sensor input) {
		input.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
		String apiKey = input.generateKeyHash(hasher);
		this.validator.validate(input);
		if (!this.series.existsById(input.getSeriesId())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Wskazana seria nie istnieje");
		}
		this.sensors.save(input);
		return apiKey;
	}

	@Transactional
	public void delete(long id) {
		ensureExists(id);
		// Wyniki zostają: baza zeruje measurement.sensor_id (ON DELETE SET NULL)
		this.sensors.deleteById(id);
	}

	private void ensureExists(long id) {
		if (!this.sensors.existsById(id)) {
			throw notFound();
		}
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Czujnik nie istnieje");
	}
}
