// src/app/utils/common.utils.ts  (или src/app/common.utils.ts)

export class CommonUtils {

  /**
   * A common trackBy function for *ngFor loops iterating over
   * arrays of objects that have an 'id' property.
   * Helps Angular optimize rendering by identifying items.
   *
   * @param index - The index of the item in the array.
   * @param item - The item object itself (expected to have an 'id').
   * @returns The 'id' of the item, or the index if 'id' is missing.
   */
  public static trackById(index: number, item: any): any {
    // Use item.id if it exists, otherwise fall back to the index.
    // Using ?? null ensures that 0 is a valid ID.
    return item?.id ?? index;
    // Или, если уверены, что id всегда будет number:
    // return item && typeof item.id === 'number' ? item.id : index;
  }

  // Сюда можно добавлять другие общие утилитные статические методы
  // Например:
  // public static formatDate(...) { ... }
  // public static generateUniqueId() { ... }

}
