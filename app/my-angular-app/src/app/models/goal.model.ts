// src/app/models/goal.model.ts

// Corresponds to GoalCreateDto
export interface GoalCreateDto {
  name: string;
  targetAmount: number; // Используем number для простоты в Angular, конвертация в BigDecimal на бэкенде
  // currentAmount не включаем, т.к. его нет в конструкторе и не обновляется явно
  startDate?: string | null; // Используем строку 'YYYY-MM-DD' для <input type="date">
  endDate: string;       // Используем строку 'YYYY-MM-DD'
  clientId: number;
}

// Corresponds to GoalGetDto
export interface GoalGetDto {
  id: number;
  name: string;
  targetAmount: number;
  currentAmount: number;
  startDate?: string | null; // Может быть null/undefined если не задана
  endDate: string;
  clientId: number;
  clientUsername: string;
}
