package pl.aniedzwiedz.zaiuzbackendapp.service;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.model.Series;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SeriesRepository;
import pl.aniedzwiedz.zaiuzbackendapp.validation.EntityValidator;

import java.util.List;

@Service
public class SeriesService {

	private final SeriesRepository series;

	private final EntityValidator validator;

	SeriesService(SeriesRepository series, EntityValidator validator) {
		this.series = series;
		this.validator = validator;
	}

	public List<Series> list() {
		return this.series.findAll(Sort.by("id"));
	}

	public Series get(long id) {
		return this.series.findById(id).orElseThrow(SeriesService::notFound);
	}

	@Transactional
	public Series create(Series input) {
		return this.series.save(this.validator.validate(input));
	}

	@Transactional
	public Series update(long id, Series input) {
		ensureExists(id);
		input.setId(id);
		this.validator.validate(input);
		return this.series.save(input);
	}

	@Transactional
	public void delete(long id) {
		ensureExists(id);
		this.series.deleteById(id);
	}

	private void ensureExists(long id) {
		if (!this.series.existsById(id)) {
			throw notFound();
		}
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Seria nie istnieje");
	}
}
