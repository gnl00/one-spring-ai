# 检索增强生成 RAG（Retrieval-Augmented Generation）

> 一种结合信息检索和文本生成的技术范式。

RAG 技术就像给AI装上了「实时百科大脑」，通过先查资料后回答的机制，让AI摆脱传统模型的”知识遗忘”困境。

## 核心步骤

- 文档切割
- 向量编码（Embedding）
- 相似检索
- 生成增强

### 1.文档切割 → 建立智能档案库
将海量文档转化为易检索的知识碎片

### 2. 向量编码 → 构建语义地图

- 用AI模型将文字转化为数学向量，所有向量存入专用数据库
- 利用数学特征从相近的内容中寻找最相似的片段，建立快速检索索引（类似图书馆书目检索系统）

> 示例效果：“续航时间”和”电池容量”会被编码为相似向量

### 3. 相似检索 → 智能资料猎人

1、将用户问题转为【问题向量】
2、通过多维度匹配策略搜索知识库：
- 语义相似度
- 关键词匹配度
- 时效性权重
3、输出指定个数最相关文档片段

### 4. 生成增强 → 专业报告撰写

1、将检索结果作为指定参考资料
2、AI生成时自动关联相关知识片段
3、输出形式可以包含：
- 自然语言回答
- 附参考资料溯源路径

## 依赖

两个关键类 VectorStore 和 SimpleVectorStore 位于以下依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-rag</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-advisors-vector-store</artifactId>
    </dependency>
</dependencies>
```

## 简单查询

1、需要创建一个 PG 数据库，并添加 Vector 支持插件。（示例中直接使用了 supabase，自带 pgvector 插件，直接连接即可使用）

2、输入文档，直接添加到 VectorStore 中。例子比较简单，不需要进行切割操作

```java
vectorStore.add(List.of(
    new Document("产品说明书:产品名称：智能机器人\n" +
    "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
    "功能：\n" +
    "1. 自动导航：机器人能够自动导航到指定位置。\n" +
    "2. 自动抓取：机器人能够自动抓取物品。\n" +
    "3. 自动放置：机器人能够自动放置物品。\n")));
```

3、接来下就到 vector-store-pgvector 发挥作用了，spring-ai 利用向量模型，比如 openai/text-embedding-ada-002 将文档转化为向量，并保存到向量数据库中。

数据表结构如下

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    -- 1536 is the default embedding dimension 注意这里的向量维度，需要和 spring.ai.vectorstore.pgvector.dimensions 这个配置一样
    -- 如果没有设置 vector(1536)，仅使用 embedding vector 的话 pgvector 默认使用的向量维度是 768，同样的 spring.ai.vectorstore.pgvector.dimensions 也需要设置成 768
    embedding vector(1536)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
```

数据内容如下

```sql
INSERT INTO "public"."vector_store" ("id", "content", "metadata", "embedding") VALUES ('5b8af274-2a05-44af-809d-b8b577936c2c', '产品说明书:产品名称：智能机器人
产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
功能：
1. 自动导航：机器人能够自动导航到指定位置。
2. 自动抓取：机器人能够自动抓取物品。
3. 自动放置：机器人能够自动放置物品。
', '{}', '[-0.024051972,-0.007835817,-0.0027109932,0.008798692,-0.026787868,0.013081829,-0.012875973,0.009827973,...]');
```

4、当我们发送一个请求检索时，spring-ai 会通过向量模型，将用户问题转化为向量，并使用向量数据库进行检索。

```sql
SELECT *, embedding <=> ? AS distance FROM public.vector_store WHERE embedding <=> ? < ?  ORDER BY distance LIMIT ? 
```

这个 SQL 查询语句使用了 向量相似度搜索 的语法，`<=>` 表示 余弦距离（cosine distance） 运算符，它用于计算两个向量之间的相似性（越小越相似）。

```sql
embedding <=> '[1,2,3]' -- 表示当前行的 embedding 向量与 [1,2,3] 的余弦距离。
```

这条 SQL 的意思就是返回 public.vector_store 表中，从数据库找到与 输入的embedding向量 的余弦距离小于 ? 的记录。

5、找到相似性高的结果后将对应的结果塞到 prompt 中，让 LLM 判断是否需要使用这些结果进行回答，prompt 内容大概如下，prompt 定义在 QuestionAnswerAdvisor 中

```text
Context information is below, surrounded by ---------------------

---------------------
产品说明书:产品名称：智能机器人
产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
功能：
1. 自动导航：机器人能够自动导航到指定位置。
2. 自动抓取：机器人能够自动抓取物品。
3. 自动放置：机器人能够自动放置物品。

Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!
The World is Big and Salvation Lurks Around the Corner
---------------------

Given the context and provided history information and not prior knowledge,
reply to the user comment. If the answer is not in the context, inform
the user that you can't answer the question.
, systemText=你将作为一名机器人产品的专家，对于用户的使用需求作出解答

Use the conversation memory from the MEMORY section to provide accurate answers.

---------------------
MEMORY:
{memory}
---------------------
```

