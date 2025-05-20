package one.demo.mcp.client.weather.sse;

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

@SpringBootApplication
public class WeatherSSEClient {
    public static void main(String[] args) {
        SpringApplication.run(WeatherSSEClient.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
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
    }
}
