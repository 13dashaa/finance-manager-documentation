// src/app/models/budget.model.ts

export interface BudgetGetDto {
  id: number;
  clientIds: Array<number>;
  clientUsernames: Set<string>;
  categoryId: number;
  categoryName: string;
  limitation: number;
  availableSum: number;
  period: number; // Убедитесь, что тип соответствует (например, number of days)
  createdAt: string; // Используем строку для ISO даты
  updatedAt: string; // Используем строку для ISO даты
}

// Можно добавить и другие DTO (Create/Update), если планируется их использовать
export interface BudgetCreateDto {
  period: number;
  limitation: number;
  categoryId: number;
  clientIds: number[]; // Используем массив для простоты в формах
}

export interface BudgetUpdateDto {
  period?: number;      // Поля опциональны при обновлении
  limitation?: number;
  clientIds?: number[]; // Может обновляться список владельцев
  // categoryId обычно не меняется у существующего бюджета
}
