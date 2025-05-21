package one.demo.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class EmbMain {
    public static void main(String[] args) {
        SpringApplication.run(EmbMain.class, args);
    }

    @Bean
    CommandLineRunner runner(EmbeddingModel embeddingModel) {
        return args -> {
            // 计算两段文本的意思是否相似
            EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
            System.out.println(embeddingResponse.getResult());
            // 返回的额向量数组需要额外计算才能得到的结果
            float[] output = embeddingResponse.getResult().getOutput();
            System.out.println(Arrays.toString(output));
        };
    }
}
