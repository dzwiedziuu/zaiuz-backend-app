package pl.aniedzwiedz.zaiuzbackendapp.service.measurement;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.model.Measurement;
import pl.aniedzwiedz.zaiuzbackendapp.parser.TimestampParser;
import pl.aniedzwiedz.zaiuzbackendapp.repository.MeasurementRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SeriesRepository;
import pl.aniedzwiedz.zaiuzbackendapp.security.sensor.SensorPrincipal;
import pl.aniedzwiedz.zaiuzbackendapp.validation.EntityValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeasurementService {

	private static final Logger log = LoggerFactory.getLogger(MeasurementService.class);

	private final MeasurementRepository measurements;
	private final SeriesRepository series;
	private final EntityValidator validator;
	private final TimestampParser timestamps;

	MeasurementService(MeasurementRepository measurements, SeriesRepository series, EntityValidator validator, TimestampParser timestamps) {
		this.measurements = measurements;
		this.series = series;
		this.validator = validator;
		this.timestamps = timestamps;
	}

	@Transactional
	public Measurement record(SensorPrincipal sensor, Measurement input) {
		try {
			input.setMeasuredAt(this.timestamps.parseOrNow(input.getRequestedTimestamp(), "timestamp"));
			input.setSeriesId(sensor.seriesId());
			input.setSensorId(sensor.sensorId());
			input.setSeries(this.series.findById(sensor.seriesId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seria czujnika nie istnieje")));
			this.validator.validate(input);
		}
		catch (ConstraintViolationException ex) {
			logRejection(sensor, input, ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
			throw ex;
		}
		catch (ResponseStatusException ex) {
			logRejection(sensor, input, ex.getReason());
			throw ex;
		}
		return this.measurements.save(input);
	}

	private static void logRejection(SensorPrincipal sensor, Measurement input, String reason) {
		log.warn("Odrzucono wynik pomiaru: czujnik {}, seria {}, wartość {}, timestamp {} – {}", sensor.sensorId(), sensor.seriesId(),
				input.getValue(), input.getRequestedTimestamp() != null ? input.getRequestedTimestamp() : input.getMeasuredAt(), reason);
	}

	public Measurement get(long id) {
		return this.measurements.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wynik nie istnieje"));
	}

	public List<Measurement> search(MeasurementSearchInput input) {
		return this.measurements.findBy(new MeasurementSearchFilter(input), (q) -> q.sortBy(input.getSort()).limit(input.getLimit()).all());
	}
}
