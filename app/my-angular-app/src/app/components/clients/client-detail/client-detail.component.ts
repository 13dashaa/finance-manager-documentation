import {Component, Inject, OnDestroy, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule, CurrencyPipe, DatePipe, isPlatformBrowser} from '@angular/common';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {EMPTY, forkJoin, Observable, Subject} from 'rxjs';
import {catchError, switchMap, takeUntil, tap} from 'rxjs/operators';
import {BaseChartDirective} from 'ng2-charts';
import {ChartData, ChartOptions} from 'chart.js';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';

import {ClientService} from '../../../services/client.service';
import {AccountService} from '../../../services/account.service';
import {TransactionService} from '../../../services/transaction.service';
import {BudgetService} from '../../../services/budget.service';
import {GoalService} from '../../../services/goal.service';
import {CategoryService} from '../../../services/category.service';

import {ClientGetDto} from '../../../models/client.model';
import {AccountGetDto} from '../../../models/account.model';
import {TransactionGetDto, TransactionCreateDto} from '../../../models/transaction.model';
import {BudgetGetDto} from '../../../models/budget.model';
import {GoalGetDto} from '../../../models/goal.model';
import {CategoryGetDto} from '../../../models/category.model';

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, RouterModule, ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.css']
})
export class ClientDetailComponent implements OnInit, OnDestroy {

