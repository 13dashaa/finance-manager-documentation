import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ClientCreateDto, ClientGetDto, ClientUpdateDto} from '../models/client.model';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private readonly apiUrl = '/api/clients';

  constructor(private readonly http: HttpClient) {
  }

  getClients(): Observable<ClientGetDto[]> {
    return this.http.get<ClientGetDto[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getClientById(id: number): Observable<ClientGetDto> {
    return this.http.get<ClientGetDto>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createClient(clientData: ClientCreateDto): Observable<ClientGetDto> {
    return this.http.post<ClientGetDto>(this.apiUrl, clientData)
      .pipe(catchError(this.handleError));
  }

  updateClient(id: number, clientData: ClientUpdateDto): Observable<ClientGetDto> {
    return this.http.put<ClientGetDto>(`${this.apiUrl}/${id}`, clientData)
      .pipe(catchError(this.handleError));
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && typeof error.error === 'string') {
        errorMessage += `\nDetails: ${error.error}`;
      }  else if (error.error?.message){
        errorMessage += `\nDetails: ${error.error.message}`;
      } else if (error.error?.errors) { // Handle Spring Validation errors
        errorMessage += `\nValidation Errors: ${JSON.stringify(error.error.errors)}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
