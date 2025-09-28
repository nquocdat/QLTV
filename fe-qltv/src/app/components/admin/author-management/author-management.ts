import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { AuthorService } from '../../../services/author.service';
import { Author } from '../../../models/author.model';
import { AuthorCreateRequest } from '../../../models/author.model';

@Component({
  selector: 'app-author-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './author-management.html',
  styleUrl: './author-management.css',
})
export class AuthorManagement implements OnInit {
  authors: Author[] = [];
  authorForm!: FormGroup;

  // Modal state
  showAuthorModal = false;
  modalMode: 'add' | 'edit' | 'view' = 'add';
  selectedAuthor: Author | null = null;

  // Loading state
  isLoading = false;

  // Filters
  searchQuery = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Math for template
  Math = Math;

  constructor(private fb: FormBuilder, private authorService: AuthorService) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadAuthors();
  }

  initializeForm(): void {
    this.authorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      biography: [''],
      birthDate: [''],
      nationality: [''],
    });
  }

  loadAuthors(): void {
    this.authorService
      .getAllAuthors(this.currentPage - 1, this.pageSize, this.searchQuery)
      .subscribe({
        next: (response) => {
          this.authors = response.content || [];
          this.totalItems = response.totalElements || 0;
          this.totalPages = response.totalPages || 0;
        },
        error: (error) => {
          console.error('Error loading authors:', error);
          this.authors = [];
          this.totalItems = 0;
          this.totalPages = 0;
        },
      });
  }

  onSearch(): void {
    this.currentPage = 1;
    this.loadAuthors();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadAuthors();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadAuthors();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadAuthors();
  }

  openAddAuthorModal(): void {
    this.modalMode = 'add';
    this.selectedAuthor = null;
    this.initializeForm();
    this.showAuthorModal = true;
  }

  editAuthor(author: Author): void {
    this.modalMode = 'edit';
    this.selectedAuthor = author;
    this.authorForm.patchValue({
      name: author.name,
      biography: author.biography,
      birthDate: author.birthDate,
      nationality: author.nationality,
    });
    this.showAuthorModal = true;
  }

  viewAuthor(author: Author): void {
    this.modalMode = 'view';
    this.selectedAuthor = author;
    this.showAuthorModal = true;
  }

  closeModal(): void {
    this.showAuthorModal = false;
    this.selectedAuthor = null;
    this.initializeForm();
  }

  onSubmit(): void {
    if (this.authorForm.valid) {
      this.isLoading = true;
      const formData = this.authorForm.value;

      if (this.modalMode === 'add') {
        const authorRequest: AuthorCreateRequest = {
          name: formData.name,
          biography: formData.biography,
          birthDate: formData.birthDate,
          nationality: formData.nationality,
        };

        this.authorService.createAuthor(authorRequest).subscribe({
          next: (newAuthor: Author) => {
            console.log('Thêm tác giả mới thành công:', newAuthor);
            this.loadAuthors();
            this.isLoading = false;
            this.closeModal();
          },
          error: (error) => {
            console.error('Lỗi khi thêm tác giả:', error);
            this.isLoading = false;
          },
        });
      } else if (this.modalMode === 'edit' && this.selectedAuthor?.id) {
        const authorRequest: AuthorCreateRequest = {
          name: formData.name,
          biography: formData.biography,
          birthDate: formData.birthDate,
          nationality: formData.nationality,
        };

        this.authorService.updateAuthor(this.selectedAuthor.id, authorRequest).subscribe({
          next: (updatedAuthor: Author) => {
            console.log('Cập nhật tác giả thành công:', updatedAuthor);
            this.loadAuthors();
            this.isLoading = false;
            this.closeModal();
          },
          error: (error) => {
            console.error('Lỗi khi cập nhật tác giả:', error);
            this.isLoading = false;
          },
        });
      }
    }
  }

  deleteAuthor(author: Author): void {
    if (confirm(`Bạn có chắc muốn xóa tác giả "${author.name}"?`)) {
      if (author.id) {
        this.authorService.deleteAuthor(author.id).subscribe({
          next: () => {
            console.log('Đã xóa tác giả:', author.name);
            this.loadAuthors();
          },
          error: (error) => {
            console.error('Lỗi khi xóa tác giả:', error);
          },
        });
      }
    }
  }

  getAuthorInitials(name: string): string {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getVisiblePages(): number[] {
    const pages: number[] = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }
}
