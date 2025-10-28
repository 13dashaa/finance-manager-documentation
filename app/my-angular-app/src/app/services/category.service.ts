// src/app/services/category.service.ts
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {CategoryCreateDto, CategoryGetDto} from '../models/category.model'; // Адаптируйте путь

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly apiUrl = '/api/categories'; // Пример

  constructor(private readonly http: HttpClient) {
  }

  getCategories(): Observable<CategoryGetDto[]> {
    return this.http.get<CategoryGetDto[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getCategoryById(id: number): Observable<CategoryGetDto> {
    return this.http.get<CategoryGetDto>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createCategory(category: CategoryCreateDto): Observable<CategoryGetDto> {
    return this.http.post<CategoryGetDto>(this.apiUrl, category)
      .pipe(catchError(this.handleError));
  }

  updateCategory(id: number, categoryDetails: CategoryCreateDto): Observable<CategoryGetDto> {
    return this.http.put<CategoryGetDto>(`${this.apiUrl}/${id}`, categoryDetails)
      .pipe(catchError(this.handleError));
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {

      const serverError = error.error;
      errorMessage = `Server returned code ${error.status}, error message is: ${serverError?.message || error.message}`;
      if (error.status === 404) {
        errorMessage = 'Resource not found (404).';
      } else if (error.status === 400) {
        errorMessage = `Invalid input (400). ${serverError?.details || serverError?.message || ''}`;
      }
    }
    console.error(error);
    return throwError(() => new Error(errorMessage));
  }
}
