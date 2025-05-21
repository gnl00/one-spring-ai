package one.demo.rag._2advance;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Query Rewrite (查询重写)
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder, ConfigurableApplicationContext context) {
        return args -> {
            /*
             * 查询重写
             * 查询明确化，可以将模糊的问题转换为具体的查询点
             */
            /*Query query = new org.springframework.ai.rag.Query("我正在学习人工智能，什么是大语言模型？"); // 创建一个模拟用户学习AI的查询场景
            // 创建查询重写转换器
            RewriteQueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder)
                    .build();
            // 执行查询重写
            Query transformedQuery = queryTransformer.transform(query);

            // 输出重写后的查询
            System.out.println(transformedQuery.text()); // 重写后的查询：大语言模型是什么？*/

            /*
             * **查询翻译**
             * 将用户的查询从一种语言翻译成另一种语言
             */
            /*TranslationQueryTransformer translationQueryTransformer = TranslationQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder)
                    .targetLanguage("english")
                    .build();
            Query translationTransformed = translationQueryTransformer.transform(transformedQuery);
            System.out.println(translationTransformed.text());*/

            /*
             * **上下文感知查询**
             * 1.消除歧义 2.保留上下文 3.提高准确性
             */
            Query query = Query.builder()
                    .text("那这个小区的二手房均价是多少?")  // 当前用户的提问
                    .history(new UserMessage("深圳市南山区的碧海湾小区在哪里?"),  // 历史对话中用户的问题
                            new AssistantMessage("碧海湾小区位于深圳市南山区后海中心区，临近后海地铁站。"))  // AI的回答
                    .build();
            QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder)
                    .build();

            // 执行查询转换
            // 将模糊的代词引用（"这个小区"）转换为明确的实体名称（"碧海湾小区"）
            Query transformedQuery = queryTransformer.transform(query);

            System.out.println(transformedQuery.text());

            context.close();
            System.exit(0);
        };
    }
}
