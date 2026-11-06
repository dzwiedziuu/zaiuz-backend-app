package pl.aniedzwiedz.zaiuzbackendapp.service.measurement;

import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.parser.TimestampParser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class MeasurementSearchInput {

	private static final int DEFAULT_LIMIT = 1000;
	private static final int MAX_LIMIT = 10000;

	private final List<Long> seriesIds;
	private final Instant from;
	private final Instant to;
	private final Sort sort;
	private final int limit;

	public MeasurementSearchInput(String series, OffsetDateTime from, OffsetDateTime to, String sort, Integer limit, TimestampParser timestamps) {
		this.seriesIds = parseSeriesIds(series);
		this.from = (from != null) ? timestamps.supported(from, "from") : null;
		this.to = (to != null) ? timestamps.supported(to, "to") : null;
		if (this.from != null && this.to != null && this.from.isAfter(this.to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from nie może być późniejsze niż to");
		}
		this.sort = parseSort(sort);
		this.limit = parseLimit(limit);
	}

	private static List<Long> parseSeriesIds(String param) {
		if (param == null || param.isBlank()) {
			return List.of();
		}
		try {
			return Arrays.stream(param.split(",")).map(String::trim).map(Long::valueOf).toList();
		}
		catch (NumberFormatException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "series musi być listą identyfikatorów rozdzielonych przecinkami");
		}
	}

	private static Sort parseSort(String param) {
		if (param == null || param.equals("timestamp")) {
			return Sort.by(Sort.Order.asc("measuredAt"), Sort.Order.asc("id"));
		}
		if (param.equals("-timestamp")) {
			return Sort.by(Sort.Order.desc("measuredAt"), Sort.Order.desc("id"));
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort musi mieć wartość timestamp lub -timestamp");
	}

	private static int parseLimit(Integer param) {
		int limit = param != null ? param : DEFAULT_LIMIT;
		if (limit < 1 || limit > MAX_LIMIT) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit musi mieścić się w zakresie 1-10000");
		}
		return limit;
	}
}
