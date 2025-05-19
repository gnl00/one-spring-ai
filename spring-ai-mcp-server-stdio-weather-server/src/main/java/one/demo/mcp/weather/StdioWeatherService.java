package one.demo.mcp.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class StdioWeatherService {

    public record WeatherRequest(
            @JsonProperty(required = true, value = "location")
            @JsonPropertyDescription("The city and state e.g. San Francisco, CA")
            String location
    ) {}

    public record WeatherResponse(double temp, double feels_like, double temp_min, double temp_max, int pressure, int humidity) {
    }

    @Tool(description = "Get weather forecast for a specific city")
    public String getWeatherForecast(WeatherRequest weatherRequest) {
        double temperature = 10.0;
        if (weatherRequest.location().contains("Paris")) {
            temperature = 15.0;
        }
        else if (weatherRequest.location().contains("Tokyo")) {
            temperature = 18.0;
        }
        else if (weatherRequest.location().contains("San Francisco")) {
            temperature = 30.0;
        }
        return new WeatherResponse(temperature, 15.0, 20.0, 2.0, 53, 45).toString();
    }

    public static void main(String[] args) {
        StdioWeatherService weatherService = new StdioWeatherService();
        System.out.println(weatherService.getWeatherForecast(new WeatherRequest("San Francisco")));
    }
}
