# Spring AI MCP Stock STDIO Client

Server 有两种配置方式：

1、代码配置

```java
@Bean(destroyMethod = "close")
public McpSyncClient mcpClient() {
    var stdioParams = ServerParameters.builder("java")
            .args(
                    "-Dspring.ai.mcp.server.stdio=true",
                    "-Dspring.main.web-application-type=none",
                    // NOTE: You must disable the banner and the console logging to allow the STDIO transport to work !!!
                    "-Dspring.main.banner-mode=off",
                    "-Dlogging.pattern.console=",
                    "-jar",
                    "/Users/username/workspace/code/one-spring-ai/spring-ai-mcp-server-stdio-weather-server/target/spring-ai-mcp-server-stdio-weather-server-1.0-SNAPSHOT.jar"
            )
            .build();
    var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
            .requestTimeout(Duration.ofSeconds(30)).build();
    var init = mcpClient.initialize();
    System.out.println("MCP Initialized: " + init);
    System.out.println("Available Tools: " + mcpClient.listTools(null));
    return mcpClient;
}

@Bean
CommandLineRunner runner(ChatClient.Builder chatClientBuilder, McpSyncClient mcpClient, ConfigurableApplicationContext context) {
    return args -> {
        DefaultChatClient chatClient = (DefaultChatClient) chatClientBuilder
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClient))
                .build();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine();
            if (Objects.equals(input, "exit")) {
                break;
            }
            String content = chatClient.prompt(input).call().content();
            System.out.printf("\n=>%s%n", content);
        }
        context.close();
    };
}
```

自定义的 MCP Server 如果使用 Stdio 模式需要注意

> NOTE: You must disable the banner and the console logging to allow the STDIO transport to work !!!

2、配置文件

resources 目录下新增 mcp-servers-config.json 配置文件
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "/Users/gnl/workspace/code/one-spring-ai-mcp/weather-stdio-server/target/weather-stdio-server-1.0-SNAPSHOT.jar"
      ],
      "env": {
      }
    },
    "brave-search-server": {
      "command": "npx",
      "args": [],
      "env": {}
    }
  }
}
```
然后在 application 配置文件中新增配置
```properties
# 开启自动注入 ToolCallbackProvider
spring.ai.mcp.client.toolcallback.enabled=true
spring.ai.mcp.client.stdio.servers-configuration=classpath:mcp-servers-config.json
```

