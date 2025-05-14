package one.demo.functioncallback;

public record WeatherResponse(double temp, double feels_like, double temp_min, double temp_max, int pressure,
							  int humidity, Unit unit) {
}
