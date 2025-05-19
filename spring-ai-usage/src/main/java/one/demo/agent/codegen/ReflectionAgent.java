package one.demo.agent.codegen;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;


@Component
public class ReflectionAgent {

    private final ChatClient generateChatClient;

    private final ChatClient critiqueChatClient;

    public ReflectionAgent(ChatModel chatModel) {
        generateChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a Java programmer tasked with generating high quality Java code.
                        Your task is to Generate the best content possible for the user's request. If the user provides critique,
                        respond with a revised version of your previous attempt.
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(MessageWindowChatMemory.builder().build()))
                .build();

        critiqueChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are tasked with generating critique and recommendations to the user's generated content.
                        If the user content has something wrong or something to be improved, output a list of recommendations
                        and critiques. If the user content is ok and there's nothing to change, output this: <OK>
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(MessageWindowChatMemory.builder().build()))
                .build();
    }

    public void run(String userQuestion, int maxIterations) {
        System.out.print("\nAGENT :> ");
        String generation = generateChatClient.prompt(userQuestion).call().content();
        System.out.println("\n##generation\n" + generation);
        String critique;
        int iterations = 0;
        for (; iterations < maxIterations; iterations++) {
            critique = critiqueChatClient.prompt(generation).call().content();
            System.out.println("\n##Critique\n" + critique);
            if (critique.contains("<OK>")) {
                System.out.println("\n[Stop Generate]\n");
                break;
            }
            generation = generateChatClient.prompt(critique).call().content();
        }
        if (iterations != 0) {
            System.out.print("\nAGENT :> ");
            System.out.println(generation);
        }
    }

}
