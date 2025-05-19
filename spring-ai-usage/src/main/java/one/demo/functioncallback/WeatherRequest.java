package one.demo.functioncallback;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("Weather API request")
public class WeatherRequest {

	@JsonProperty(required = true, value = "location")
	@JsonPropertyDescription("The city and state e.g. San Francisco, CA")
	private String location = "";

	@JsonProperty(required = true, value = "lat")
	@JsonPropertyDescription("The city latitude")
	private double lat = 0.0;

	@JsonProperty(required = true, value = "lon")
	@JsonPropertyDescription("The city longitude")
	private double lon = 0.0;

	@JsonProperty(required = true, value = "unit")
	@JsonPropertyDescription("Temperature unit")
	private Unit unit = Unit.C;

}

