import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {BudgetCreateDto, BudgetGetDto, BudgetUpdateDto} from '../models/budget.model';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private readonly apiUrl = '/api/budgets';

  constructor(private readonly http: HttpClient) {
  }

  getAllBudgets(): Observable<BudgetGetDto[]> {
    return this.http.get<BudgetGetDto[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getBudgetById(id: number): Observable<BudgetGetDto> {
    return this.http.get<BudgetGetDto>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createBudget(budgetData: BudgetCreateDto): Observable<BudgetGetDto> {
    return this.http.post<BudgetGetDto>(this.apiUrl, budgetData)
      .pipe(catchError(this.handleError));
  }

  updateBudget(id: number, budgetData: BudgetUpdateDto): Observable<BudgetGetDto> {
    return this.http.put<BudgetGetDto>(`${this.apiUrl}/${id}`, budgetData)
      .pipe(catchError(this.handleError));
  }

  deleteBudget(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error (BudgetService):', error);

    const userFriendlyMessage = error.error instanceof ErrorEvent
      ? this.getNetworkErrorMessage(error.error)
      : this.getServerErrorMessage(error);

    return throwError(() => new Error(userFriendlyMessage));
  }

  private getNetworkErrorMessage(error: ErrorEvent): string {
    return `Network error: ${error.message}`;
  }

  private getServerErrorMessage(error: HttpErrorResponse): string {
    const { status, error: backendError } = error;
    const detailedMessage = this.extractDetailedMessage(backendError);

    if ((status === 400 || status === 409) && detailedMessage) {
      return this.getValidationErrorMessage(status, detailedMessage, backendError);
    }

    if (status === 404) {
      return 'Budget or related resource not found (Error 404).';
    }

    if (status >= 500) {
      return `Server error during budget operation (Error ${status}).`;
    }

    return `Budget operation error (Error ${status}).`;
  }

  private extractDetailedMessage(backendError: any): string {
    if (typeof backendError === 'string') return backendError;
    if (backendError?.message) return backendError.message;
    if (backendError?.error) return backendError.error;
    return '';
  }

  private getValidationErrorMessage(status: number, detailedMessage: string, backendError: any): string {
    if (!backendError?.errors) {
      return `Invalid data submitted for budget (Error ${status}). Check fields like limits, periods, or associated clients/categories. Details: ${detailedMessage}`;
    }

    try {
      const validationErrors = Object.values(backendError.errors).flat().join('. ');
      return `Budget validation failed: ${validationErrors}`;
    } catch {
      return `Invalid data submitted for budget (Error ${status}). Details: ${detailedMessage}`;
    }
  }
}
