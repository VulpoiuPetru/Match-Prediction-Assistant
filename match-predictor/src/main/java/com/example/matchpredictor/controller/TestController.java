package com.example.matchpredictor.controller;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private OllamaChatClient ollamaChatClient;

    @GetMapping("/")
    public String home() {
        return "⚽ Match Prediction Assistant is running! ⚽\n\n" +
                "Test endpoints:\n" +
                "- /test-ai?message=your_message\n" +
                "- /test-prediction\n" +
                "- /status";
    }

    @GetMapping("/test-ai")
    public String testAi(@RequestParam(defaultValue = "Hello") String message) {
        try {
            ChatResponse response = ollamaChatClient.call(new Prompt(message));
            return "🤖 AI Response:\n\n" + response.getResult().getOutput().getContent();
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage() +
                    "\n\nMake sure Ollama is running with model llama3.2";
        }
    }

    @GetMapping("/test-prediction")
    public String testPrediction() {
        String prompt = """
            You are a football analyst. Analyze this match:
            
            Home Team: Manchester United
            Away Team: Liverpool  
            League: Premier League
            Venue: Old Trafford
            
            Provide win probabilities and brief reasoning.
            """;

        try {
            ChatResponse response = ollamaChatClient.call(new Prompt(prompt));
            return "⚽ Match Prediction:\n\n" + response.getResult().getOutput().getContent();
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @GetMapping("/status")
    public String status() {
        return "✅ Application Status: RUNNING\n" +
                "🤖 AI Service: Ollama (llama3.2)\n" +
                "🗄️ Database: H2 (in-memory)\n" +
                "🌐 Server: http://localhost:8080";
    }
}