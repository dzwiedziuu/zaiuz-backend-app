package pl.aniedzwiedz.zaiuzbackendapp.service.measurement;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import pl.aniedzwiedz.zaiuzbackendapp.model.Measurement;

import java.util.ArrayList;
import java.util.List;

class MeasurementSearchFilter implements Specification<Measurement> {

	private final MeasurementSearchInput input;

	MeasurementSearchFilter(MeasurementSearchInput input) {
		this.input = input;
	}

	@Override
	public Predicate toPredicate(Root<Measurement> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<>();
		if (!this.input.getSeriesIds().isEmpty()) {
			predicates.add(root.get("seriesId").in(this.input.getSeriesIds()));
		}
		if (this.input.getFrom() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("measuredAt"), this.input.getFrom()));
		}
		if (this.input.getTo() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("measuredAt"), this.input.getTo()));
		}
		return cb.and(predicates.toArray(Predicate[]::new));
	}
}
