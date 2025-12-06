import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { TeamsComponent } from './components/teams-component/teams-component';
import { MatchesComponent } from './components/matches-component/matches-component';
import { PredictionsComponent } from './components/predictions-component/predictions-component';
import { AiTestComponent } from './components/ai-test-component/ai-test-component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
//   { path: 'teams', component: TeamsComponent },
//   { path: 'matches', component: MatchesComponent },
//   { path: 'predictions', component: PredictionsComponent },
//   { path: 'ai-test', component: AiTestComponent }
];