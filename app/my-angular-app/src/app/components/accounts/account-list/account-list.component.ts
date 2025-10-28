// src/app/features/accounts/account-list/account-list.component.ts
import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, CurrencyPipe} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {EMPTY, Observable, Subject} from 'rxjs';
import {catchError, switchMap, takeUntil, tap} from 'rxjs/operators';
import {RouterModule} from '@angular/router';

import {AccountService} from '../../../services/account.service'; // Adjust path
import {ClientService} from '../../../services/client.service'; // Adjust path
import {AccountCreateDto, AccountGetDto, AccountUpdateDto} from '../../../models/account.model'; // Adjust path
import {ClientGetDto} from '../../../models/client.model'; // Adjust path

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    CurrencyPipe
  ],
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css', ]
})
export class AccountListComponent implements OnInit, OnDestroy {

  accounts: AccountGetDto[] = [];
  clients: ClientGetDto[] = []; // For dropdowns
  isLoading = false; // General loading for accounts list and save/delete
  isLoadingClients = false; // Specific loading for clients
  errorMessage = ''; // General errors
  clientLoadErrorMessage = ''; // Specific client loading error
  accountForm: FormGroup;
  showAddEditForm = false;
  isEditMode = false;
  currentAccountId: number | null = null;
  selectedClientIdFilter: number | null = null; // <-- ADDED: State for filtering
  protected readonly Array = Array;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly accountService: AccountService,
    private readonly clientService: ClientService,
    private readonly fb: FormBuilder
  ) {
    this.accountForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(100)]],
      balance: [null, [Validators.required, Validators.min(0.01)]],
      clientId: [null] // Validators added/removed dynamically
    });
  }

  isAnArray(value: any): boolean {
    return Array.isArray(value);
  }

  ngOnInit(): void {
    // Load initial data: all accounts and clients for dropdowns
    this.loadAccounts(); // Initial load uses selectedClientIdFilter = null (all accounts)
    this.loadClientsForDropdown();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // MODIFIED: Load accounts based on filter
  loadAccounts(): void {
    this.isLoading = true;
    this.clearErrors(); // Clear errors before loading

    let accountsObservable$: Observable<AccountGetDto[]>;

    // Choose the correct service method based on the filter
    if (this.selectedClientIdFilter === null || this.selectedClientIdFilter === undefined) {
      // --- Load All Accounts ---
      accountsObservable$ = this.accountService.getAllAccounts(); // Use correct method name
    } else {
      // --- Load Filtered Accounts ---
      accountsObservable$ = this.accountService.getAccountsByClientId(this.selectedClientIdFilter); // Use correct method name
    }

    accountsObservable$.pipe(
      takeUntil(this.destroy$),
      tap(() => this.isLoading = false), // Stop loading indicator on success or error
      catchError(err => {
        console.error('Error loading accounts:', err);
        this.errorMessage = `Failed to load accounts: ${err.message || 'Unknown error'}`;
        this.isLoading = false;
        this.accounts = []; // Clear accounts on error
        return EMPTY;
      })
    )
      .subscribe(data => {
        this.accounts = data;
      });
  }

  loadClientsForDropdown(): void {
    this.isLoadingClients = true;
    this.clientLoadErrorMessage = ''; // Clear specific client error
    this.clientService.getClients()
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.isLoadingClients = false),
        catchError(err => {
          console.error('Error loading clients for dropdown:', err);
          this.clientLoadErrorMessage = `Failed to load client list: ${err.message || 'Unknown error'}`;
          this.isLoadingClients = false;
          this.clients = []; // Clear clients on error
          return EMPTY;
        })
      )
      .subscribe(data => {
        this.clients = data;
      });
  }

  // ADDED: Handle filter selection change
  onClientFilterChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const value = selectElement.value;
    // Parse value to number or null
    this.selectedClientIdFilter = value === 'null' || value === '' ? null : parseInt(value, 10);
    // Reload accounts with the new filter
    this.loadAccounts();
  }

  deleteAccount(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete the account "${name}" (ID: ${id})?`)) {
      this.isLoading = true;
      this.clearErrors();
      this.accountService.deleteAccount(id)
        .pipe(
          takeUntil(this.destroy$),
          // Reload accounts respecting the current filter AFTER successful deletion
          switchMap(() => {
            // Choose the correct observable based on the current filter
            if (this.selectedClientIdFilter === null || this.selectedClientIdFilter === undefined) {
              return this.accountService.getAllAccounts();
            } else {
              return this.accountService.getAccountsByClientId(this.selectedClientIdFilter);
            }
          }),
          tap(updatedAccounts => {
            this.accounts = updatedAccounts;
            this.isLoading = false;
            console.log(`Account ${id} deleted successfully.`);
          }),
          catchError(err => {
            console.error(`Error deleting account ${id}:`, err);
            this.errorMessage = `Failed to delete account ${id}: ${err.message || 'Unknown error'}`;
            this.isLoading = false;
            return EMPTY;
          })
        )
        .subscribe();
    }
  }

  openAddForm(): void {
    this.isEditMode = false;
    this.currentAccountId = null;
    this.accountForm.reset({balance: null, clientId: null});
    this.accountForm.get('clientId')?.setValidators([Validators.required]);
    this.accountForm.get('clientId')?.enable();
    this.accountForm.get('clientId')?.updateValueAndValidity();
    this.showAddEditForm = true;
    this.clearErrors();
    if (this.clients.length === 0 && !this.isLoadingClients) {
      this.loadClientsForDropdown();
    }
  }

  openEditForm(account: AccountGetDto): void {
    this.isEditMode = true;
    this.currentAccountId = account.id;
    this.accountForm.reset();
    this.accountForm.patchValue({
      name: account.name,
      balance: account.balance,
      clientId: account.clientId
    });
    this.accountForm.get('clientId')?.disable();
    this.accountForm.get('clientId')?.clearValidators();
    this.accountForm.get('clientId')?.updateValueAndValidity();
    this.showAddEditForm = true;
    this.clearErrors();
  }

  cancelForm(): void {
    this.showAddEditForm = false;
    this.isEditMode = false;
    this.currentAccountId = null;
    this.accountForm.reset({balance: null, clientId: null});
    this.accountForm.get('clientId')?.enable();
    this.accountForm.get('clientId')?.clearValidators();
    this.accountForm.get('clientId')?.updateValueAndValidity();
    this.clearErrors();
  }

  onSubmit(): void {
    this.clearErrors();
    this.accountForm.markAllAsTouched();

    if (this.accountForm.invalid) {
      this.findFirstError();
      if (!this.errorMessage) {
        this.errorMessage = 'Please fill in all required fields correctly.';
      }
      return;
    }

    this.isLoading = true;
    const formValue = this.accountForm.getRawValue();

    let operation$: Observable<AccountGetDto>;

    if (this.isEditMode && this.currentAccountId !== null) {
      const payload: AccountUpdateDto = {
        name: formValue.name,
        balance: formValue.balance
      };
      operation$ = this.accountService.updateAccount(this.currentAccountId, payload);
    } else {
      const payload: AccountCreateDto = {
        name: formValue.name,
        balance: formValue.balance,
        clientId: formValue.clientId
      };
      operation$ = this.accountService.createAccount(payload);
    }

    operation$.pipe(
      takeUntil(this.destroy$),
      // Reload accounts respecting the current filter AFTER successful save/update
      switchMap(() => {
        // Choose the correct observable based on the current filter
        if (this.selectedClientIdFilter === null || this.selectedClientIdFilter === undefined) {
          return this.accountService.getAllAccounts();
        } else {
          // If adding/editing an account for a different client than the filter,
          // the list might appear empty after reload unless filter is reset.
          // For simplicity, we reload based on the *current* filter.
          // Consider resetting filter to null or the updated/added client ID if desired.
          return this.accountService.getAccountsByClientId(this.selectedClientIdFilter);
        }
      }),
      tap(updatedAccounts => {
        this.accounts = updatedAccounts;
        this.isLoading = false;
        this.cancelForm();
        console.log(`Account ${this.isEditMode ? 'updated' : 'created'} successfully.`);
      }),
      catchError(err => {
        console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} account:`, err);
        this.errorMessage = `Failed to ${this.isEditMode ? 'update' : 'create'} account: ${err.message || 'Unknown error'}`;
        this.isLoading = false;
        if (this.isEditMode) {
          this.accountForm.get('clientId')?.disable();
        } else {
          this.accountForm.get('clientId')?.enable();
        }
        return EMPTY;
      })
    ).subscribe();
  }

  clearErrors(): void {
    this.errorMessage = '';
    this.clientLoadErrorMessage = '';
  }

  findFirstError(): void {
    const controls = this.accountForm.controls;
    for (const name in controls) {
      if (controls[name].invalid && controls[name].enabled) {
        const controlErrors = controls[name].errors;
        const label = this.getControlLabel(name);
        if (controlErrors?.['required']) {
          this.errorMessage = `Field '${label}' is required.`;
        } else if (controlErrors?.['min']) {
          this.errorMessage = `Field '${label}' must be at least ${controlErrors['min'].min}.`;
        } else if (controlErrors?.['maxlength']) {
          this.errorMessage = `Field '${label}' cannot exceed ${controlErrors['maxlength'].requiredLength} characters.`;
        } else if (controlErrors?.['minlength']) {
          this.errorMessage = `Field '${label}' must be at least ${controlErrors['minlength'].requiredLength} characters.`;
        } else {
          this.errorMessage = `Field '${label}' has an invalid value.`;
        }
        return;
      }
    }
  }

  getControlLabel(name: string): string {
    switch (name) {
      case 'name':
        return 'Account Name';
      case 'balance':
        return 'Balance';
      case 'clientId':
        return 'Client';
      default:
        return name;
    }
  }

  trackById(index: number, item: AccountGetDto | ClientGetDto): number {
    return item.id;
  }

  formatTransactionDescriptions(descriptions: string[] | undefined | null): string {
    if (!descriptions || !Array.isArray(descriptions) || descriptions.length === 0) {
      return 'None';
    }
    const maxDisplay = 3;
    let displayStr = descriptions.slice(0, maxDisplay).join(', ');
    if (descriptions.length > maxDisplay) {
      displayStr += '...';
    }
    return displayStr;
  }

  getClientUsername(clientId: number | null): string | null {
    if (clientId === null) {
      return null;
    }
    const client = this.clients.find(c => c.id === clientId);
    return client ? client.username : '(Client not found)';
  }
}
