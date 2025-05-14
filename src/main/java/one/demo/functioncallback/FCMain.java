package one.demo.functioncallback;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Scanner;
import java.util.function.Function;

/**
 * FunctionCallbackApplication
 */
@SpringBootApplication
public class FCMain {
    public static void main(String[] args) {
        SpringApplication.run(FCMain.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nLet's chat!");
            System.out.print("\nUSER :> ");
            while (true) {
                ChatClient chatClient = chatClientBuilder.build();
                Flux<ChatResponse> responseStream = chatClient.prompt(new Prompt(new UserMessage(scanner.nextLine()))).toolNames("WeatherInfo").stream().chatResponse();
                System.out.print("\nASSISTANT :> ");
                responseStream.subscribe(resp -> {
                    if (resp.hasFinishReasons(Collections.singleton("STOP"))) {
                        System.out.print("\nUSER :> ");
                    } else {
                        System.out.print(resp.getResult().getOutput().getText());
                    }
                });
            }
        };
    }

    @Configuration
    static class WeatherConfig {
        @Bean
        public ToolCallback weatherInfo(Function<WeatherRequest, WeatherResponse> weatherInfoFunc) {
            return FunctionToolCallback.builder("WeatherInfo", weatherInfoFunc)
                    .description("Find the weather conditions, forecasts, and temperatures for a location, like a city or state.")
                    .inputType(WeatherRequest.class)
                    .build();
        }
    }

    @Service
    static class WeatherInfoFunc implements Function<WeatherRequest, WeatherResponse> {
        @Override
        public WeatherResponse apply(WeatherRequest weatherRequest) {
            double temperature = 10.0;
            if (weatherRequest.getLocation().contains("Paris")) {
                temperature = 15.0;
            }
            else if (weatherRequest.getLocation().contains("Tokyo")) {
                temperature = 10.0;
            }
            else if (weatherRequest.getLocation().contains("San Francisco")) {
                temperature = 30.0;
            }

            return new WeatherResponse(temperature, 15.0, 20.0, 2.0, 53, 45, Unit.C);
        }
    }
}


