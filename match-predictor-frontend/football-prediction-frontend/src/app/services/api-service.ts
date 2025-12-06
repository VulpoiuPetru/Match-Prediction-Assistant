import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // Teams
  getTeams(): Observable<any[]> {
    return this. http.get<any[]>(`${this.baseUrl}/predictions/teams`);
  }

  // Matches
  getUpcomingMatches(): Observable<any[]> {
    return this. http.get<any[]>(`${this.baseUrl}/predictions/upcoming-matches`);
  }

  // Predictions
  generatePrediction(matchId: number): Observable<any> {
    return this.http. post<any>(`${this.baseUrl}/predictions/generate/${matchId}`, {});
  }

  getLatestPrediction(matchId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/predictions/match/${matchId}/latest`);
  }

  getPredictionStats(): Observable<string> {
    return this.http.get(`${this.baseUrl}/predictions/stats`, { responseType: 'text' });
  }

  // AI Test
  testAi(message: string): Observable<string> {
    return this.http.get(`http://localhost:8080/test-ai? message=${encodeURIComponent(message)}`, { responseType: 'text' });
  }
}