6、拿到 LLM 响应之后再根据记忆进行校准，prompt 大概如下，记忆 prompt 定义在 PromptChatMemoryAdvisor 中

```text
Use the conversation memory from the MEMORY section to provide accurate answers.

---------------------
MEMORY:
---------------------

, role=SYSTEM, name=null, toolCallId=null, toolCalls=null, refusal=null, audioOutput=null], ChatCompletionMessage[rawContent=智能机器人产品有什么功能？

Context information is below, surrounded by ---------------------

---------------------
产品说明书:产品名称：智能机器人
产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
功能：
1. 自动导航：机器人能够自动导航到指定位置。
2. 自动抓取：机器人能够自动抓取物品。
3. 自动放置：机器人能够自动放置物品。

Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!
The World is Big and Salvation Lurks Around the Corner
---------------------

Given the context and provided history information and not prior knowledge,
reply to the user comment. If the answer is not in the context, inform
the user that you can't answer the question.
```

7、最后得到的结果大概率就能符合预期了

```text
智能机器人产品具有以下功能：

1. **自动导航**：能够自动导航到指定位置。
2. **自动抓取**：能够自动抓取物品。
3. **自动放置**：能够自动放置物品。

如果您有其他问题或需要了解更多功能，请告诉我！
```

> 这里出现的 QuestionAnswerAdvisor/PromptChatMemoryAdvisor 中的 Advisor 可以理解成“增强器”或者“拦截器”

## 文档检索

1、需要文档解析依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-tika-document-reader</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-markdown-document-reader</artifactId>
    </dependency>
</dependencies>
```

2、注意配置 `spring.ai.vectorstore.pgvector.dimensions`

```sql
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    -- 1536 is the default embedding dimension 注意这里的向量维度，需要和 spring.ai.vectorstore.pgvector.dimensions 这个配置一样
    -- 如果没有设置 vector(1536)，仅使用 embedding vector 的话 pgvector 默认使用的向量维度是 768，同样的 spring.ai.vectorstore.pgvector.dimensions 也需要设置成 768
    embedding vector(1536)
);
```

## Advance功能

### 查询重写

Spring AI提供了RewriteQueryTransformer来实现查询重写功能。查询重写的主要优势：查询明确化，可以将模糊的问题转换为具体的查询点。

这种转换不仅有助于系统检索到更相关的文档，还能帮助生成更全面和专业的回答。 依靠 prompt 来完成

```text
Given a user query, rewrite it to provide better results when querying a {target}.
Given a user query, rewrite it to provide better results when querying a vector store.
Remove any irrelevant information, and ensure the query is concise and specific.

Original query:
我正在学习人工智能，什么是大语言模型？

Rewritten query:
```

输出

```text
大语言模型是什么？
```

### 查询翻译

将用户的查询从一种语言翻译成另一种语言。

```text
Given a user query, translate it to {targetLanguage}.
If the query is already in {targetLanguage}, return it unchanged.
If you don't know the language of the query, return it unchanged.
Do not add explanations nor any other text.

Original query: {query}

Translated query:
```

### 上下文感知查询

```text
Given the following conversation history and a follow-up query, your task is to synthesize
a concise, standalone query that incorporates the context from the history.
Ensure the standalone query is clear, specific, and maintains the user's intent.

Conversation history:
USER: 深圳市南山区的碧海湾小区在哪里?
ASSISTANT: 碧海湾小区位于深圳市南山区后海中心区，临近后海地铁站。

Follow-up query:
那这个小区的二手房均价是多少?

Standalone query:
```

### 文档合并器

ConcatenationDocumentJoiner 合并来自不同数据源的文档

```java
public class ConcatenationDocumentJoiner implements DocumentJoiner {
	@Override
	public List<Document> join(Map<Query, List<List<Document>>> documentsForQuery) {
		return new ArrayList<>(documentsForQuery.values()
			.stream()
			.flatMap(List::stream)
			.flatMap(List::stream)
			.collect(Collectors.toMap(Document::getId, Function.identity(), (existing, duplicate) -> existing))
			.values());
	}
}
```

### RetrievalAugmentationAdvisor

RetrievalAugmentationAdvisor是Spring AI提供的一个强大工具，它能够自动化地处理文档检索和查询增强过程。这个顾问组件将文档检索与查询处理无缝集成，使得AI助手能够基于检索到的相关文档提供更准确的回答。

1、基础用法

```java
// 1. 初始化向量存储
SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
        .build();

// 2. 添加文档到向量存储
List<Document> documents = List.of(
        new Document("产品说明书:产品名称：智能机器人\n" +
                "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
                "功能：\n" +
                "1. 自动导航：机器人能够自动导航到指定位置。\n" +
                "2. 自动抓取：机器人能够自动抓取物品。\n" +
                "3. 自动放置：机器人能够自动放置物品。\n"));
vectorStore.add(documents);

// 3. 创建检索增强顾问
Advisor advisor = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .build())
        .build();

// 4. 在聊天客户端中使用顾问
String response = chatClient.prompt()
        .user("机器人有哪些功能？")
        .advisors(advisor)  // 添加检索增强顾问
        .call()
        .content();
