import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import {
  CategoryService,
  Category,
  CategoryCreateRequest,
} from '../../../services/category.service';

interface CategoryUI extends Category {
  color?: string;
  isActive?: boolean;
  bookCount?: number;
}

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './category-management.html',
  styleUrl: './category-management.css',
})
export class CategoryManagement implements OnInit {
  categories: CategoryUI[] = [];
  filteredCategories: CategoryUI[] = [];
  categoryForm!: FormGroup;

  // Modal state
  showCategoryModal = false;
  modalMode: 'add' | 'edit' | 'view' = 'add';
  selectedCategory: CategoryUI | null = null;

  // Loading state
  isLoading = false;

  // Filters
  searchQuery = '';
  selectedStatus = '';

  // Stats
  totalCategories = 0;
  activeCategories = 0;
  totalBooks = 0;

  constructor(private fb: FormBuilder, private categoryService: CategoryService) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  initializeForm(): void {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      color: ['#6366f1', Validators.required],
      isActive: [true],
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        // Transform to CategoryUI with default values for UI properties
        this.categories = categories.map((cat) => ({
          ...cat,
          color: '#6366f1', // Default color
          isActive: true, // Default active
          bookCount: 0, // Default book count
        }));
        this.calculateStats();
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.categories = [];
        this.calculateStats();
      },
    });
  }

  calculateStats(): void {
    this.totalCategories = this.categories.length;
    this.activeCategories = this.categories.filter((cat) => cat.isActive ?? true).length;
    this.totalBooks = this.categories.reduce((sum, cat) => sum + (cat.bookCount ?? 0), 0);
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = [...this.categories];

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (category) =>
          category.name.toLowerCase().includes(query) ||
          (category.description && category.description.toLowerCase().includes(query))
      );
    }

    // Status filter
    if (this.selectedStatus !== '') {
      const isActive = this.selectedStatus === 'true';
      filtered = filtered.filter((category) => category.isActive === isActive);
    }

    this.filteredCategories = filtered;
  }

  openAddCategoryModal(): void {
    this.modalMode = 'add';
    this.selectedCategory = null;
    this.initializeForm();
    this.showCategoryModal = true;
  }

  editCategory(category: CategoryUI): void {
    this.modalMode = 'edit';
    this.selectedCategory = category;
    this.categoryForm.patchValue({
      name: category.name,
      description: category.description,
      color: category.color || '#6366f1',
      isActive: category.isActive ?? true,
    });
    this.showCategoryModal = true;
  }

  viewCategory(category: CategoryUI): void {
    this.modalMode = 'view';
    this.selectedCategory = category;
    this.showCategoryModal = true;
  }

  closeModal(): void {
    this.showCategoryModal = false;
    this.selectedCategory = null;
    this.initializeForm();
  }

  onSubmit(): void {
    if (this.categoryForm.valid) {
      this.isLoading = true;

      const formData = this.categoryForm.value;

      if (this.modalMode === 'add') {
        const categoryRequest: CategoryCreateRequest = {
          name: formData.name,
          description: formData.description,
        };

        this.categoryService.createCategory(categoryRequest).subscribe({
          next: (newCategory) => {
            const categoryUI: CategoryUI = {
              ...newCategory,
              color: formData.color,
              isActive: formData.isActive,
              bookCount: 0,
            };
            this.categories.push(categoryUI);
            console.log('Thêm thể loại mới:', categoryUI);
            this.calculateStats();
            this.applyFilters();
          },
          error: (error) => {
            console.error('Error creating category:', error);
          },
        });
      } else if (this.modalMode === 'edit' && this.selectedCategory) {
        const categoryIndex = this.categories.findIndex((c) => c.id === this.selectedCategory!.id);
        if (categoryIndex !== -1) {
          this.categories[categoryIndex] = {
            ...this.categories[categoryIndex],
            name: formData.name,
            description: formData.description,
            color: formData.color,
            isActive: formData.isActive,
          };
          console.log('Cập nhật thể loại:', this.categories[categoryIndex]);
        }
      }

      setTimeout(() => {
        this.isLoading = false;
        this.closeModal();
        this.calculateStats();
        this.applyFilters();
      }, 1000);
    }
  }

  toggleCategoryStatus(category: CategoryUI): void {
    const action = category.isActive ?? true ? 'vô hiệu hóa' : 'kích hoạt';
    if (confirm(`Bạn có chắc muốn ${action} thể loại "${category.name}"?`)) {
      const categoryIndex = this.categories.findIndex((c) => c.id === category.id);
      if (categoryIndex !== -1) {
        this.categories[categoryIndex].isActive = !(
          this.categories[categoryIndex].isActive ?? true
        );
        this.calculateStats();
        this.applyFilters();
        console.log(`Đã ${action} thể loại:`, category.name);
      }
    }
  }
}
