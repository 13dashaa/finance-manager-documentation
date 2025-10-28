import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AccountCreateDto, AccountGetDto, AccountUpdateDto} from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly apiUrl = '/api/accounts';

  constructor(private readonly http: HttpClient) {
  }

  getAllAccounts(): Observable<AccountGetDto[]> {
    return this.http.get<AccountGetDto[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getAccountById(id: number): Observable<AccountGetDto> {
    return this.http.get<AccountGetDto>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  getAccountsByClientId(clientId: number): Observable<AccountGetDto[]> {
    const params = new HttpParams().set('clientId', clientId.toString());
    return this.http.get<AccountGetDto[]>(`${this.apiUrl}/filter`, {params})
      .pipe(catchError(this.handleError));
  }

  createAccount(account: AccountCreateDto): Observable<AccountGetDto> {
    return this.http.post<AccountGetDto>(this.apiUrl, account)
      .pipe(catchError(this.handleError));
  }

  updateAccount(id: number, accountDetails: AccountUpdateDto): Observable<AccountGetDto> {
    return this.http.put<AccountGetDto>(`${this.apiUrl}/${id}`, accountDetails)
      .pipe(catchError(this.handleError));
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    const errorMessage = this.determineErrorMessage(error);
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  private determineErrorMessage(error: HttpErrorResponse): string {
    if (error.error instanceof ErrorEvent) {
      return `Error: ${error.error.message}`;
    }

    console.error(
      `Backend returned code ${error.status}, ` +
      `body was: ${JSON.stringify(error.error)}`);

    if (error.status === 400) {
      return this.handle400Error(error);
    }

    if (error.status === 404) {
      return `Resource not found (Status ${error.status}): ${error.error?.message || error.statusText}`;
    }

    return `Server error (Status ${error.status}): ${error.error?.message || error.statusText}`;
  }

  private handle400Error(error: HttpErrorResponse): string {
    if (!error.error) {
      return `Invalid request (Status ${error.status}): ${error.statusText}`;
    }

    if (typeof error.error === 'object') {
      return this.handle400ObjectError(error);
    }

    return `Invalid request (Status ${error.status}): ${error.error}`;
  }

  private handle400ObjectError(error: HttpErrorResponse): string {
    const fieldErrors = error.error.errors;

    if (Array.isArray(fieldErrors)) {
      return fieldErrors.map(e => `${e.field}: ${e.defaultMessage}`).join('\n');
    }

    if (error.error.message) {
      return `Error: ${error.error.message}`;
    }

    return `Invalid input (Status ${error.status}). Check console for details.`;
  }
}
