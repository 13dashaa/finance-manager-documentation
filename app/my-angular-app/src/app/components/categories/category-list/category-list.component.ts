// src/app/components/categories/category-list/category-list.component.ts
import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {EMPTY, Observable, Subject} from 'rxjs';
import {catchError, switchMap, takeUntil, tap} from 'rxjs/operators';
import {CategoryService} from '../../../services/category.service'; // Адаптируйте путь
import {CategoryCreateDto, CategoryGetDto} from '../../../models/category.model'; // Адаптируйте путь

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
    // RouterModule не нужен, если нет ссылок на детали
  ],
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css'] // Используем свои или общие стили
})
export class CategoryListComponent implements OnInit, OnDestroy {

  categories: CategoryGetDto[] = [];
  isLoading = false;
  errorMessage = '';
  categoryForm: FormGroup;
  showAddEditForm = false;
  isEditMode = false;
  currentCategoryId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly categoryService: CategoryService,
    private readonly fb: FormBuilder
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.categoryService.getCategories()
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.isLoading = false),
        catchError(err => {
          console.error('Error loading categories:', err);
          this.errorMessage = err.message || 'Failed to load categories.';
          this.isLoading = false;
          return EMPTY;
        })
      )
      .subscribe(data => {
        this.categories = data;
      });
  }

  deleteCategory(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete category "${name}" (ID: ${id})?`)) {
      this.isLoading = true;
      this.errorMessage = '';
      this.categoryService.deleteCategory(id)
        .pipe(
          takeUntil(this.destroy$),
          switchMap(() => this.categoryService.getCategories()),
          tap(updatedCategories => {
            this.categories = updatedCategories;
            this.isLoading = false;
          }),
          catchError(err => {
            console.error('Error deleting category:', err);
            this.errorMessage = err.message || `Failed to delete category ${id}.`;
            this.isLoading = false;
            return EMPTY;
          })
        )
        .subscribe();
    }
  }

  openAddForm(): void {
    this.isEditMode = false;
    this.currentCategoryId = null;
    this.categoryForm.reset();
    this.showAddEditForm = true;
    this.errorMessage = '';
  }

  openEditForm(category: CategoryGetDto): void {
    this.isEditMode = true;
    this.currentCategoryId = category.id;
    this.categoryForm.patchValue({name: category.name}); // Заполняем форму
    this.showAddEditForm = true;
    this.errorMessage = '';
  }

  cancelForm(): void {
    this.showAddEditForm = false;
    this.isEditMode = false;
    this.currentCategoryId = null;
    this.categoryForm.reset();
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.errorMessage = 'Please correct the errors in the form.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    let operation$: Observable<CategoryGetDto>;

    const payload: CategoryCreateDto = {
      name: this.categoryForm.value.name.trim() // Убираем лишние пробелы
    };

    if (this.isEditMode && this.currentCategoryId !== null) {
      operation$ = this.categoryService.updateCategory(this.currentCategoryId, payload);
    } else {
      operation$ = this.categoryService.createCategory(payload);
    }

    operation$.pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.categoryService.getCategories()),
      tap(updatedCategories => {
        this.categories = updatedCategories;
        this.isLoading = false;
        this.cancelForm();
      }),
      catchError(err => {
        console.error('Error saving category:', err);
        this.errorMessage = err.message || `Failed to ${this.isEditMode ? 'update' : 'create'} category.`;
        this.isLoading = false;
        return EMPTY;
      })
    ).subscribe();
  }

  trackById(index: number, item: CategoryGetDto): number {
    return item.id;
  }
}
