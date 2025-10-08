import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  BookCopyService,
  BookCopyDTO,
  BulkCreateRequest,
} from '../../../services/book-copy.service';
import { BookService } from '../../../services/book.service';
import { Book } from '../../../models/book.model';

@Component({
  selector: 'app-admin-copies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-copies.component.html',
  styleUrls: ['./admin-copies.component.css'],
})
export class AdminCopiesComponent implements OnInit {
  copies: BookCopyDTO[] = [];
  books: Book[] = [];
  isLoading = false;
  error = '';

  // Filter
  filterStatus: string = '';
  filterBookId: number | null = null;
  searchBarcode: string = '';

  // Create modal
  showCreateModal = false;
  showBulkCreateModal = false;
  selectedBookForCreate: number | null = null;

  // Bulk create form
  bulkCreateForm: BulkCreateRequest = {
    quantity: 1,
    location: 'Kho chính',
    price: undefined,
  };

  // Edit modal
  showEditModal = false;
  editingCopy: BookCopyDTO | null = null;
  editForm = {
    conditionStatus: '',
    status: '',
    location: '',
    price: 0,
    notes: '',
  };

  constructor(
    private bookCopyService: BookCopyService,
    private bookService: BookService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCopies();
    this.loadBooks();
  }

  loadCopies(): void {
    this.isLoading = true;
    this.error = '';

    this.bookCopyService.getAllCopies().subscribe({
      next: (copies) => {
        this.copies = copies;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading copies:', err);
        this.error = 'Không thể tải danh sách bản sao';
        this.isLoading = false;
      },
    });
  }

  loadBooks(): void {
    this.bookService.getAllBooks().subscribe({
      next: (response: any) => {
        this.books = response.content || response;
      },
      error: (err) => {
        console.error('Error loading books:', err);
      },
    });
  }

  applyFilters(): void {
    // Implement filtering logic based on filterStatus, filterBookId, searchBarcode
  }

  openBulkCreateModal(): void {
    this.showBulkCreateModal = true;
  }

  closeBulkCreateModal(): void {
    this.showBulkCreateModal = false;
    this.selectedBookForCreate = null;
    this.bulkCreateForm = {
      quantity: 1,
      location: 'Kho chính',
      price: undefined,
    };
  }

  submitBulkCreate(): void {
    if (!this.selectedBookForCreate) {
      alert('Vui lòng chọn sách');
      return;
    }

    this.bookCopyService
      .createMultipleCopies(this.selectedBookForCreate, this.bulkCreateForm)
      .subscribe({
        next: () => {
          alert(`✅ Đã tạo ${this.bulkCreateForm.quantity} bản sao thành công!`);
          this.closeBulkCreateModal();
          this.loadCopies();
        },
        error: (err) => {
          console.error('Error creating copies:', err);
          alert('❌ Lỗi khi tạo bản sao: ' + (err.error?.error || 'Unknown error'));
        },
      });
  }

  openEditModal(copy: BookCopyDTO): void {
    this.editingCopy = copy;
    this.editForm = {
      conditionStatus: copy.conditionStatus,
      status: copy.status,
      location: copy.location,
      price: copy.price || 0,
      notes: copy.notes || '',
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingCopy = null;
  }

  submitEdit(): void {
    if (!this.editingCopy) return;

    this.bookCopyService.updateCopy(this.editingCopy.id, this.editForm).subscribe({
      next: () => {
        alert('✅ Cập nhật bản sao thành công!');
        this.closeEditModal();
        this.loadCopies();
      },
      error: (err) => {
        console.error('Error updating copy:', err);
        alert('❌ Lỗi khi cập nhật: ' + (err.error?.error || 'Unknown error'));
      },
    });
  }

  deleteCopy(copy: BookCopyDTO): void {
    if (confirm(`Bạn có chắc muốn xóa bản sao #${copy.copyNumber} (${copy.barcode})?`)) {
      this.bookCopyService.deleteCopy(copy.id).subscribe({
        next: () => {
          alert('✅ Đã xóa bản sao!');
          this.loadCopies();
        },
        error: (err) => {
          console.error('Error deleting copy:', err);
          alert('❌ Lỗi: ' + (err.error?.error || 'Không thể xóa bản sao đang được mượn'));
        },
      });
    }
  }

  getStatusClass(status: string): string {
    return this.bookCopyService.getStatusClass(status);
  }

  getConditionClass(condition: string): string {
    return this.bookCopyService.getConditionClass(condition);
  }

  getStatusIcon(status: string): string {
    return this.bookCopyService.getStatusIcon(status);
  }

  viewMaintenance(): void {
    this.bookCopyService.getCopiesNeedingMaintenance().subscribe({
      next: (copies) => {
        this.copies = copies;
      },
      error: (err) => {
        console.error('Error:', err);
      },
    });
  }
}
