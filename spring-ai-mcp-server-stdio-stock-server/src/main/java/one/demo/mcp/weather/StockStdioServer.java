package one.demo.mcp.weather;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StockStdioServer {
    public static void main(String[] args) {
        SpringApplication.run(StockStdioServer.class, args);
    }

    @Bean
    ToolCallbackProvider weatherTool(StockService service) {
        return MethodToolCallbackProvider.builder().toolObjects(service).build();
    }
}
