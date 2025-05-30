package one.demo.mcp.client.weather.sse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools, ChatMemory chatMemory, ConfigurableApplicationContext context) {
        return args -> {
            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultToolCallbacks(tools)
                    /*
                     * 聊天记忆
                     * @See https://java2ai.com/docs/1.0.0-M6.1/tutorials/chat-client/#%E8%81%8A%E5%A4%A9%E8%AE%B0%E5%BF%86
                     * MessageChatMemoryAdvisor：内存被检索并作为消息集合添加到提示中
                     * PromptChatMemoryAdvisor：检索内存并将其添加到提示的系统文本中。
                     *
                     * 日志记录
                     * @See https://java2ai.com/docs/1.0.0-M6.1/tutorials/chat-client/#%E6%97%A5%E5%BF%97%E8%AE%B0%E5%BD%95
                     * SimpleLoggerAdvisor 查看 LLM 请求和响应日志记录，需要配置 org.springframework.ai.chat.client.advisor=DEBUG
                     */
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
