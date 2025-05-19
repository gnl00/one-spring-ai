package one.demo.streaming;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Scanner;

@RestController
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * Streaming in command line
     */
    @Bean
    CommandLineRunner runner(OpenAiChatModel chatModel) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nLet's chat!");
            System.out.print("\nUSER :> ");
            while (true) {
                // Flux<String> stream = chatModel.stream(new UserMessage(scanner.nextLine()));
                Flux<ChatResponse> responseStream = chatModel.stream(new Prompt(new UserMessage(scanner.nextLine())));
                System.out.print("\nASSISTANT :> ");
                responseStream.subscribe(resp -> {
                    if (resp.hasFinishReasons(Collections.singleton("STOP"))) {
                        System.out.print("\nUSER :> ");
                    } else {
                        System.out.print(resp.getResult().getOutput().getText());
                    }
                    // System.out.println("\nhasFinishReasons = " + resp.hasFinishReasons(Collections.singleton("STOP")));
                });
            }
        };
    }

}
