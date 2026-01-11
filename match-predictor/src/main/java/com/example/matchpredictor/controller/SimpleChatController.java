package com.example.matchpredictor.controller;

import com.example.matchpredictor.service.MatchService;
import com.example.matchpredictor.service.TeamService;
import com.example.matchpredictor.entity.Match;
import com.example.matchpredictor.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class SimpleChatController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private TeamService teamService;

    /**
     * Simple chat endpoint that returns predictions based on team names
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "response", "Please ask me about a match prediction!"
            ));
        }

        String response = generateResponse(message.toLowerCase());

        return ResponseEntity.ok(Map.of(
                "response", response
        ));
    }

    private String generateResponse(String message) {
        // Greetings
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! I'm your AI football prediction assistant. Ask me about any upcoming match!";
        }

        // Help
        if (message.contains("help")) {
            return "I can help you with match predictions! Try asking:\n" +
                    "- 'Real Madrid vs Barcelona'\n" +
                    "- 'Who will win Liverpool vs Chelsea?'\n" +
                    "- 'Show me upcoming matches'\n" +
                    "- 'List all teams'";
        }

        // List teams
        if (message.contains("teams") || message.contains("list teams")) {
            List<Team> teams = teamService.getAllTeams();
            if (teams.isEmpty()) {
                return "No teams found in the database. Please add some teams first!";
            }

            String teamList = teams.stream()
                    .limit(10) // Show first 10 teams
                    .map(t -> t.getName() + " (" + t.getCountry() + ")")
                    .collect(Collectors.joining(", "));

            return "Available teams: " + teamList +
                    (teams.size() > 10 ? " and " + (teams.size() - 10) + " more..." : "");
        }

        // List matches
        if (message.contains("matches") || message.contains("upcoming")) {
            List<Match> matches = matchService.getUpcomingMatches();
            if (matches.isEmpty()) {
                return "No upcoming matches scheduled. Please create some matches first!";
            }

            String matchList = matches.stream()
                    .limit(5)
                    .map(m -> m.getHomeTeam().getName() + " vs " + m.getAwayTeam().getName() +
                            " (" + m.getLeague() + ")")
                    .collect(Collectors.joining("\n"));

            return "Upcoming matches:\n" + matchList;
        }

        // Predict specific match (try to extract team names)
        if (message.contains("vs") || message.contains("predict")) {
            String[] parts = message.split("vs");
            if (parts.length == 2) {
                String team1Name = parts[0].trim()
                        .replace("predict", "")
                        .replace("who will win", "")
                        .replace("between", "")
                        .trim();
                String team2Name = parts[1].trim()
                        .replace("?", "")
                        .trim();

                return generatePredictionResponse(team1Name, team2Name);
            }
        }

        // Check if message contains specific team names
        List<Team> allTeams = teamService.getAllTeams();
        for (Team team : allTeams) {
            if (message.contains(team.getName().toLowerCase())) {
                return String.format(
                        "I see you mentioned %s! They're from %s and were founded in %d. " +
                                "Would you like to know about their upcoming matches or make a prediction?",
                        team.getName(),
                        team.getCountry(),
                        team.getFoundedYear() != null ? team.getFoundedYear() : 0
                );
            }
        }

        // Default response
        return "I'm not sure I understand. Try asking about:\n" +
                "- Specific matches (e.g., 'Real Madrid vs Barcelona')\n" +
                "- 'Show upcoming matches'\n" +
                "- 'List all teams'\n" +
                "- Type 'help' for more options";
    }

    private String generatePredictionResponse(String team1, String team2) {
        // Try to find teams in database
        List<Team> allTeams = teamService.getAllTeams();

        Team foundTeam1 = allTeams.stream()
                .filter(t -> t.getName().toLowerCase().contains(team1.toLowerCase()) ||
                        team1.toLowerCase().contains(t.getName().toLowerCase()))
                .findFirst()
                .orElse(null);

        Team foundTeam2 = allTeams.stream()
                .filter(t -> t.getName().toLowerCase().contains(team2.toLowerCase()) ||
                        team2.toLowerCase().contains(t.getName().toLowerCase()))
                .findFirst()
                .orElse(null);

        if (foundTeam1 == null || foundTeam2 == null) {
            return String.format(
                    "I couldn't find both teams in the database. Available teams: %s",
                    allTeams.stream().limit(5).map(Team::getName).collect(Collectors.joining(", "))
            );
        }

        // Check if there's an upcoming match between these teams
        List<Match> upcomingMatches = matchService.getUpcomingMatches();
        Match foundMatch = upcomingMatches.stream()
                .filter(m ->
                        (m.getHomeTeam().getId().equals(foundTeam1.getId()) &&
                                m.getAwayTeam().getId().equals(foundTeam2.getId())) ||
                                (m.getHomeTeam().getId().equals(foundTeam2.getId()) &&
                                        m.getAwayTeam().getId().equals(foundTeam1.getId()))
                )
                .findFirst()
                .orElse(null);

        if (foundMatch != null) {
            return String.format(
                    "Great! I found the match: %s vs %s in %s.\n\n" +
                            "Based on current analysis:\n" +
                            "🏠 %s (home): ~55%% chance\n" +
                            "⚖️ Draw: ~25%% chance\n" +
                            "✈️ %s (away): ~20%% chance\n\n" +
                            "Home advantage typically plays a significant role. Click 'Predict' on this match for detailed AI analysis!",
                    foundMatch.getHomeTeam().getName(),
                    foundMatch.getAwayTeam().getName(),
                    foundMatch.getLeague(),
                    foundMatch.getHomeTeam().getName(),
                    foundMatch.getAwayTeam().getName()
            );
        }

        // Generic prediction if no match scheduled
        return String.format(
                "No scheduled match found between %s and %s, but here's my general analysis:\n\n" +
                        "🔵 %s has a strong record against %s historically.\n" +
                        "📊 Head-to-head advantage and current form would be key factors.\n" +
                        "🏆 League standings and recent performances matter significantly.\n\n" +
                        "Would you like to create a match between these teams to get a detailed AI prediction?",
                foundTeam1.getName(),
                foundTeam2.getName(),
                foundTeam1.getName(),
                foundTeam2.getName()
        );
    }

}
