package org.rag.controller;

import org.rag.vo.ResponseMessage;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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

    @GetMapping("/get-solution/{question}")
    ResponseEntity<?> getSolution(@PathVariable("question") String question){
        PromptTemplate pt = new PromptTemplate("""
                {query}.
                1.Match query with issue description
                2. If the answer is not in the context, just say that you don't know.
                3. Avoid statements like "Based on the context..." or "The provided information...".{target}
                """);

        Prompt p = pt.create(
                Map.of("query", question,"target","Get only the Solution from the response")
        );

        /*return this.chatClient.prompt(p)
                .advisors(new QuestionAnswerAdvisor(store))
                .call()
                .content();*/
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.7)
                        .topK(3)
                        .vectorStore(store)
                        .build())
                .queryTransformers(rqtBuilder.promptTemplate(pt).build())
                .build();

        String response = this.chatClient.prompt(p)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content();

        return ResponseEntity.ok().body(new ResponseMessage(response));
    }
}
