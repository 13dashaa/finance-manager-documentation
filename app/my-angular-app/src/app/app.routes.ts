// src/app/app.routes.ts
import {Routes} from '@angular/router';
import {AccountListComponent} from './components/accounts/account-list/account-list.component';

import {TransactionListComponent} from './components/transactions/transaction-list/transaction-list.component';
import {ClientListComponent} from './components/clients/client-list/client-list.component';
import {ClientDetailComponent} from './components/clients/client-detail/client-detail.component';
import {CategoryListComponent} from './components/categories/category-list/category-list.component';
import {GoalListComponent} from './components/goals/goal-list/goal-list.component';
import {AccountDetailComponent} from './components/accounts/account-detail/account-detail.component';
import {BudgetListComponent} from './components/budgets/budget-list/budget-list.component';
import {BudgetDetailComponent} from './components/budgets/budget-detail/budget-detail.component';
import {HomeComponent} from './components/home/home-page/home-page.component'; // <-- Импорт


export const routes: Routes = [
  // {
  //   path: '', redirectTo: '/clients', pathMatch: 'full'
  // },
  { path: '', component:  HomeComponent },  // главная страница

  {
    path: 'transactions', component: TransactionListComponent
  },
  {
    path: 'budgets', component: BudgetListComponent
  },
  {
    path: 'budgets/:id', component: BudgetDetailComponent
  },
  {path: 'categories', component: CategoryListComponent},
  {
    path: 'goals', component: GoalListComponent
  },
  {
    path: 'clients', component: ClientListComponent
  },
  {path: 'clients/:id', component: ClientDetailComponent, title: 'Client Details'}, // <-- Маршрут с параметром ID

  {
    path: 'accounts', // Базовый путь для счетов -> список
    component: AccountListComponent
  },
  {path: 'accounts/:id', component: AccountDetailComponent, title: 'Accounts Details'}, // <-- Маршрут с параметром ID


];
