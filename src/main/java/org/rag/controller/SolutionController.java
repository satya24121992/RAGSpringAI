package org.rag.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SolutionController {
    private final static Logger LOG = LoggerFactory.getLogger(SolutionController.class);
    private final ChatClient chatClient;
    private final VectorStore store;
    private final RewriteQueryTransformer.Builder rqtBuilder;

    public SolutionController(ChatClient.Builder chatClientBuilder,VectorStore store) {
        this.store=store;
        this.rqtBuilder = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder);

        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/get-solution")
    String getSolution(){
        PromptTemplate pt = new PromptTemplate("""
            {query}.
            Match query with issue description and Get only the Solution from the response.{target}
            """);

        Prompt p = pt.create(
                Map.of("query", "null pointer error","target","")
        );

        return this.chatClient.prompt(p)
                .advisors(new QuestionAnswerAdvisor(store))
                .call()
                .content();
        /*Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.7)
                        .topK(3)
                        .vectorStore(store)
                        .build())
                .queryTransformers(rqtBuilder.promptTemplate(pt).build())
                .build();

        return this.chatClient.prompt(p)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content();*/
    }

}
