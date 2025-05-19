package one.demo.agent.parallelization_worflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            String prompt = """
                Analyze how market changes will impact this stakeholder group.
                Provide specific impacts and recommended actions.
                Format with clear sections and priorities.
                """;

            List<String> inputs = List.of(
                    """
                        Customers:
                        - Price sensitive
                        - Want better tech
                        - Environmental concerns
                        """,

                    """
                        Employees:
                        - Job security worries
                        - Need new skills
                        - Want clear direction
                        """,

                    """
                        Investors:
                        - Expect growth
                        - Want cost control
                        - Risk concerns
                        """,

                    """
                        Suppliers:
                        - Capacity constraints
                        - Price pressures
                        - Tech transitions
                        """);

            // List<String> parallelResponse = new ParallelizationWorkflow(chatClientBuilder.defaultOptions(ChatOptions.builder().model("gpt-3.5-turbo").build()).build())
            List<String> parallelResponse = new ParallelizationWorkflow(chatClientBuilder.build())

                    .parallel(prompt, inputs, inputs.size());

            for (int i = 0; i < parallelResponse.size(); i++) {
                System.out.printf("\n=== PARALLEL WORKFLOW %d ===\n", i);
                System.out.println(parallelResponse.get(i));
            }
        };
    }
}
