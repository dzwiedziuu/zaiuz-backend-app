package pl.aniedzwiedz.zaiuzbackendapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.aniedzwiedz.zaiuzbackendapp.model.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long>, JpaSpecificationExecutor<Measurement> {

	@Query("select count(m) > 0 from Measurement m where m.seriesId = :seriesId and (m.value < :min or m.value > :max)")
	boolean existsOutsideRange(@Param("seriesId") long seriesId, @Param("min") double min, @Param("max") double max);
}
