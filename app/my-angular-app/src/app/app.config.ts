// src/app/config.ts
import {ApplicationConfig, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withFetch} from '@angular/common/http'; // Импортируем provideHttpClient
import {routes} from './app.routes';
import {provideCharts, withDefaultRegisterables} from 'ng2-charts'; // <-- ИМПОРТ
import {provideClientHydration} from '@angular/platform-browser';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes), // Подключаем маршруты
    provideClientHydration(),
    provideCharts(withDefaultRegisterables()), // <-- ДОБАВЬТЕ ПРОВАЙДЕР

    provideHttpClient(withFetch()) // <--- УБЕДИТЕСЬ, ЧТО ЭТО ЕСТЬ. Позволяет использовать HttpClient во всем приложении
    // withFetch() - опционально, для использования Fetch API бэкендом HttpClient
  ]
};
