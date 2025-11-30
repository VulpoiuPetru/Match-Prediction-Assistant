package com.example.matchpredictor.service;

import com.example.matchpredictor.entity.AiPrediction;
import com.example.matchpredictor.entity.Match;
import com.example.matchpredictor.repository.AiPredictionRepository;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AiPredictionService {

    @Autowired
    private AiPredictionRepository aiPredictionRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private OllamaChatClient ollamaChatClient;

    //Generate AI prediction for a match
    public AiPrediction generatePrediction(Integer matchId) {
        Match match = matchService.getMatchById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + matchId));

        String prompt = createPredictionPrompt(match);

        try {
            // Create NEW client with explicit model (same as working TestController)
            OllamaApi directApi = new OllamaApi("http://localhost:11434");
            OllamaChatClient directClient = new OllamaChatClient(directApi);

            ChatResponse response = directClient.call(
                    new Prompt(prompt,
                            org.springframework.ai.ollama. api.OllamaOptions. create()
                                    .withModel("llama3.2"))
            );

            String aiResponse = response.getResult().getOutput().getContent();
            AiPrediction prediction = parseAiResponse(match, aiResponse);
            prediction.setModelVersion("llama3.2");
            return aiPredictionRepository.save(prediction);

        } catch (Exception e) {
            throw new RuntimeException("Error generating AI prediction: " + e.getMessage());
        }
    }

    //Get prediction for a match
    public List<AiPrediction> getPredictionsForMatch(Integer matchId) {
        Match match = matchService.getMatchById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + matchId));
        return aiPredictionRepository.findByMatch(match);
    }

    //Get latest prediction for a match
    public Optional<AiPrediction> getLatestPrediction(Integer matchId) {
        Match match = matchService.getMatchById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + matchId));

        List<AiPrediction> predictions = aiPredictionRepository
                .findByMatchOrderByCreatedAtDesc(match, PageRequest.of(0, 1));

        return predictions.isEmpty() ? Optional.empty() : Optional.of(predictions.get(0));
    }

    //Get all predictions
    public List<AiPrediction> getAllPredictions(){
        return aiPredictionRepository.findAll();
    }

    //Create prediction prompt
    private String createPredictionPrompt(Match match) {
        return String.format("""
            You are a professional football analyst. Analyze this upcoming match and provide prediction probabilities:
            
            Match Details:
            - Home Team: %s (%s)
            - Away Team: %s (%s)
            - League: %s
            - Venue: %s
            - Date: %s
            
            Please provide:
            1. Home Win Probability (0-100)
            2. Draw Probability (0-100)  
            3. Away Win Probability (0-100)
            4. Brief reasoning (2-3 sentences)
            
            Format your response as:
            HOME_WIN: [percentage]
            DRAW: [percentage]
            AWAY_WIN: [percentage]
            REASONING: [your analysis]
            
            Make sure the probabilities add up to 100.
            """,
                match.getHomeTeam().getName(),
                match.getHomeTeam().getCountry(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getCountry(),
                match.getLeague(),
                match.getVenue() != null ? match.getVenue() : "TBD",
                match.getMatchDate()
        );
    }

    //Parse AI response(simplified version)
    private AiPrediction parseAiResponse(Match match, String aiResponse) {
        AiPrediction prediction = new AiPrediction();
        prediction.setMatch(match);

        try {
            // Extract probabilities (basic parsing - you can enhance this)
            String[] lines = aiResponse.split("\n");
            BigDecimal homeWin = new BigDecimal("33.33");
            BigDecimal draw = new BigDecimal("33.34");
            BigDecimal awayWin = new BigDecimal("33.33");
            String reasoning = aiResponse;

            for (String line : lines) {
                if (line.startsWith("HOME_WIN:")) {
                    homeWin = new BigDecimal(line.replace("HOME_WIN:", "").trim().replace("%", ""));
                } else if (line.startsWith("DRAW:")) {
                    draw = new BigDecimal(line.replace("DRAW:", "").trim().replace("%", ""));
                } else if (line.startsWith("AWAY_WIN:")) {
                    awayWin = new BigDecimal(line.replace("AWAY_WIN:", "").trim().replace("%", ""));
                } else if (line.startsWith("REASONING:")) {
                    reasoning = line.replace("REASONING:", "").trim();
                }
            }

            prediction.setHomeWinProbability(homeWin);
            prediction.setDrawProbability(draw);
            prediction.setAwayWinProbability(awayWin);
            prediction.setReasoning(reasoning);
            prediction.setConfidenceScore(new BigDecimal("0.75")); // Default confidence

        } catch (Exception e) {
            // Default values if parsing fails
            prediction.setHomeWinProbability(new BigDecimal("33.33"));
            prediction.setDrawProbability(new BigDecimal("33.34"));
            prediction.setAwayWinProbability(new BigDecimal("33.33"));
            prediction.setReasoning("AI analysis: " + aiResponse);
            prediction.setConfidenceScore(new BigDecimal("0.50"));
        }

        return prediction;
    }

    // Get prediction accuracy statistics
    public String getPredictionStats() {
        Long correct = aiPredictionRepository.countCorrectPredictions();
        Long total = aiPredictionRepository.countEvaluatedPredictions();

        if (total == 0) {
            return "No evaluated predictions yet";
        }

        double accuracy = (correct.doubleValue() / total.doubleValue()) * 100;
        return String.format("Accuracy: %.2f%% (%d correct out of %d predictions)", accuracy, correct, total);
    }
}
