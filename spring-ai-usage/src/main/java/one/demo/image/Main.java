package one.demo.image;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Objects;
import java.util.Scanner;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                    .build();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nQ:> ");
                String input = scanner.nextLine();
                if (Objects.equals(input, "exit")) {
                    break;
                }
                String content = chatClient.prompt(input).call().content();
                System.out.printf("\nA:=>\n%s", content);
            }
            context.close();
            System.exit(0);
        };
    }
}
