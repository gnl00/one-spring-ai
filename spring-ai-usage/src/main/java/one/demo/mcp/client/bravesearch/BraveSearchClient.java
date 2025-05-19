package one.demo.mcp.client.bravesearch;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Objects;
import java.util.Scanner;

/**
 * IT WORKS!!!
 */
@SpringBootApplication
public class BraveSearchClient {
    public static void main(String[] args) {
        SpringApplication.run(BraveSearchClient.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
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
    }

}
