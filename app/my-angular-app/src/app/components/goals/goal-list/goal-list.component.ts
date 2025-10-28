import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, CurrencyPipe, DatePipe} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {EMPTY, Observable, Subject} from 'rxjs';
import {catchError, takeUntil, tap} from 'rxjs/operators';
import {RouterModule} from '@angular/router';

import {GoalService} from '../../../services/goal.service';
import {ClientService} from '../../../services/client.service';
import {GoalCreateDto, GoalGetDto} from '../../../models/goal.model';
import {futureDateValidator} from '../../../validators/future-date.validator';
import {ClientGetDto} from '../../../models/client.model';

@Component({
  selector: 'app-goal-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    CurrencyPipe,
    DatePipe
  ],
  templateUrl: './goal-list.component.html',
  styleUrls: ['./goal-list.component.css']
})
export class GoalListComponent implements OnInit, OnDestroy {

  goals: GoalGetDto[] = [];
  clients: ClientGetDto[] = [];
  isLoading = false;
  isLoadingClients = false;
  errorMessage = '';
  clientLoadErrorMessage = '';
  formErrorMessage = '';
  goalForm: FormGroup;
  showAddEditForm = false;
  isEditMode = false;
  currentGoalId: number | null = null;
  selectedClientIdFilter: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly goalService: GoalService,
    private readonly clientService: ClientService,
    private readonly fb: FormBuilder
  ) {
    this.goalForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(100)]],
      targetAmount: [null, [Validators.required, Validators.min(0.01)]],
      startDate: [''],
      endDate: ['', [Validators.required, futureDateValidator()]],
      clientId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadGoals();
    this.loadClientsForDropdown();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadGoals(): void {
    this.isLoading = true;
    this.errorMessage = '';

    let goalsObservable$: Observable<GoalGetDto[]>;

    if (this.selectedClientIdFilter === null || this.selectedClientIdFilter === undefined) {
      goalsObservable$ = this.goalService.getAllGoals();
    } else {
      goalsObservable$ = this.goalService.getGoalsByClientId(this.selectedClientIdFilter);
    }


    goalsObservable$.pipe(
      takeUntil(this.destroy$),
      tap(() => this.isLoading = false),
      catchError(err => {
        console.error('Error loading goals:', err);
        this.errorMessage = `Failed to load goals: ${err.message || 'Unknown error'}`;
        this.isLoading = false;
        this.goals = [];
        return EMPTY;
      })
    )
      .subscribe(data => {
        this.goals = data;
      });
  }
  getClientName(clientId: number | null): string {
    if (!clientId) return 'Unknown Client';
    const client = this.clients.find(c => c.id === clientId);
    return client ? `${client.username}` : 'Client not found';
  }

  loadClientsForDropdown(): void {
    this.isLoadingClients = true;
    this.clientLoadErrorMessage = '';
    this.clientService.getClients()
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.isLoadingClients = false),
        catchError(err => {
          console.error('Error loading clients for dropdown:', err);
          this.clientLoadErrorMessage = `Failed to load client list: ${err.message || 'Unknown error'}`;
          this.isLoadingClients = false;
          this.clients = [];
          return EMPTY;
        })
      )
      .subscribe(data => {
        this.clients = data;
      });
  }

  onClientFilterChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const value = selectElement.value;
    this.selectedClientIdFilter = value === 'null' || value === '' ? null : parseInt(value, 10);
    this.loadGoals();
  }


  deleteGoal(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete the goal "${name}" (ID: ${id})?`)) {
      this.isLoading = true;
      this.errorMessage = '';
      this.formErrorMessage = '';
      this.goalService.deleteGoal(id)
        .pipe(
          takeUntil(this.destroy$),
          tap(() => {
            console.log(`Goal ${id} deleted request sent.`);
            this.loadGoals();
          }),
          catchError(err => {
            console.error('Error deleting goal:', err);
            this.errorMessage = `Failed to delete goal ${id}: ${err.message || 'Unknown error'}`;
            this.isLoading = false; // Устанавливаем isLoading в false при ошибке
            return EMPTY;
          })
        )
        .subscribe({
          complete: () => { }
        });
    }
  }


  openAddForm(): void {
    this.isEditMode = false;
    this.currentGoalId = null;
    this.goalForm.reset();
    this.showAddEditForm = true;
    this.errorMessage = '';
    this.formErrorMessage = '';
    if (this.clients.length === 0 && !this.isLoadingClients) {
      this.loadClientsForDropdown();
    }
  }

  openEditForm(goal: GoalGetDto): void {
    this.isEditMode = true;
    this.currentGoalId = goal.id;
    this.goalForm.reset();
    this.goalForm.patchValue({
      name: goal.name,
      targetAmount: goal.targetAmount,
      startDate: goal.startDate ? goal.startDate.split('T')[0] : null,
      endDate: goal.endDate ? goal.endDate.split('T')[0] : null,
      clientId: goal.clientId
    });
    this.showAddEditForm = true;
    this.errorMessage = '';
    this.formErrorMessage = '';
    if (this.clients.length === 0 && !this.isLoadingClients) {
      this.loadClientsForDropdown();
    }
  }

  cancelForm(): void {
    this.showAddEditForm = false;
    this.isEditMode = false;
    this.currentGoalId = null;
    this.goalForm.reset();
    this.errorMessage = '';
    this.formErrorMessage = '';
  }

  onSubmit(): void {
    if (this.goalForm.invalid) {
      this.goalForm.markAllAsTouched();
      this.findFirstError();
      if (!this.formErrorMessage) {
        this.formErrorMessage = 'Please fill in all required fields correctly.';
      }
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.formErrorMessage = '';
    let operation$: Observable<GoalGetDto>;

    const payload: GoalCreateDto = {
      name: this.goalForm.value.name,
      targetAmount: this.goalForm.value.targetAmount,
      startDate: this.goalForm.value.startDate || null,
      endDate: this.goalForm.value.endDate,
      clientId: this.goalForm.value.clientId
    };

    if (this.isEditMode && this.currentGoalId !== null) {
      operation$ = this.goalService.updateGoal(this.currentGoalId, payload);
    } else {
      operation$ = this.goalService.createGoal(payload);
    }

    operation$.pipe(
      takeUntil(this.destroy$),
      tap(() => {
        console.log(`Goal ${this.isEditMode ? 'updated' : 'created'} successfully.`);
        this.cancelForm();
        this.loadGoals();
      }),
      catchError(err => {
        console.error('Error saving goal:', err);
        this.formErrorMessage = `Failed to ${this.isEditMode ? 'update' : 'create'} goal: ${err.message || 'Unknown error'}`;
        this.isLoading = false; // Снимаем флаг загрузки
        return EMPTY;
      })
    ).subscribe({
      complete: () => { if (this.isLoading) this.isLoading = false; }
    });
  }

  findFirstError(): void {
    this.formErrorMessage = '';
    const controls = this.goalForm.controls;
    for (const name in controls) {
      if (controls[name].invalid && controls[name].enabled) {
        const errors = controls[name].errors;
        const label = this.getControlLabel(name);
        if (errors?.['required']) { this.formErrorMessage = `Field '${label}' is required.`; }
        else if (errors?.['min']) { this.formErrorMessage = `Field '${label}' must be at least ${errors['min'].min}.`; }
        else if (errors?.['maxlength']) { this.formErrorMessage = `Field '${label}' cannot exceed ${errors['maxlength'].requiredLength} characters.`; }
        else if (errors?.['minlength']) { this.formErrorMessage = `Field '${label}' must be at least ${errors['minlength'].requiredLength} characters.`; }
        else if (errors?.['futureDate']) { this.formErrorMessage = `Field '${label}' must be a future date.`; }
        else { this.formErrorMessage = `Field '${label}' has an invalid value.`; }
        return;
      }
    }
  }

  getControlLabel(name: string): string {
    switch (name) {
      case 'name': return 'Name';
      case 'targetAmount': return 'Target Amount';
      case 'startDate': return 'Start Date';
      case 'endDate': return 'End Date';
      case 'clientId': return 'Client';
      default: return name;
    }
  }

  trackById(index: number, item: GoalGetDto | ClientGetDto): number {
    return item.id;
  }
}
