import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { EMPTY, Subject, Observable, forkJoin } from 'rxjs';
import { catchError, takeUntil, tap } from 'rxjs/operators';
import { RouterModule } from '@angular/router';

import { BudgetService } from '../../../services/budget.service';
import { ClientService } from '../../../services/client.service';
import { CategoryService } from '../../../services/category.service';
import { BudgetCreateDto, BudgetGetDto, BudgetUpdateDto } from '../../../models/budget.model';
import { ClientGetDto } from '../../../models/client.model';
import { CategoryGetDto } from '../../../models/category.model';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    CurrencyPipe
  ],
  templateUrl: './budget-list.component.html',
  styleUrls: ['./budget-list.component.css']
})
export class BudgetListComponent implements OnInit, OnDestroy {

  budgets: BudgetGetDto[] = [];
  clients: ClientGetDto[] = [];
  categories: CategoryGetDto[] = [];

  isLoading = false;
  isLoadingClients = false;
  isLoadingCategories = false;
  isSubmitting = false;

  errorMessage = '';
  clientLoadErrorMessage = '';
  categoryLoadErrorMessage = '';
  formErrorMessage = '';

  budgetForm: FormGroup;
  showAddEditForm = false;
  isEditMode = false;
  currentBudgetId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly budgetService: BudgetService,
    private readonly clientService: ClientService,
    private readonly categoryService: CategoryService,
    private readonly fb: FormBuilder
  ) {
    this.budgetForm = this.fb.group({
      categoryId: [null, Validators.required],
      limitation: [null, [Validators.required, Validators.min(0.01)]],
      period: [30, [Validators.required, Validators.min(1)]], // По умолчанию 30 дней
      clientIds: this.fb.array([], Validators.required)
    });
  }

  get clientIdsFormArray(): FormArray {
    return this.budgetForm.get('clientIds') as FormArray;
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
    this.isLoadingClients = true;
    this.isLoadingCategories = true;
    this.clearErrors();

    forkJoin({
      budgets: this.budgetService.getAllBudgets(),
      clients: this.clientService.getClients(),
      categories: this.categoryService.getCategories()
    }).pipe(
      takeUntil(this.destroy$),
      tap(results => {
        this.budgets = results.budgets;
        this.clients = results.clients;
        this.categories = results.categories;
        this.isLoading = false;
        this.isLoadingClients = false;
        this.isLoadingCategories = false;
      }),
      catchError(err => {
        console.error('Error loading initial data:', err);
        this.errorMessage = `Failed to load data: ${err.message || 'Unknown error'}`;
        if (!this.clients.length) this.clientLoadErrorMessage = 'Failed to load clients.';
        if (!this.categories.length) this.categoryLoadErrorMessage = 'Failed to load categories.';
        this.isLoading = false;
        this.isLoadingClients = false;
        this.isLoadingCategories = false;
        return EMPTY;
      })
    ).subscribe();
  }


  loadBudgets(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.budgetService.getAllBudgets().pipe(
      takeUntil(this.destroy$),
      tap(() => this.isLoading = false),
      catchError(err => {
        this.errorMessage = `Failed to reload budgets: ${err.message || 'Unknown error'}`;
        this.isLoading = false;
        this.budgets = [];
        return EMPTY;
      })
    ).subscribe(data => {
      this.budgets = data;
    });
  }


  deleteBudget(id: number, categoryName: string): void {
    if (confirm(`Are you sure you want to delete the budget for category "${categoryName}" (ID: ${id})?`)) {
      this.isLoading = true;
      this.clearErrors();
      this.budgetService.deleteBudget(id)
        .pipe(
          takeUntil(this.destroy$),
          tap(() => {
            this.isLoading = false;
            console.log(`Budget ${id} deleted successfully.`);
            this.loadBudgets();
          }),
          catchError(err => {
            console.error(`Error deleting budget ${id}:`, err);
            this.errorMessage = `Failed to delete budget ${id}: ${err.message || 'Unknown error'}`;
            this.isLoading = false;
            return EMPTY;
          })
        )
        .subscribe();
    }
  }

  openAddForm(): void {
    this.isEditMode = false;
    this.currentBudgetId = null;
    this.budgetForm.reset({ period: 30 });
    this.clientIdsFormArray.clear();
    this.budgetForm.get('categoryId')?.enable();
    this.budgetForm.get('categoryId')?.setValidators(Validators.required);
    this.budgetForm.get('categoryId')?.updateValueAndValidity();
    this.showAddEditForm = true;
    this.clearErrors();
    if (this.clients.length === 0 && !this.isLoadingClients) this.loadClientsForDropdown();
    if (this.categories.length === 0 && !this.isLoadingCategories) this.loadCategoriesForDropdown();
  }

  openEditForm(budget: BudgetGetDto): void {
    this.isEditMode = true;
    this.currentBudgetId = budget.id;
    this.budgetForm.reset(); // Сначала сбрасываем
    this.clientIdsFormArray.clear(); // Очищаем массив перед заполнением

    this.budgetForm.patchValue({
      categoryId: budget.categoryId, // Для отображения, но поле будет отключено
      limitation: budget.limitation,
      period: budget.period,
    });

    budget.clientIds.forEach(clientId => {
      this.clientIdsFormArray.push(this.fb.control(clientId));
    });


    this.budgetForm.get('categoryId')?.disable();
    this.budgetForm.get('categoryId')?.clearValidators();
    this.budgetForm.get('categoryId')?.updateValueAndValidity();

    this.showAddEditForm = true;
    this.clearErrors();
    if (this.clients.length === 0 && !this.isLoadingClients) this.loadClientsForDropdown();
    if (this.categories.length === 0 && !this.isLoadingCategories) this.loadCategoriesForDropdown();
  }

  cancelForm(): void {
    this.showAddEditForm = false;
    this.isEditMode = false;
    this.currentBudgetId = null;
    this.budgetForm.reset({ period: 30 });
    this.clientIdsFormArray.clear();
    // Включаем категорию обратно на случай следующего добавления
    this.budgetForm.get('categoryId')?.enable();
    this.clearErrors();
  }

  onSubmit(): void {
    this.formErrorMessage = ''; // Очищаем ошибку формы
    this.budgetForm.markAllAsTouched();

    if (this.clientIdsFormArray.length === 0) {
      this.clientIdsFormArray.markAsTouched();
      this.formErrorMessage = 'Please select at least one client for the budget.';
      return;
    }


    if (this.budgetForm.invalid) {
      this.formErrorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.isSubmitting = true;

    const formValue = this.budgetForm.getRawValue();

    let operation$: Observable<BudgetGetDto>;

    if (this.isEditMode && this.currentBudgetId !== null) {
      const payload: BudgetUpdateDto = {
        limitation: formValue.limitation,
        period: formValue.period,
        clientIds: formValue.clientIds
      };
      operation$ = this.budgetService.updateBudget(this.currentBudgetId, payload);
    } else {
      const payload: BudgetCreateDto = {
        categoryId: formValue.categoryId,
        limitation: formValue.limitation,
        period: formValue.period,
        clientIds: formValue.clientIds
      };
      operation$ = this.budgetService.createBudget(payload);
    }

    operation$.pipe(
      takeUntil(this.destroy$),
      tap(() => {
        this.isSubmitting = false;
        console.log(`Budget ${this.isEditMode ? 'updated' : 'created'} successfully.`);
        this.cancelForm(); // Закрываем форму
        this.loadBudgets(); // Перезагружаем список
      }),
      catchError(err => {
        console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} budget:`, err);
        this.formErrorMessage = `Failed to ${this.isEditMode ? 'update' : 'create'} budget: ${err.message || 'Unknown error'}`;
        this.isSubmitting = false;

        if(this.isEditMode) {
          this.budgetForm.get('categoryId')?.disable();
        } else {
          this.budgetForm.get('categoryId')?.enable();
        }
        return EMPTY;
      })
    ).subscribe();
  }

  loadClientsForDropdown(): void {
    if (this.isLoadingClients) return;
    this.isLoadingClients = true;
    this.clientLoadErrorMessage = '';
    this.clientService.getClients().pipe(
      takeUntil(this.destroy$),
      tap(() => this.isLoadingClients = false),
      catchError(err => {
        this.clientLoadErrorMessage = `Failed to load clients: ${err.message || 'Error'}`;
        this.isLoadingClients = false;
        return EMPTY;
      })
    ).subscribe(data => this.clients = data);
  }

  loadCategoriesForDropdown(): void {
    if (this.isLoadingCategories) return;
    this.isLoadingCategories = true;
    this.categoryLoadErrorMessage = '';
    this.categoryService.getCategories().pipe(
      takeUntil(this.destroy$),
      tap(() => this.isLoadingCategories = false),
      catchError(err => {
        this.categoryLoadErrorMessage = `Failed to load categories: ${err.message || 'Error'}`;
        this.isLoadingCategories = false;
        return EMPTY;
      })
    ).subscribe(data => this.categories = data);
  }


  clearErrors(): void {
    this.errorMessage = '';
    this.clientLoadErrorMessage = '';
    this.categoryLoadErrorMessage = '';
    this.formErrorMessage = '';
  }

  trackById(index: number, item: BudgetGetDto | ClientGetDto | CategoryGetDto): number {
    return item.id;
  }

  onClientSelectionChange(event: Event): void {
    const selectedOptions = (event.target as HTMLSelectElement).selectedOptions;
    this.clientIdsFormArray.clear(); // Очищаем перед заполнением
    Array.from(selectedOptions).forEach(option => {
      this.clientIdsFormArray.push(this.fb.control(parseInt(option.value, 10)));
    });
  }

  isClientSelected(clientId: number): boolean {
    return this.clientIdsFormArray.value.includes(clientId);
  }

  formatClientUsernames(usernames: string[] | undefined | null): string {
    if (!usernames || usernames.length === 0) return 'None';
    return usernames.join(', ');
  }

  protected readonly Array = Array;
}