```

2、高级配置

```java
Advisor advisor = RetrievalAugmentationAdvisor.builder()
    // 配置查询增强器
    .queryAugmenter(ContextualQueryAugmenter.builder()
            .allowEmptyContext(true)        // 允许空上下文查询
            .build())
    // 配置文档检索器
    .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.5)       // 相似度阈值
            .topK(3)                        // 返回文档数量
            .filterExpression(new FilterExpressionBuilder()
                    .eq("genre", "fairytale")
                    .build())     // 文档过滤表达式
            .build())
    .build();
```

### DocumentSelection

> 文档选择

1、文档结构设计，一个结构良好的文档示例：

```java
// 生成室内设计案例文档
List<Document> documents = new ArrayList<>();

// 现代简约风格客厅案例
documents.add(new Document(
    "案例编号：LR-2023-001\n" +
        "项目概述：180平米大平层现代简约风格客厅改造\n" +
        "设计要点：\n" +
        "1. 采用5.2米挑高的落地窗，最大化自然采光\n" +
        "2. 主色调：云雾白(哑光，NCS S0500-N)配合莫兰迪灰\n" +
        "3. 家具选择：意大利B&B品牌真皮沙发，北欧白橡木茶几\n" +
        "4. 照明设计：嵌入式筒灯搭配意大利Flos吊灯\n" +
        "5. 软装配饰：进口黑胡桃木电视墙，几何图案地毯\n" +
        "空间效果：通透大气，适合商务接待和家庭日常起居",
    Map.of(
        "type", "interior",    // 文档类型
        "year", "2023",        // 年份
        "month", "06",         // 月份
        "location", "indoor",   // 位置类型
        "style", "modern",      // 装修风格
        "room", "living_room"   // 房间类型
)));
```

2、高级检索实现

```java
// 1. 初始化向量存储
SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
        .build();

// 2. 配置AI助手角色
ChatClient chatClient = builder
        .defaultSystem("你是一位专业的室内设计顾问，精通各种装修风格、材料选择和空间布局。请基于提供的参考资料，为用户提供专业、详细且实用的建议。在回答时，请注意：\n" +
                "1. 准确理解用户的具体需求\n" +
                "2. 结合参考资料中的实际案例\n" +
                "3. 提供专业的设计理念和原理解释\n" +
                "4. 考虑实用性、美观性和成本效益\n" +
                "5. 如有需要，可以提供替代方案")
        .build();

// 3. 构建复杂的文档过滤条件
var b = new FilterExpressionBuilder();
var filterExpression = b.and(
        b.and(
                b.eq("year", "2023"),         // 筛选2023年的案例
                b.eq("location", "indoor")),   // 仅选择室内案例
        b.and(
                b.eq("type", "interior"),      // 类型为室内设计
                b.in("room", "living_room", "study", "kitchen")  // 指定房间类型
));

// 4. 配置文档检索器
DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .similarityThreshold(0.5)    // 设置相似度阈值
        .topK(3)                     // 返回前3个最相关的文档
        .filterExpression(filterExpression.build())
        .build();

// 5. 创建上下文感知的查询增强器
Advisor advisor = RetrievalAugmentationAdvisor.builder()
        .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build())
        .documentRetriever(retriever)
        .build();

// 6. 执行查询并获取响应
String userQuestion = "根据已经提供的资料，请描述所有相关的场景风格，输出案例编号，尽可能详细地描述其内容。";
String response = chatClient.prompt()
        .user(userQuestion)
        .advisors(advisor)
        .call()
        .content();
```

### 错误处理和边界情况

通过使用 ContextualQueryAugmenter，我们可以实现更友好的错误处理机制：

```java
// 1. 构建检索增强顾问
Advisor advisor = RetrievalAugmentationAdvisor.builder()
        .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)  // 允许空上下文，避免NPE
                .build())
        .documentRetriever(retriever)
        .build();

// 2. 执行查询并处理可能的异常
return chatClient.prompt()
        .user(query)
        .advisors(advisor)
        .call()
        .getContent();
```

修改前回答结果：
```text
AI回答：I'm sorry, but it appears that the specific details or references you mentioned for your interior design query are not included in my current knowledge base. 
To provide you with the best possible advice, I would need more information about your project, 
such as the style you're aiming for, the size of the space, your budget, and any specific elements you want to include or avoid. 
If you can provide more details, I would be more than happy to offer tailored advice on interior design, space planning, material selection, and more.
```

修改后回答结果：
```text
AI回答：很抱歉，您没有提供具体的参考资料或案例编号。为了能够提供详细的场景风格描述，我需要您提供具体的案例编号或者相关资料。
一旦您提供了这些信息，我将能够准确地描述相关的场景风格，包括以下内容：

1. 设计风格和主题
2. 空间布局和功能规划
3. 材料选择和色彩搭配
4. 灯光设计和氛围营造
5. 家具配置和软装搭配
```


## Reference

- [spring-ai-pgvector] (https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)

- [spring-ai-rag] (https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)

- https://java2ai.com/docs/1.0.0-M6.1/tutorials/rag/