import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {BudgetGetDto} from '../../../models/budget.model';
import {BudgetService} from '../../../services/budget.service';
import {CommonModule, CurrencyPipe} from '@angular/common';
import {forkJoin} from 'rxjs';
import {TransactionGetDto} from '../../../models/transaction.model';
import {TransactionService} from '../../../services/transaction.service';


@Component({
  selector: 'app-budget-detail',
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe // Добавляем пайп для валюты
  ],
  templateUrl: './budget-detail.component.html',
  styleUrls: ['./budget-detail.component.css']
})
export class BudgetDetailComponent implements OnInit {
  budget: BudgetGetDto | null = null;
  transactions: TransactionGetDto[] = [];
  isLoading = true;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly transactionService: TransactionService,
    private readonly budgetService: BudgetService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTransactions();
    if (!isNaN(id)) {
      this.budgetService.getBudgetById(id).subscribe(data => {
        this.budget = data;
      });
    }
  }

  getClientUsernames(): string[] {
    return this.budget?.clientUsernames ? Array.from(this.budget.clientUsernames) : [];
  }

  getClientIdByIndex(index: number): number | null {
    return this.budget?.clientIds?.[index] ?? null;
  }
  loadTransactions(): void {
    if (!this.budget?.clientIds?.length) {
      this.isLoading = false;
      return;
    }

    this.isLoading = true;

    const requests = this.budget.clientIds
      .filter(clientId => clientId !== null && clientId !== undefined)
      .map(clientId =>
        this.transactionService.getTransactionsByClientAndCategory(
          clientId,
          this.budget!.categoryId // Используем "!", так как проверили budget выше
        )
      );

    if (requests.length === 0) {
      this.isLoading = false;
      return;
    }

    forkJoin(requests).subscribe({
      next: (transactionsArrays) => {
        this.transactions = transactionsArrays.flat().filter(t => t !== null);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading transactions', err);
        this.isLoading = false;
      }
    });
  }

  getSpent(): number {
    if (!this.budget) return 0;
    return this.budget.limitation - this.budget.availableSum;
  }
}
