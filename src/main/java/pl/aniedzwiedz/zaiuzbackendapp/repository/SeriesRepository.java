package pl.aniedzwiedz.zaiuzbackendapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.aniedzwiedz.zaiuzbackendapp.model.Series;

public interface SeriesRepository extends JpaRepository<Series, Long> {
}
