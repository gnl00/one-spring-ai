package one.demo.agent.codegen;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * Code generate agent of Reflection Pattern
 * @see <a href="https://github.com/neural-maze/agentic-patterns-course">code agentic patterns</a>
 */
@SpringBootApplication
public class CodeGenMain {
    public static void main(String[] args) {
        SpringApplication.run(CodeGenMain.class, args);
    }

    /**
     * Streaming in command line
     */
    @Bean
    CommandLineRunner runner(ReflectionAgent reflectionAgent) {
        return args -> {
            System.out.println("\nLet's chat!");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nUSER :> ");
                reflectionAgent.run(scanner.nextLine(), 2);
            }
        };
    }
}
