package one.demo.mcp.client.weather.sse;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Objects;
import java.util.Scanner;

@SpringBootApplication
public class WeatherSSEClient {
    public static void main(String[] args) {
        SpringApplication.run(WeatherSSEClient.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    // .defaultOptions(ChatOptions.builder().model("claude-3-7-sonnet-latest").build())
                    // .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClient))
                    .build();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n> ");
                String input = scanner.nextLine();
                if (Objects.equals(input, "exit")) {
                    break;
                }
                String content = chatClient.prompt(input).call().content();
                System.out.printf("\n=>%s%n", content);
            }
            context.close();
            System.exit(0);
        };
    }
}
