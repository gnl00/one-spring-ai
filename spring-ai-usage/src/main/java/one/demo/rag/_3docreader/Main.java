package one.demo.rag._3docreader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * 0、文档解析
 * 1、文本 embedding 存到数据库
 * 2、检索数据库
 * 3、对比向量数据+生成结果+校准结果
 */
@Slf4j
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /*@Value("${rag.doc:SJPlatform_Dev_Manual.html}")
    Resource docResource;*/

    @Value("${rag.doc.path:classpath:rag/markdown/overview.md}")
    String filePath;

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ChatMemory chatMemory, ConfigurableApplicationContext context) {
        return args -> {
            MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(
                    filePath,
                    MarkdownDocumentReaderConfig.builder().withIncludeCodeBlock(true).withIncludeBlockquote(true).build());
            // TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(docResource);
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(2000, 1024, 10, 10000, true);
            log.info("Loading document into vector store");
            // NOTE: spring.ai.vectorstore.pgvector.dimensions 需要设置成 768
            //  tokenTextSplitter.apply 才能正常工作！
            vectorStore.accept(tokenTextSplitter.apply(markdownDocumentReader.get()));
            log.info("Documents loaded into vector store");

            // vectorStore.delete(List.of("26db18d7-37a5-4917-906c-ff091e648888", "a6f48be6-33f7-4454-aa82-6bd391b31465"));

            DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                    .defaultSystem("你将作为一名精通 Spring AI 的技术专家，对于用户的使用需求作出解答")
                    .defaultAdvisors(
                            new PromptChatMemoryAdvisor(chatMemory),
                            new SimpleLoggerAdvisor(),
                            // 能不能从数据库里召回数据靠的是 QuestionAnswerAdvisor
                            // Context for the question is retrieved from a Vector Store and added to the prompt's user text.
                            QuestionAnswerAdvisor.builder(vectorStore).build()
                    )
                    .build();

            /**
             * 文档 Loaded 之后可以问
             * - Spring AI Alibaba 的官网地址？
             * - Spring AI Alibaba 的作者是谁？
             * LLM 就会从文档中解析对应的内容来回答
             */

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
