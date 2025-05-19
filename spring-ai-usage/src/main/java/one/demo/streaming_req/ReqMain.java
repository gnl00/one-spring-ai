package one.demo.streaming_req;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * StreamingWebRequestApplication
 */
@RestController
@SpringBootApplication
public class ReqMain {
    public static void main(String[] args) {
        SpringApplication.run(ReqMain.class, args);
    }

    @Autowired OpenAiChatModel chatModel;

    /**
     * Streaming api response
     */
    @GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateStream(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        // return chatModel.stream(new Prompt(new UserMessage(message), ChatOptions.builder().model("gpt-4o-mini").build()));
        return chatModel.stream(new Prompt(new UserMessage(message)));
    }

}
