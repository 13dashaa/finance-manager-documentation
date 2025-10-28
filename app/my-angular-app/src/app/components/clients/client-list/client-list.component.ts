import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {EMPTY, Observable, Subject} from 'rxjs';
import {catchError, switchMap, takeUntil, tap} from 'rxjs/operators';
import {RouterModule} from '@angular/router';
import {ClientService} from '../../../services/client.service'; // Адаптируйте путь
import {ClientCreateDto, ClientGetDto, ClientUpdateDto} from '../../../models/client.model'; // Адаптируйте путь

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css'] // Используем те же стили или создаем новые
})
export class ClientListComponent implements OnInit, OnDestroy {

  clients: ClientGetDto[] = [];
  isLoading = false;
  errorMessage = '';
  clientForm: FormGroup;
  showAddEditForm = false; // Единый флаг для формы
  isEditMode = false;
  currentClientId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly clientService: ClientService,
    private readonly fb: FormBuilder
  ) {
    this.clientForm = this.fb.group({
      username: ['', [Validators.required, Validators.pattern(/^\S*$/)]], // Добавим паттерн без пробелов, если нужно
      email: ['', [Validators.required, Validators.email]],
      // Пароль обязателен только при создании
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]]
    });
  }

  ngOnInit(): void {
    this.loadClients();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadClients(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.clientService.getClients()
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.isLoading = false),
        catchError(err => {
          console.error('Error loading clients:', err);
          this.errorMessage = err.message || 'Failed to load clients.';
          this.isLoading = false;
          return EMPTY;
        })
      )
      .subscribe(data => {
        this.clients = data;
      });
  }

  deleteClient(id: number): void {
    if (confirm(`Are you sure you want to delete client ${id}?`)) {
      this.isLoading = true;
      this.errorMessage = '';
      this.clientService.deleteClient(id)
        .pipe(
          takeUntil(this.destroy$),
          // Успешно удалили, перезагружаем список
          switchMap(() => {
            // Не возвращаем результат delete, а инициируем новый запрос getClients
            return this.clientService.getClients();
          }),
          tap(updatedClients => {
            this.clients = updatedClients; // Обновляем список
            this.isLoading = false;
          }),
          catchError(err => {
            console.error('Error deleting client:', err);
            this.errorMessage = err.message || `Failed to delete client ${id}.`;
            this.isLoading = false;
            // Важно вернуть EMPTY или другой Observable, чтобы внешняя подписка не сломалась
            return EMPTY;
          })
        )
        // Подписываемся только для обработки catchError или если нужен финальный tap
        // Основная логика обновления списка теперь в tap внутри switchMap
        .subscribe({
          // Пустая подписка, если вся логика в pipe
          // error: обработка уже в catchError
        });
    }
  }

  openAddForm(): void {
    this.isEditMode = false;
    this.currentClientId = null;
    this.clientForm.reset(); // Сбрасываем форму
    // Убедимся, что валидатор пароля активен
    this.clientForm.get('password')?.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(64)]);
    this.clientForm.get('password')?.updateValueAndValidity();
    this.showAddEditForm = true;
    this.errorMessage = ''; // Очищаем ошибки при открытии формы
  }

  openEditForm(client: ClientGetDto): void {
    this.isEditMode = true;
    this.currentClientId = client.id;
    // Заполняем форму данными клиента. Пароль не трогаем.
    this.clientForm.patchValue({
      username: client.username,
      email: client.email // Хотя email не обновляется, покажем его в форме
    });
    // Пароль не обязателен и не редактируется, убираем валидаторы
    this.clientForm.get('password')?.clearValidators();
    this.clientForm.get('password')?.updateValueAndValidity();
    this.clientForm.get('password')?.reset(); // Очищаем поле пароля для ясности

    this.showAddEditForm = true;
    this.errorMessage = ''; // Очищаем ошибки при открытии формы
  }

  cancelForm(): void {
    this.showAddEditForm = false;
    this.isEditMode = false;
    this.currentClientId = null;
    this.clientForm.reset();
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    let operation$: Observable<ClientGetDto>;

    if (this.isEditMode && this.currentClientId !== null) {
      // --- Режим Редактирования ---
      const payload: ClientUpdateDto = {
        username: this.clientForm.value.username
      };
      operation$ = this.clientService.updateClient(this.currentClientId, payload);

    } else {
      // --- Режим Добавления ---
      if (!this.clientForm.value.password) {
        this.errorMessage = 'Password is required for new clients.';
        this.isLoading = false;
        this.clientForm.get('password')?.markAsTouched();
        return;
      }
      const payload: ClientCreateDto = {
        username: this.clientForm.value.username,
        email: this.clientForm.value.email,
        password: this.clientForm.value.password
      };
      operation$ = this.clientService.createClient(payload);
    }

    // Общая логика для обоих случаев
    operation$.pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.clientService.getClients()),
      tap(updatedClients => {
        this.clients = updatedClients;
        this.isLoading = false;
        this.cancelForm();
      }),
      catchError(err => {
        console.error('Error saving client:', err);
        this.isLoading = false;

        // Обработка ошибки дублирования username
        if (err.message?.includes('Key (username)')) {
          const username = this.clientForm.value.username;
          this.errorMessage = `Username "${username}" already exists.`;
        } else {
          this.errorMessage = err.message || `Failed to ${this.isEditMode ? 'update' : 'create'} client.`;
        }

        return EMPTY;
      })
    ).subscribe();
  }

  // Метод для отслеживания элементов в *ngFor (оптимизация)
  trackById(index: number, item: ClientGetDto): number {
    return item.id;
  }
}
