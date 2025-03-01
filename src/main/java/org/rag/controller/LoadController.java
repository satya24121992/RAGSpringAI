package org.rag.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rag.model.RemedyText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/snow")
public class LoadController {
    private final static Logger LOG = LoggerFactory.getLogger(LoadController.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final VectorStore store;


    public LoadController(VectorStore store) {
        this.store = store;
    }

    @GetMapping("/load-data")
    public String load() throws JsonProcessingException {
        RemedyText remedyText1 = new RemedyText();
        remedyText1.setIssueDescription("Encountered with 404 errors");
        remedyText1.setSolution("Correct the URL's");

        RemedyText remedyText2 = new RemedyText();
        remedyText2.setIssueDescription("Encountered with nullpointer errors");
        remedyText2.setSolution("Keep the null checks");


        var doc1 = Document.builder()
                .id(UUID.randomUUID().toString())
                .text(mapper.writeValueAsString(remedyText1))
                .build();
        var doc2 = Document.builder()
                .id(UUID.randomUUID().toString())
                .text(mapper.writeValueAsString(remedyText2))
                .build();

        List<Document> docList = new ArrayList<>();
        docList.add(doc2);
        docList.add(doc1);

        try {
            store.add(docList);
        }catch (Exception e){
            LOG.error("Error while Storing",e);
        }
        return "loaded Successfully";
    }

    @GetMapping("/docs")
    List<Document> query() {
        SearchRequest searchRequest = SearchRequest.builder()
                .query("errors")
                .topK(2)
                .build();
        List<Document> docs = store.similaritySearch(searchRequest);
        return docs;
    }
}
