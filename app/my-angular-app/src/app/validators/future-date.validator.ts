// src/app/validators/future-date.validator.ts
import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export function futureDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null; // Не валидируем пустое значение (required позаботится об этом)
    }

    try {
      const inputDate = new Date(control.value);
      const today = new Date();

      // Обнуляем время для корректного сравнения только дат
      inputDate.setHours(0, 0, 0, 0);
      today.setHours(0, 0, 0, 0);

      // Дата должна быть строго больше сегодняшней
      return inputDate > today ? null : {futureDate: {value: control.value}};

    } catch (e) {
      // Если не удалось распарсить дату (маловероятно с type="date", но на всякий случай)
      return {futureDate: {value: control.value, error: 'Invalid date format'}};
    }
  };
}
