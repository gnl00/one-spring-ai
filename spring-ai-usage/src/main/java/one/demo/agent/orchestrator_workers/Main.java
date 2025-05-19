package one.demo.agent.orchestrator_workers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    String input = """
        <user input>
        Write a product description for a new eco-friendly bicycle
        </user input>
        """;

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            new OrchestratorWorkers(chatClientBuilder.build()).process(input);
        };
    }
}
