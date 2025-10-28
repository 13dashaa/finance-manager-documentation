import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TransactionCreateDto, TransactionGetDto} from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private readonly apiUrl = '/api/transactions';

  constructor(private readonly http: HttpClient) {
  }

  createTransaction(payload: TransactionCreateDto): Observable<TransactionGetDto> {
    return this.http.post<TransactionGetDto>(this.apiUrl, payload);
  }

  getTransactions(filters?: { accountId?: number | null; categoryId?: number | null }): Observable<TransactionGetDto[]> {
    let params = new HttpParams();

    if (filters) {
      if (filters.accountId != null) {
        params = params.set('accountId', filters.accountId.toString());
      }
      if (filters.categoryId != null) {
        params = params.set('categoryId', filters.categoryId.toString());
      }
    }

    return this.http.get<TransactionGetDto[]>(this.apiUrl, { params });
  }

  getTransactionById(id: number): Observable<TransactionGetDto> {
    return this.http.get<TransactionGetDto>(`${this.apiUrl}/${id}`);
  }

  getTransactionsByClientAndCategory(clientId: number, categoryId: number): Observable<TransactionGetDto[]> {
    const params = new HttpParams()
      .set('clientId', clientId.toString())
      .set('categoryId', categoryId.toString());
    return this.http.get<TransactionGetDto[]>(`${this.apiUrl}/filter`, {params});
  }

  updateTransaction(id: number, payload: TransactionCreateDto): Observable<TransactionGetDto> {
    return this.http.put<TransactionGetDto>(`${this.apiUrl}/${id}`, payload);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
