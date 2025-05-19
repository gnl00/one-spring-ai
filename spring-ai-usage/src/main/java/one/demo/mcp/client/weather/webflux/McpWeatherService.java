//package one.demo.mcp.weather_server.webflux;
//
//import one.demo.functioncallback.Unit;
//import one.demo.functioncallback.WeatherRequest;
//import one.demo.functioncallback.WeatherResponse;
//import org.springframework.ai.tool.annotation.Tool;
//import org.springframework.stereotype.Service;
//
//import java.util.function.Function;
//
//@Service
//public class McpWeatherService implements Function<WeatherRequest, WeatherResponse> {
//    @Tool(description = "Get weather forecast for a specific city")
//    @Override
//    public WeatherResponse apply(WeatherRequest weatherRequest) {
//        double temperature = 10.0;
//        if (weatherRequest.getLocation().contains("Paris")) {
//            temperature = 15.0;
//        }
//        else if (weatherRequest.getLocation().contains("Tokyo")) {
//            temperature = 18.0;
//        }
//        else if (weatherRequest.getLocation().contains("San Francisco")) {
//            temperature = 30.0;
//        }
//        return new WeatherResponse(temperature, 15.0, 20.0, 2.0, 53, 45, Unit.C);
//    }
//}
