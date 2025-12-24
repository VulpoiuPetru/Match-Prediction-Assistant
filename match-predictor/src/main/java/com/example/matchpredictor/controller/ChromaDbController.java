package com.example.matchpredictor.controller;

import com.example.matchpredictor.service.ChromaDbService;
import org.springframework.beans. factory.annotation. Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework. web.bind.annotation.*;

import java. util.List;
import java.util. Map;

@RestController
@RequestMapping("/api/chromadb")
@CrossOrigin(origins = "*")
public class ChromaDbController {

    @Autowired
    private ChromaDbService chromaDbService;


     //Check ChromaDB connection status
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "connected", chromaDbService.isConnected(),
                "predictionCount", chromaDbService.getPredictionCount(),
                "message", chromaDbService.isConnected()
                        ? "ChromaDB is connected and ready"
                        : "ChromaDB is not connected.  Run:  docker run -p 8000:8000 chromadb/chroma"
        ));
    }


     // Search similar predictions using semantic search
    @GetMapping("/search")
    public ResponseEntity<? > searchPredictions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {

        if (!chromaDbService.isConnected()) {
            return ResponseEntity.ok(Map.of(
                    "error", "ChromaDB not connected",
                    "results", List.of()
            ));
        }

        List<String> results = chromaDbService. searchSimilarPredictions(query, limit);
        return ResponseEntity.ok(Map.of(
                "query", query,
                "results", results,
                "count", results.size()
        ));
    }


     // Get prediction history for a specific team
    @GetMapping("/team/{teamName}")
    public ResponseEntity<?> getPredictionsByTeam(
            @PathVariable String teamName,
            @RequestParam(defaultValue = "10") int limit) {

        if (!chromaDbService.isConnected()) {
            return ResponseEntity.ok(Map.of(
                    "error", "ChromaDB not connected",
                    "results", List.of()
            ));
        }

        List<String> results = chromaDbService.getPredictionsByTeam(teamName, limit);
        return ResponseEntity.ok(Map.of(
                "team", teamName,
                "results", results,
                "count", results. size()
        ));
    }
}