  public Math = Math;
  totalBalance: number = 0;
  client: ClientGetDto | null = null;
  accounts: AccountGetDto[] = [];
  budgets: BudgetGetDto[] = [];
  clientGoals: GoalGetDto[] = [];
  transactions: TransactionGetDto[] = [];
  totalIncome: number = 0;
  totalExpenses: number = 0;
  isBrowser: boolean;
  isLoading = true;
  isLoadingGoals = false;
  goalsErrorMessage = '';
  errorMessage = '';
  chartData: ChartData<'doughnut'> = {
    labels: ['Income', 'Expenses'],
    datasets: [{
      data: [0, 0],
      backgroundColor: ['#4CAF50', '#F44336']
    }]};
  chartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    plugins: {
      legend: {display: true},
    },
  };

  categories: CategoryGetDto[] = [];
  isLoadingCategories = false;
  categoryLoadErrorMessage = '';
  showAddFundsForm = false;
  selectedGoalForFunding: GoalGetDto | null = null;
  addFundsForm: FormGroup;
  isSubmittingFunds = false;
  addFundsErrorMessage = '';

  private clientId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly clientService: ClientService,
    private readonly accountService: AccountService,
    private readonly transactionService: TransactionService,
    private readonly budgetService: BudgetService,
    private readonly goalService: GoalService,
    private readonly fb: FormBuilder,
    private readonly categoryService: CategoryService,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    this.addFundsForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      accountId: [null, Validators.required],
      categoryId: [null, Validators.required]
    });

  }

  ngOnInit(): void {
    this.loadCategories();
    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        const idParam = params.get('id');
        if (!idParam) {
          this.errorMessage = 'Client ID not found in URL.';
          this.stopLoadingOnError();
          return EMPTY;
        }
        this.clientId = parseInt(idParam, 10);
        if (isNaN(this.clientId)) {
          this.errorMessage = 'Invalid Client ID in URL.';
          this.stopLoadingOnError();
          return EMPTY;
        }
        this.resetStateBeforeLoad();
        return this.loadClientData(this.clientId);
      }),
      catchError(err => {
        this.errorMessage = err.message || 'Failed to load client data.';
        this.goalsErrorMessage = 'Could not load goals due to a general error.';
        this.stopLoadingOnError();
        return EMPTY;
      })
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  resetStateBeforeLoad(): void {
    this.isLoading = true;
    this.isLoadingGoals = true;
    this.errorMessage = '';
    this.goalsErrorMessage = '';
    this.client = null;
    this.accounts = [];
    this.budgets = [];
    this.clientGoals = [];
    this.transactions = [];
    this.totalBalance = 0;
    this.totalIncome = 0;
    this.totalExpenses = 0;
  }

  stopLoadingOnError(): void {
    this.isLoading = false;
    this.isLoadingGoals = false;
    this.isLoadingCategories = false;
  }


  loadCategories(): void {
    if (this.isLoadingCategories || this.categories.length > 0) return;
    this.isLoadingCategories = true;
    this.categoryLoadErrorMessage = '';
    this.categoryService.getCategories().pipe(
      takeUntil(this.destroy$),
      tap(cats => {
        this.categories = cats;
        this.isLoadingCategories = false;
      }),
      catchError(err => {
        console.error("Error loading categories", err);
        this.categoryLoadErrorMessage = `Failed to load categories: ${err.message || 'Error'}`;
        this.isLoadingCategories = false;
        this.categories = [];
        return EMPTY;
      })
    ).subscribe();
  }

  openAddFundsModal(goal: GoalGetDto): void {
    this.selectedGoalForFunding = goal;
    this.addFundsForm.reset();
    this.addFundsErrorMessage = '';
    this.showAddFundsForm = true;
    if (this.accounts.length === 1) {
      this.addFundsForm.patchValue({ accountId: this.accounts[0].id });
    }
    if (this.categories.length === 0 && !this.isLoadingCategories) {
      this.loadCategories(); // Пробуем загрузить, если не загружены
    }
  }

  closeAddFundsModal(): void {
    this.showAddFundsForm = false;
    this.selectedGoalForFunding = null;
    this.addFundsErrorMessage = '';
    this.addFundsForm.reset();
  }

  submitAddFunds(): void {
    if (this.addFundsForm.invalid || !this.selectedGoalForFunding) {
      this.addFundsForm.markAllAsTouched();
      this.addFundsErrorMessage = 'Please enter a valid amount and select an account and category.';
      return;
    }

    this.isSubmittingFunds = true;
    this.addFundsErrorMessage = '';

    const formValue = this.addFundsForm.value;
    const amountToSave = Math.abs(formValue.amount);

    const transactionData: TransactionCreateDto = {
      accountId: formValue.accountId,
      amount: amountToSave,
      categoryId: formValue.categoryId,
      description: `On goal: ${this.selectedGoalForFunding.name}`,
      date: new Date().toISOString()
    };

    this.goalService.addFundsToGoal(this.selectedGoalForFunding.id, transactionData)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => {
          console.log('Funds added, reloading client data...');
          if (this.clientId === null) return EMPTY;
          return this.loadClientData(this.clientId);
        }),
        tap(() => {
          this.isSubmittingFunds = false;
          this.closeAddFundsModal();
        }),
        catchError(err => {
          console.error('Error adding funds to goal:', err);
          this.addFundsErrorMessage = `Failed to add funds: ${err.message || 'Unknown error'}`;
          this.isSubmittingFunds = false;
          return EMPTY;
        })
      )
      .subscribe();
  }

  loadClientData(id: number): Observable<any> {
    this.resetStateBeforeLoad();

    return forkJoin({
      client: this.clientService.getClientById(id),
      allAccounts: this.accountService.getAllAccounts(),
      allTransactions: this.transactionService.getTransactions(),
      goals: this.goalService.getGoalsByClientId(id),
      allBudgets: this.budgetService.getAllBudgets()
    }).pipe(
      tap(results => {
        this.client = results.client;
        this.accounts = results.allAccounts.filter(acc => acc.clientId === id);
        const accountIds = new Set(this.accounts.map(acc => acc.id));
        this.transactions = results.allTransactions.filter(tx => accountIds.has(tx.accountId));
        this.budgets = results.allBudgets.filter(budget => budget.clientIds.includes(id));
        this.clientGoals = results.goals;
        this.calculateTotalBalance();
        this.calculateFinancialSummary(this.transactions);
        this.isLoading = false;
        this.isLoadingGoals = false;
      })

    );
  }

  calculateTotalBalance() {
    this.totalBalance = this.accounts.reduce((sum, account) => sum + account.balance, 0);
  }

  calculateFinancialSummary(transactions: TransactionGetDto[]): void {
    this.totalIncome = 0;
    this.totalExpenses = 0;
    transactions.forEach(tx => {
      if (tx.amount > 0) {
        this.totalIncome += tx.amount;
      } else if (tx.amount < 0) {
        this.totalExpenses += tx.amount;
      }
    });

    this.chartData = {
      labels: ['Income', 'Expenses'],
      datasets: [{
        data: [this.totalIncome, Math.abs(this.totalExpenses)],
        backgroundColor: ['#4CAF50', '#F44336']
      }]
    };
  }

  goBack(): void {
    this.router.navigate(['/clients']);
  }

  trackById(index: number, item: { id: number }): number {
    return item.id;
  }
}
