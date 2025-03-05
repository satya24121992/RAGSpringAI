package org.rag.SourceSystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class StackOverflowAPI implements SourceSystemInterface{

    public void getSourceData() {
        try {
            // Step 1: Fetch questions related to Java, Database, and Security
            String urlString = "https://api.stackexchange.com/2.3/questions?site=stackoverflow&tagged=java;database;security";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status == 200) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray questions = jsonResponse.getAsJsonArray("items");

                // Step 2: Loop through each question to fetch answers
                for (int i = 0; i < questions.size(); i++) {
                    JsonObject question = questions.get(i).getAsJsonObject();
                    String questionTitle = question.get("title").getAsString();
                    int questionId = question.get("question_id").getAsInt();

                    System.out.println("Question: " + questionTitle);
                    System.out.println("Fetching answers for question ID: " + questionId);

                    // Step 3: Fetch answers for the current question
                    String answerUrlString = "https://api.stackexchange.com/2.3/questions/" + questionId + "/answers?site=stackoverflow&filter=withbody";
                    URL answerUrl = new URL(answerUrlString);
                    HttpURLConnection answerConnection = (HttpURLConnection) answerUrl.openConnection();
                    answerConnection.setRequestMethod("GET");
                    answerConnection.setConnectTimeout(5000);
                    answerConnection.setReadTimeout(5000);

                    int answerStatus = answerConnection.getResponseCode();
                    if (answerStatus == 200) {
                        // Read the answers response
                        BufferedReader answerIn = new BufferedReader(new InputStreamReader(answerConnection.getInputStream()));
                        StringBuilder answerResponse = new StringBuilder();
                        while ((inputLine = answerIn.readLine()) != null) {
                            answerResponse.append(inputLine);
                        }
                        answerIn.close();

                        // Parse the answers
                        JsonObject answerJsonResponse = JsonParser.parseString(answerResponse.toString()).getAsJsonObject();
                        JsonArray answers = answerJsonResponse.getAsJsonArray("items");

                        if (answers.size() > 0) {
                            System.out.println("Answers:");
                            for (int j = 0; j < answers.size(); j++) {
                                JsonObject answer = answers.get(j).getAsJsonObject();
                                if(answer.get("body") != null) {
                                    String answerBody = answer.get("body").getAsString();
                                    System.out.println("- " + answerBody);
                                }
                            }
                        } else {
                            System.out.println("No answers found for this question.");
                        }

                    } else {
                        System.out.println("Error fetching answers for question ID: " + questionId);
                    }

                    System.out.println(); // Blank line between questions
                }

            } else {
                System.out.println("Error: Unable to retrieve questions. HTTP Status Code: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

