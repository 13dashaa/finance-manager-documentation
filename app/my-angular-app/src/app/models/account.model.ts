// Define the structure for account data received from the backend
export interface AccountGetDto {
  id: number;
  name: string;
  clientId: number;
  balance: number;
  createdAt: string; // Dates are typically strings in JSON
  updatedAt: string;
  clientUsername: string; // Added based on your DTO
  transactionDescriptions: string[]; // Java Set maps to Array
}

// Define the structure for creating a new account
export interface AccountCreateDto {
  name: string;
  balance: number;
  clientId: number;
}

// Define the structure for updating an existing account
export interface AccountUpdateDto {
  name: string;
  balance: number;
  // Note: clientId is not part of the update DTO based on your backend
}

// Optional: Model for Bulk Create if you implement that feature
export interface BulkCreateDto<T> {
  items: T[];
}
