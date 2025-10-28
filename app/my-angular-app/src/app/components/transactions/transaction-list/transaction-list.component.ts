import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged, EMPTY, Observable, Subject} from 'rxjs';
import {catchError, switchMap, takeUntil, tap} from 'rxjs/operators';

import {TransactionService} from '../../../services/transaction.service';
import {AccountService} from '../../../services/account.service';
import {CategoryService} from '../../../services/category.service';
import {TransactionCreateDto, TransactionGetDto} from '../../../models/transaction.model';
import {AccountGetDto} from '../../../models/account.model';
import {CategoryGetDto} from '../../../models/category.model';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule

  ],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit, OnDestroy {

  transactions: TransactionGetDto[] = [];
  accounts$: Observable<AccountGetDto[]> | undefined;
  categories$: Observable<CategoryGetDto[]> | undefined;
  isEditMode = false;
  currentTransactionId: number | null = null;
  isLoading = false;
  errorMessage = '';
  transactionForm: FormGroup;
  showAddForm = false;
  filterForm: FormGroup;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly transactionService: TransactionService,
    private readonly accountService: AccountService,
    private readonly categoryService: CategoryService,
    private readonly fb: FormBuilder
  ) {
    this.transactionForm = this.fb.group({
      description: [''],
      amount: [null, [Validators.required, Validators.pattern(/^-?\d+(\.\d+)?$/)]],
      date: [this.getCurrentDateTimeLocal(), Validators.required],
      categoryId: [null, [Validators.required, Validators.min(1)]],
      accountId: [null, [Validators.required, Validators.min(1)]]
    });
    this.filterForm = this.fb.group({
      selectedAccountId: [null],
      selectedCategoryId: [null]
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadInitialData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.loadTransactions();
    this.loadAccounts();
    this.loadCategories();
  }

  loadTransactions(): void {
    this.isLoading = true;
    this.errorMessage = '';
    const currentFilters = {
      accountId: this.filterForm.value.selectedAccountId,
      categoryId: this.filterForm.value.selectedCategoryId
    };

    console.log('Loading transactions with filters:', currentFilters);

    this.transactionService.getTransactions(currentFilters)
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.isLoading = false),
        catchError(err => {
          console.error('Error loading transactions:', err);
          this.errorMessage = 'Failed to load transactions. Please try again later.';
          this.transactions = [];
          this.isLoading = false;
          return EMPTY;
        })
      )
      .subscribe(data => {
        this.transactions = data;
        console.log('Transactions loaded:', this.transactions.length);
      });
  }

  setupFilterListener(): void {
    this.filterForm.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
      tap(filters => console.log('Filter changed, reloading transactions...', filters)),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.loadTransactions();
    });
  }
  loadAccounts(): void {
    this.accounts$ = this.accountService.getAllAccounts().pipe(
      catchError(err => {
        console.error('Error loading accounts:', err);
        return EMPTY;
      })
    );
  }

  loadCategories(): void {
    this.categories$ = this.categoryService.getCategories().pipe(
      catchError(err => {
        console.error('Error loading categories:', err);
        return EMPTY;
      })
    );
  }

  openEditForm(transaction: TransactionGetDto): void {
    this.isEditMode = true;
    this.currentTransactionId = transaction.id;
    let formattedDate = '';
    try {
      formattedDate = transaction.date.slice(0, 16);
    } catch (e) {
      console.error("Error formatting date for input:", transaction.date, e);
    }
    this.transactionForm.patchValue({
      accountId: transaction.accountId,
      categoryId: transaction.categoryId,
      amount: transaction.amount,
      description: transaction.description,
      date: formattedDate
    });
    this.showAddForm = true;
    this.errorMessage = '';
  }

  deleteTransaction(id: number): void {
    if (confirm(`Are you sure you want to delete transaction ${id}?`)) {
      this.isLoading = true;
      this.transactionService.deleteTransaction(id)
        .pipe(
          takeUntil(this.destroy$),
          switchMap(() => this.transactionService.getTransactions()),
          tap(() => this.isLoading = false),
          catchError(err => {
            console.error('Error deleting transaction:', err);
            this.errorMessage = `Failed to delete transaction ${id}.`;
            this.isLoading = false;
            return EMPTY;
          })
        )
        .subscribe(updatedTransactions => {
          this.transactions = updatedTransactions;
          this.errorMessage = '';
        });
    }
  }

  onSubmit(): void {
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const payload: TransactionCreateDto = {
      accountId: this.transactionForm.value.accountId,
      categoryId: this.transactionForm.value.categoryId,
      amount: this.transactionForm.value.amount,
      description: this.transactionForm.value.description || null,
      date: this.transactionForm.value.date
    };

    let operation$: Observable<any>;

    if (this.isEditMode && this.currentTransactionId !== null) {
      operation$ = this.transactionService.updateTransaction(this.currentTransactionId, payload);
    } else {
      operation$ = this.transactionService.createTransaction(payload);
    }

    operation$.pipe(
      takeUntil(this.destroy$),
      tap(() => {
        this.isLoading = false;
        this.cancelForm();
        this.loadTransactions();
      }),
      catchError(err => {
        console.error('Error saving transaction:', err);
        this.errorMessage = err.message || `Failed to ${this.isEditMode ? 'update' : 'create'} transaction.`;
        this.isLoading = false;
        return EMPTY;
      })
    ).subscribe();
  }

  cancelForm(): void {
    this.showAddForm = false;
    this.isEditMode = false;
    this.currentTransactionId = null;
    this.transactionForm.reset({date: this.getCurrentDateTimeLocal()});
    this.errorMessage = '';
  }

  getCurrentDateTimeLocal(): string {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000;
    const localDate = new Date(now.getTime() - offset);
    return localDate.toISOString().slice(0, 16);
  }

  trackById(index: number, item: TransactionGetDto | AccountGetDto | CategoryGetDto): number {
    return item.id;
  }

  private formatDateTimeForBackend(dateString: string): string {
    if (dateString && dateString.includes('T') && dateString.length >= 16) {
      return dateString.length === 16 ? dateString + ':00' : dateString.slice(0, 19);
    }
    console.warn("Invalid date format for backend:", dateString);
    return new Date().toISOString().slice(0, 19);
  }
}
