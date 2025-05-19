package one.demo.mcp.client.filesystem;

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

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.Scanner;

/**
 * IT WORKS!!!
 */
@SpringBootApplication
public class FileSystemStdioClient {
    public static void main(String[] args) {
        SpringApplication.run(FileSystemStdioClient.class, args);
    }

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient() {
        // based on
        // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", getDbPath())
                .build();
        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(20))
                .build();
        var init = mcpClient.initialize();
        System.out.println("MCP Initialized: " + init);
        System.out.println("Available Tools: " + mcpClient.listTools(null));
        return mcpClient;
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, McpSyncClient mcpClient, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
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

    /*@Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultOptions(ChatOptions.builder().model("claude-3-7-sonnet-latest").build())
                    .defaultTools(tools)
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
        };
    }*/

    private static String getDbPath() {
        return Paths.get(System.getProperty("user.dir")).toString();
    }
}
