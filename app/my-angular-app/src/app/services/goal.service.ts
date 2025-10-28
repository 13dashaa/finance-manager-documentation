import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {GoalCreateDto, GoalGetDto} from '../models/goal.model';
import {TransactionCreateDto} from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class GoalService {

  private readonly apiUrl = '/api/goals';

  constructor(private readonly http: HttpClient) {
  }

  getAllGoals(): Observable<GoalGetDto[]> {
    return this.http.get<GoalGetDto[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  addFundsToGoal(goalId: number, transactionData: TransactionCreateDto): Observable<GoalGetDto> {
    const url = `${this.apiUrl}/${goalId}/add-funds`;
    return this.http.post<GoalGetDto>(url, transactionData)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          return throwError(() => this.handleError(error));
        })
      );
  }
  getGoalsByClientId(clientId: number): Observable<GoalGetDto[]> {
    const params = new HttpParams().set('clientId', clientId.toString());
    return this.http.get<GoalGetDto[]>(`${this.apiUrl}/filter`, {params})
      .pipe(catchError(this.handleError));
  }

  createGoal(goal: GoalCreateDto): Observable<GoalGetDto> {
    return this.http.post<GoalGetDto>(this.apiUrl, goal)
      .pipe(catchError(this.handleError));
  }

  updateGoal(id: number, goalDetails: GoalCreateDto): Observable<GoalGetDto> {
    return this.http.put<GoalGetDto>(`${this.apiUrl}/${id}`, goalDetails)
      .pipe(catchError(this.handleError));
  }

  deleteGoal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private determineErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Could not connect to the server. Please check your network connection.';
    }
    if (typeof error.error === 'string') {
      return error.error;
    }
    if (error.error?.message) {
      return `Error ${error.status}: ${error.error.message}`;
    }
    return `Server returned code ${error.status}, error message: ${error.message}`;
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = this.determineErrorMessage(error);
    }
    if (error.status === 400 && error.error && typeof error.error === 'object') {
      const errors = error.error.errors;
      if (Array.isArray(errors)) {
        const validationErrors = errors.map((e: any) => `${e.field}: ${e.defaultMessage}`).join('\n');
        errorMessage += `\nValidation Errors:\n${validationErrors}`;
      } else if (typeof error.error.message === 'string') {
        errorMessage = error.error.message;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
