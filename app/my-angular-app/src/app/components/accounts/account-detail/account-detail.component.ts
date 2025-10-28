// src/app/features/accounts/account-detail/account-detail.component.ts
import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {CommonModule, CurrencyPipe, DatePipe} from '@angular/common';
import {EMPTY, of, Subject} from 'rxjs'; // Импортируй 'of'
import {catchError, map, switchMap, takeUntil, tap} from 'rxjs/operators'; // Импортируй 'map'
import {AccountService} from '../../../services/account.service';
import {AccountGetDto} from '../../../models/account.model';
import {TransactionGetDto} from '../../../models/transaction.model';
import {TransactionService} from '../../../services/transaction.service';

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe],
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit, OnDestroy {
  account: AccountGetDto | null = null;
  transactions: TransactionGetDto[] = [];
  isLoading = false;
  isLoadingTransactions = false;
  errorMessage = '';
  transactionErrorMessage = '';
  private accountId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly accountService: AccountService,
    private readonly transactionService: TransactionService
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      tap(() => {
        this.isLoading = true;
        this.isLoadingTransactions = true; // Начинаем обе загрузки
        this.errorMessage = '';
        this.transactionErrorMessage = '';
        this.account = null;
        this.transactions = [];
      }),
      switchMap(params => {
        const idParam = params.get('id');
        if (!idParam) {
          this.errorMessage = 'Account ID not found in URL.';
          this.isLoading = false;
          this.isLoadingTransactions = false;
          return EMPTY;
        }
        this.accountId = parseInt(idParam, 10);
        if (isNaN(this.accountId)) {
          this.errorMessage = 'Invalid Account ID in URL.';
          this.isLoading = false;
          this.isLoadingTransactions = false;
          return EMPTY;
        }

        // Загружаем сначала детали аккаунта
        return this.accountService.getAccountById(this.accountId).pipe(
          tap(accountData => {
            this.account = accountData; // Сохраняем аккаунт
            this.isLoading = false; // Загрузка аккаунта завершена
            // НЕ вызываем загрузку транзакций здесь, сделаем это дальше
          }),
          catchError(err => { // Ошибка загрузки аккаунта
            this.errorMessage = `Failed to load account details: ${err.message || 'Unknown error'}`;
            this.isLoading = false;
            this.isLoadingTransactions = false; // Останавливаем и загрузку транзакций
            return EMPTY; // Прерываем поток
          })
        );
      }),
      // *** ИЗМЕНЕНО: Загружаем ВСЕ транзакции ПОСЛЕ успешной загрузки аккаунта ***
      switchMap(accountData => { // accountData здесь - это результат accountService.getAccountById
        if (!this.accountId) return EMPTY; // На всякий случай
        this.isLoadingTransactions = true; // Убедимся, что флаг установлен
        this.transactionErrorMessage = '';
        // Вызываем метод для получения ВСЕХ транзакций
        return this.transactionService.getTransactions().pipe(
          // Фильтруем результат на клиенте
          map(allTransactions =>
            allTransactions.filter(tx => tx.accountId === this.accountId)
          ),
          catchError(err => { // Ошибка загрузки ВСЕХ транзакций
            this.transactionErrorMessage = `Failed to load transactions: ${err.message || 'Unknown error'}`;
            this.isLoadingTransactions = false;
            this.transactions = []; // Очищаем транзакции при ошибке
            // Возвращаем of([]) чтобы основной поток продолжился и детали аккаунта отобразились
            return of([]);
          })
        );
      })
      // *** КОНЕЦ ИЗМЕНЕНИЙ ***
    ).subscribe(filteredTransactions => {
      // Сюда придет результат фильтрации (или пустой массив при ошибке)
      this.transactions = filteredTransactions;
      this.isLoadingTransactions = false; // Загрузка транзакций (или обработка ошибки) завершена
    });
  }

  // Метод loadTransactions больше не нужен в таком виде
  // loadTransactions(accountId: number): Observable<TransactionGetDto[]> { ... }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(['/accounts']);
  }

  // Вспомогательный метод для проверки массива (оставляем)
  isAnArray(value: any): boolean {
    return Array.isArray(value);
  }

  formatTransactionDescriptions(descriptions: string[] | undefined | null): string {
    if (!this.isAnArray(descriptions) || descriptions!.length === 0) { // Используем isAnArray
      return 'N/A';
    }
    return descriptions!.join(', ');
  }

  // Добавь метод trackBy для таблицы транзакций (хорошая практика)
  trackTransactionById(index: number, item: TransactionGetDto): number {
    return item.id;
  }
}
