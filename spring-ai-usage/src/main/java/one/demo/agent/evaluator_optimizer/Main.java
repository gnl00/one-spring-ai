package one.demo.agent.evaluator_optimizer;

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
        Implement a Stack in Java with:
        1. push(x)
        2. pop()
        3. getMin()
        All operations should be O(1).
        All inner fields should be private and when used should be prefixed with 'this.'.
        </user input>
        """;

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            EvaluatorOptimizer.RefinedResponse refinedResponse = new EvaluatorOptimizer(chatClientBuilder.build()).loop(input);

            System.out.printf("FINAL OUTPUT:\n\n%s\n%s", refinedResponse.solution(), refinedResponse.chainOfThought());
        };
    }
}
