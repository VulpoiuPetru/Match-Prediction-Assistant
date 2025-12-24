package com.example.matchpredictor.service;

import com.example.matchpredictor.entity.AiPrediction;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class ChromaDbService {

    private Object client;  // Using Object to avoid import errors if library not fully configured
    private Object predictionCollection;
    private boolean isConnected = false;

    @PostConstruct
    public void init() {
        try {
            // Try to connect to ChromaDB
            Class<? > clientClass = Class.forName("tech.amikos.chromadb.Client");
            client = clientClass.getConstructor(String.class).newInstance("http://localhost:8000");

            // Try to get or create collection
            var getOrCreateMethod = clientClass.getMethod("getOrCreateCollection", String.class, Map.class);
            predictionCollection = getOrCreateMethod.invoke(client, "match_predictions", null);

            isConnected = true;
            System.out.println("✅ ChromaDB connected successfully!");
            System.out.println("   Collection 'match_predictions' ready for vector storage.");

        } catch (ClassNotFoundException e) {
            System.out.println("ℹ️ ChromaDB client library not found - vector storage disabled");
            isConnected = false;
        } catch (Exception e) {
            System.out.println("⚠️ ChromaDB connection failed:  " + e.getMessage());
            System.out.println("ℹ️ App will continue without vector storage.");
            System.out.println("   To enable:  docker run -d -p 8000:8000 chromadb/chroma");
            isConnected = false;
        }
    }


     // Store prediction in ChromaDB for semantic search and history tracking

    public void storePrediction(AiPrediction prediction) {
        if (!isConnected || predictionCollection == null) {
            // Log but don't fail - app works without ChromaDB
            System.out. println("ℹ️ ChromaDB not available - prediction stored in PostgreSQL only");
            return;
        }

        try {
            String id = "prediction_" + prediction.getId();

            String document = String.format(
                    "Match:  %s vs %s.  League: %s.  Prediction: Home %. 2f%%, Draw %.2f%%, Away %.2f%%.  Reasoning: %s",
                    prediction.getMatch().getHomeTeam().getName(),
                    prediction.getMatch().getAwayTeam().getName(),
                    prediction.getMatch().getLeague(),
                    prediction.getHomeWinProbability(),
                    prediction.getDrawProbability(),
                    prediction.getAwayWinProbability(),
                    prediction.getReasoning()
            );

            Map<String, String> metadata = new HashMap<>();
            metadata. put("match_id", prediction.getMatch().getId().toString());
            metadata.put("home_team", prediction. getMatch().getHomeTeam().getName());
            metadata.put("away_team", prediction.getMatch().getAwayTeam().getName());
            metadata.put("league", prediction.getMatch().getLeague());

            // Use reflection to call add method
            var addMethod = predictionCollection.getClass().getMethod("add",
                    List.class, List.class, List.class, List.class);
            addMethod. invoke(predictionCollection,
                    null, List.of(metadata), List.of(document), List.of(id));

            System.out.println("✅ Stored in ChromaDB: " + id);

        } catch (Exception e) {
            System.out.println("⚠️ ChromaDB storage failed:  " + e.getMessage());
        }
    }


    //  Search similar predictions using semantic/vector search

    public List<String> searchSimilarPredictions(String query, int limit) {
        if (!isConnected) {
            return Collections.singletonList("ChromaDB not connected - semantic search unavailable");
        }

        try {
            var queryMethod = predictionCollection. getClass().getMethod("query",
                    List.class, Integer.class, Map.class, Map.class, List.class);
            Object results = queryMethod. invoke(predictionCollection,
                    List.of(query), limit, null, null, null);

            // Extract documents from results
            var getDocsMethod = results.getClass().getMethod("getDocuments");
            @SuppressWarnings("unchecked")
            List<List<String>> docs = (List<List<String>>) getDocsMethod.invoke(results);

            if (docs != null && !docs.isEmpty()) {
                return docs.get(0);
            }
        } catch (Exception e) {
            System.out.println("⚠️ ChromaDB query failed: " + e.getMessage());
        }

        return Collections.emptyList();
    }


     // Get predictions related to a specific team

    public List<String> getPredictionsByTeam(String teamName, int limit) {
        return searchSimilarPredictions(teamName + " match prediction analysis", limit);
    }

     // Get count of stored predictions in ChromaDB
    public int getPredictionCount() {
        if (! isConnected || predictionCollection == null) {
            return 0;
        }
        try {
            var countMethod = predictionCollection.getClass().getMethod("count");
            return (Integer) countMethod.invoke(predictionCollection);
        } catch (Exception e) {
            return 0;
        }
    }


    //  Check if ChromaDB is connected
    public boolean isConnected() {
        return isConnected;
    }

     // Get connection status message
    public String getStatusMessage() {
        if (isConnected) {
            return "ChromaDB connected - Vector storage enabled";
        } else {
            return "ChromaDB not connected - Run:  docker run -d -p 8000:8000 chromadb/chroma";
        }
    }


}
