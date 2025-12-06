import { Component, inject, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../services/api-service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms'; 
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-component',
  imports: [MatButtonModule,MatCardModule,MatProgressSpinnerModule,MatTableModule,MatIconModule,MatTableModule,FormsModule,MatFormFieldModule,MatInputModule,CommonModule],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent implements OnInit {

  private apiService = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  
  // Teams data
  teams: any[] = [];
  isLoadingTeams = false;
  teamsError = '';

  // Matches data
  matches: any[] = [];
  isLoadingMatches = false;
  matchesError = '';
  
  // Prediction state
  predictingMatchId: number | null = null;

    // AI Chat data - ADD THESE
  chatMessages: {role: 'user' | 'ai', content: string, timestamp: Date}[] = [];
  userMessage = '';
  isAiThinking = false;

  // Recent Predictions data - ADD THESE
  recentPredictions: any[] = [];
  isLoadingPredictions = false;
  predictionsError = '';
  totalPredictions = 0;
  avgConfidence = 0;

  ngOnInit() {
    this.loadTeams();
    this.loadMatches();
    this.initializeChat();
     this.loadRecentPredictions();
  }

  loadRecentPredictions() {
    this.isLoadingPredictions = true;
    this. predictionsError = '';
    
    // For now, we'll create mock data since you might not have predictions yet
    // Later you can replace this with a real API call
    setTimeout(() => {
      this.recentPredictions = this.generateMockPredictions();
      this.calculateStats();
      this.isLoadingPredictions = false;
    }, 1000);
    
    // Uncomment this when you have a real API endpoint for predictions
    /*
    this.apiService.getRecentPredictions().subscribe({
      next: (data) => {
        this.recentPredictions = data;
        this.calculateStats();
        this.isLoadingPredictions = false;
      },
      error: (error) => {
        this.predictionsError = 'Failed to load predictions. ';
        this.isLoadingPredictions = false;
      }
    });
    */
  }

   generateMockPredictions() {
    const mockPredictions = [
      {
        id: 1,
        homeTeam: { name: 'Real Madrid' },
        awayTeam: { name: 'Barcelona' },
        predictedHomeScore: 2,
        predictedAwayScore: 1,
        confidence: 85.5,
        explanation: 'Real Madrid has strong home advantage and better recent form.',
        predictionDate: new Date(Date.now() - 1000 * 60 * 30), // 30 min ago
        league: 'La Liga'
      },
      {
        id: 2,
        homeTeam: { name: 'Liverpool' },
        awayTeam: { name: 'Manchester City' },
        predictedHomeScore: 1,
        predictedAwayScore: 2,
        confidence: 78.2,
        explanation: 'Manchester City has superior squad depth and tactical flexibility.',
        predictionDate: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2 hours ago
        league: 'Premier League'
      },
      {
        id: 3,
        homeTeam: { name: 'Bayern Munich' },
        awayTeam: { name: 'Borussia Dortmund' },
        predictedHomeScore: 3,
        predictedAwayScore: 1,
        confidence: 92.1,
        explanation: 'Bayern Munich dominates head-to-head record and has key players available.',
        predictionDate: new Date(Date.now() - 1000 * 60 * 60 * 5), // 5 hours ago
        league: 'Bundesliga'
      },
      {
        id: 4,
        homeTeam: { name: 'PSG' },
        awayTeam: { name: 'Lyon' },
        predictedHomeScore: 2,
        predictedAwayScore: 0,
        confidence: 76.8,
        explanation: 'PSG has home advantage and stronger attacking options.',
        predictionDate: new Date(Date.now() - 1000 * 60 * 60 * 8), // 8 hours ago
        league: 'Ligue 1'
      }
    ];

    return mockPredictions;
  }

  // ADD THIS METHOD
  calculateStats() {
    this.totalPredictions = this.recentPredictions.length;
    if (this.totalPredictions > 0) {
      const totalConfidence = this.recentPredictions.reduce((sum, pred) => sum + pred.confidence, 0);
      this.avgConfidence = Math.round(totalConfidence / this.totalPredictions * 10) / 10;
    }
  }

  // ADD THIS METHOD
  getTimeAgo(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math. floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 60) {
      return `${diffMins} min ago`;
    } else if (diffHours < 24) {
      return `${diffHours}h ago`;
    } else {
      return `${diffDays}d ago`;
    }
  }

  // ADD THIS METHOD
  getConfidenceColor(confidence: number): string {
    if (confidence >= 80) return 'high-confidence';
    if (confidence >= 60) return 'medium-confidence';
    return 'low-confidence';
  }

  // ADD THIS METHOD
  refreshPredictions() {
    this.loadRecentPredictions();
  }

   initializeChat() {
    this. chatMessages = [
      {
        role: 'ai',
        content: 'Hello! Ask me about match predictions!\n\nTry: "Real Madrid vs Barcelona" or "Who will win Liverpool vs Chelsea?"',
        timestamp: new Date()
      }
    ];
  }
   sendMessage() {
    if (! this.userMessage.trim() || this.isAiThinking) return;

    // Add user message
    this.chatMessages.push({
      role: 'user',
      content: this.userMessage,
      timestamp: new Date()
    });

    const message = this.userMessage;
    this.userMessage = '';
    this.isAiThinking = true;

    // Send to AI
    this.apiService. testAi(message).subscribe({
      next: (response) => {
        this.chatMessages.push({
          role: 'ai',
          content: this.formatAiResponse(response),
          timestamp: new Date()
        });
        this.isAiThinking = false;
        this.scrollToBottom();
      },
      error: (error) => {
        this.chatMessages.push({
          role: 'ai',
          content: '❌ Sorry, I couldn\'t process that. Make sure the backend is running.',
          timestamp: new Date()
        });
        this.isAiThinking = false;
        console.error('AI Error:', error);
      }
    });
  }

   formatAiResponse(response: string): string {
    // Clean up the response and add emoji formatting
    let formatted = response.trim();
    
    // Add soccer emoji if it's about predictions
    if (formatted.includes('vs') || formatted.includes('win') || formatted.includes('score')) {
      formatted = '⚽ ' + formatted;
    }
    
    return formatted;
  }

  // ADD THIS METHOD
  scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }

  // ADD THIS METHOD
  onEnterKey(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }


  loadTeams() {
    this. isLoadingTeams = true;
    this.teamsError = '';
    
    this.apiService.getTeams().subscribe({
      next: (data) => {
        this.teams = data;
        this.isLoadingTeams = false;
        console.log('Teams loaded:', data);
      },
      error: (error) => {
        this.teamsError = 'Failed to load teams. Make sure your backend is running on port 8080. ';
        this.isLoadingTeams = false;
        console.error('Error loading teams:', error);
      }
    });
  }

  loadMatches() {
    this.isLoadingMatches = true;
    this.matchesError = '';
    
    this.apiService.getUpcomingMatches().subscribe({
      next: (data) => {
        this.matches = data;
        this.isLoadingMatches = false;
        console. log('Matches loaded:', data);
      },
      error: (error) => {
        this. matchesError = 'Failed to load matches. ';
        this.isLoadingMatches = false;
        console.error('Error loading matches:', error);
      }
    });
  }

  generatePrediction(match: any) {
    this. predictingMatchId = match.id;
    
    this.apiService.generatePrediction(match.id).subscribe({
      next: (prediction) => {
        this.predictingMatchId = null;
        this.snackBar.open('Prediction generated successfully!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        console.log('Prediction generated:', prediction);
        // Refresh predictions to show the new one
        this.loadRecentPredictions();
      },
      error: (error) => {
        this.predictingMatchId = null;
        this.snackBar.open('Failed to generate prediction', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        console.error('Error generating prediction:', error);
      }
    });
  }

  formatMatchDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
  }

  trackPrediction(index: number, prediction: any): any {
  return prediction. id;
}

}
