package org.rag.SourceSystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.micrometer.common.util.StringUtils;
import org.rag.model.RemedyText;
import org.rag.repository.VectorDataStoreImpl;
import org.rag.svc.StackOverflowHttpClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class StackOverflowAPI implements SourceSystemInterface {

    private final StackOverflowHttpClient httpClient;

    private final VectorDataStoreImpl vectorDataStoreImpl;

    private final ObjectMapper mapper = new ObjectMapper();

    public StackOverflowAPI(StackOverflowHttpClient httpClient, VectorDataStoreImpl vectorDataStoreImpl) {
        this.httpClient = httpClient;
        this.vectorDataStoreImpl = vectorDataStoreImpl;
    }

    public String getSourceData() {
        try {
            String urlString = "https://api.stackexchange.com/2.3/questions?site=stackoverflow&tagged=java;database;security";
            String response = httpClient.fetchData(urlString);
            if (response != null) {
              return  processQuestions(response);
            } else {
                System.out.println("Error: Unable to retrieve questions.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "failed loading data";
    }

    private String processQuestions(String jsonResponse) throws JsonProcessingException {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray questions = jsonObject.getAsJsonArray("items");
        List<Document> docList = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            JsonObject question = questions.get(i).getAsJsonObject();
            String questionTitle = question.get("title").getAsString();
            int questionId = question.get("question_id").getAsInt();

            System.out.println("Question: " + questionTitle);
            System.out.println("Fetching answers for question ID: " + questionId);

            String answer = fetchAndProcessAnswers(questionId);
            System.out.println(); // Blank line between questions

            if(StringUtils.isNotEmpty(answer)) {
                RemedyText remedyText = new RemedyText();
                remedyText.setIssueDescription(questionTitle);
                remedyText.setSolution(answer);

                var doc = Document.builder()
                        .id(UUID.randomUUID().toString())
                        .text(mapper.writeValueAsString(remedyText))
                        .build();

                docList.add(doc);
            }
        }
        return loadDataToVectorStore(docList);
    }

    private String fetchAndProcessAnswers(int questionId) {
        String answerUrlString = "https://api.stackexchange.com/2.3/questions/" + questionId + "/answers?site=stackoverflow&filter=withbody";
        String response = httpClient.fetchData(answerUrlString);
        String completeAnswer = null;
        if (response != null) {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonArray answers = jsonResponse.getAsJsonArray("items");
            if (answers.size() > 0) {
                System.out.println("Answers:");
                for (int j = 0; j < answers.size(); j++) {
                    JsonObject answer = answers.get(j).getAsJsonObject();
                    if (answer.get("body") != null) {
                        String answerBody = answer.get("body").getAsString();
                        completeAnswer = StringUtils.isBlank(completeAnswer)?answerBody:completeAnswer.concat(answerBody);
                    }
                    System.out.println("- " + completeAnswer);
                }
            } else {
                System.out.println("No answers found for this question.");
            }
        } else {
            System.out.println("Error fetching answers for question ID: " + questionId);
        }
        return  completeAnswer;
    }

    private String loadDataToVectorStore(List<Document> docList) {
       return vectorDataStoreImpl.saveData(docList);
    }
}