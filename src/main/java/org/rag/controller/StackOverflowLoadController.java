package org.rag.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.rag.SourceSystem.StackOverflowAPI;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stackoverflow")
public class StackOverflowLoadController {

    private final VectorStore store;
    private final StackOverflowAPI stackOverflowAPI;

    public StackOverflowLoadController(VectorStore store, StackOverflowAPI stackOverflowAPI) {
        this.store = store;
        this.stackOverflowAPI=stackOverflowAPI;
    }

    @GetMapping("/load-stack-overflow-data")
    public String loadStackOverflowData() throws JsonProcessingException {
        stackOverflowAPI.getSourceData();
        return "loaded stack data";
    }
}
