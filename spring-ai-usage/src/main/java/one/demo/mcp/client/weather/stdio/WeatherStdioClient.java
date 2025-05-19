package one.demo.mcp.client.weather.stdio;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
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
public class WeatherStdioClient {
    public static void main(String[] args) {
        SpringApplication.run(WeatherStdioClient.class, args);
    }

    /*@Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultOptions(ChatOptions.builder().model("claude-3-7-sonnet-latest").build())
                    .defaultToolCallbacks(tools)
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
    }*/

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient() {
        // based on
        // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder("java")
                .args(
                        "-Dspring.ai.mcp.server.stdio=true",
                        "-Dspring.main.web-application-type=none",
                        // NOTE: You must disable the banner and the console logging to allow the STDIO transport to work !!!
                        "-Dspring.main.banner-mode=off",
                        "-Dlogging.pattern.console=",
                        "-jar",
                        "/Users/gnl/workspace/code/one-spring-ai/spring-ai-mcp-server-stdio-weather-server/target/spring-ai-mcp-server-stdio-weather-server-1.0-SNAPSHOT.jar"
                )
                .build();
        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(30)).build();
        var init = mcpClient.initialize();
        System.out.println("MCP Initialized: " + init);
        System.out.println("Available Tools: " + mcpClient.listTools(null));
        return mcpClient;
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, McpSyncClient mcpClient, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    // .defaultOptions(ChatOptions.builder().model("claude-3-7-sonnet-latest").build())
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClient))
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
