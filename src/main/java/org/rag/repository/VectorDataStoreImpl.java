package org.rag.repository;

import org.rag.controller.LoadController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorDataStoreImpl {
    private final static Logger LOG = LoggerFactory.getLogger(VectorDataStoreImpl.class);
    private final VectorStore store;

    public VectorDataStoreImpl(VectorStore store) {
        this.store = store;
    }

    public String saveData(List<Document> docList) {

        try {
            store.add(docList);
        }catch (Exception e){
            LOG.error("Error while Storing",e);
        }
        return "loaded Successfully";
    }
}
