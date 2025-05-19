package one.demo.mcp.weather;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeatherStdioServer {
    public static void main(String[] args) {
        SpringApplication.run(WeatherStdioServer.class, args);
    }

    /*@Bean
    ToolCallbackProvider weatherTool(StdioWeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }*/

    @Bean
    ToolCallbackProvider weatherTool(OpenMeteoWeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }
}
