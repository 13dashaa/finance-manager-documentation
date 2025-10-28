// src/app/models/client.model.ts

export interface ClientCreateDto {
  username: string;
  email: string;
  password?: string; // Пароль обязателен только при создании
}

export interface ClientUpdateDto {
  username: string;
  // email и password не обновляются через этот DTO на бэкенде
}

export interface ClientGetDto {
  id: number;
  username: string;
  email: string;
  // Добавляем остальные поля, если они понадобятся в UI,
  // но для базовой таблицы они не нужны
  budgetCategoryNames?: Set<string>;
  // budgetIds?: Set<number>;
  // accountNames?: Set<string>;
  // accountIds?: Set<number>;
  goalNames?: Set<string>;
  goalIds?: Set<number>;
}
