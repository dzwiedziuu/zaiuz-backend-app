package pl.aniedzwiedz.zaiuzbackendapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.aniedzwiedz.zaiuzbackendapp.security.TokenHasher;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = @Index(columnList = "seriesId"))
@Getter
@NoArgsConstructor
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	private String name;

	@NotNull
	private Long seriesId;

	@ManyToOne
	@JoinColumn(name = "seriesId", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Series series;

	@NotNull
	@Column(unique = true)
	@JsonIgnore
	@Getter(AccessLevel.NONE)
	private String keyHash;

	@NotNull
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter
	private Instant createdAt;

	public String generateKeyHash(TokenHasher hasher) {
		String apiKey = UUID.randomUUID().toString().replace("-", "");
		this.keyHash = hasher.hash(apiKey);
		return apiKey;
	}
}
