package one.demo.rag._1simple;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * 1、文本 embedding 存到数据库
 * 2、检索数据库
 * 3、对比向量数据+生成结果+校准结果
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /*@Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 输入检索文档
        vectorStore.add(List.of(
                new Document("产品说明书:产品名称：智能机器人\n" +
                "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
                "功能：\n" +
                "1. 自动导航：机器人能够自动导航到指定位置。\n" +
                "2. 自动抓取：机器人能够自动抓取物品。\n" +
                "3. 自动放置：机器人能够自动放置物品。\n")));

        return vectorStore;
    }*/

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ChatMemory chatMemory, ConfigurableApplicationContext context) {
        return args -> {

            // Retrieve documents similar to a query
            /*SearchRequest request = SearchRequest.builder().query("Spring").topK(5).build();
            List<Document> results = vectorStore.similaritySearch(request);
            System.out.println(results);*/

            // 输入检索文档
            /*vectorStore.add(List.of(
                    new Document("产品说明书:产品名称：智能机器人\n" +
                            "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
                            "功能：\n" +
                            "1. 自动导航：机器人能够自动导航到指定位置。\n" +
                            "2. 自动抓取：机器人能够自动抓取物品。\n" +
                            "3. 自动放置：机器人能够自动放置物品。\n")));*/

            /*List<Document> documents = List.of(
                    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                    new Document("The World is Big and Salvation Lurks Around the Corner"),
                    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
            vectorStore.add(documents);*/

            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultSystem("你将作为一名机器人产品的专家，对于用户的使用需求作出解答")
                    .defaultAdvisors(
                            new PromptChatMemoryAdvisor(chatMemory),
                            new SimpleLoggerAdvisor(),
                            // 能不能从数据库里召回数据靠的是 QuestionAnswerAdvisor
                            // Context for the question is retrieved from a Vector Store and added to the prompt's user text.
                            QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().similarityThreshold(0.6d).topK(3).build()).build()
                    )
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
