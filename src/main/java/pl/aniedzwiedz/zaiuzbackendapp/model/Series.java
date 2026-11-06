package pl.aniedzwiedz.zaiuzbackendapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class Series {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter
	private Long id;

	@NotBlank
	@Size(max = 100)
	private String name;

	@NotNull
	private Double minValue;

	@NotNull
	private Double maxValue;

	@NotNull
	@Pattern(regexp = "#[0-9A-Fa-f]{6}")
	private String color;

	@Size(max = 100)
	private String icon;

	@Size(max = 20)
	private String unit;

	@AssertTrue(message = "MinValue musi być mniejsze od MaxValue")
	boolean isRangeValid() {
		return this.minValue == null || this.maxValue == null || this.minValue < this.maxValue;
	}
}
