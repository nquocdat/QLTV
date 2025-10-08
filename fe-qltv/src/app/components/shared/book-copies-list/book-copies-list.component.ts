import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookCopyService, BookCopyDTO } from '../../../services/book-copy.service';

@Component({
  selector: 'app-book-copies-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './book-copies-list.component.html',
  styleUrls: ['./book-copies-list.component.css'],
})
export class BookCopiesListComponent implements OnInit {
  @Input() bookId!: number;
  @Input() showActions: boolean = false; // Admin/Librarian mode

  copies: BookCopyDTO[] = [];
  isLoading: boolean = false;
  error: string = '';

  // Stats
  totalCopies: number = 0;
  availableCount: number = 0;
  borrowedCount: number = 0;
  reservedCount: number = 0;

  constructor(private bookCopyService: BookCopyService) {}

  ngOnInit(): void {
    if (this.bookId) {
      this.loadCopies();
    }
  }

  loadCopies(): void {
    this.isLoading = true;
    this.error = '';

    this.bookCopyService.getCopiesByBookId(this.bookId).subscribe({
      next: (copies) => {
        this.copies = copies;
        this.calculateStats();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading copies:', err);
        this.error = 'Không thể tải danh sách bản sao. Vui lòng thử lại.';
        this.isLoading = false;
      },
    });
  }

  calculateStats(): void {
    this.totalCopies = this.copies.length;
    this.availableCount = this.copies.filter((c) => c.status === 'AVAILABLE').length;
    this.borrowedCount = this.copies.filter((c) => c.status === 'BORROWED').length;
    this.reservedCount = this.copies.filter((c) => c.status === 'RESERVED').length;
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

  onEditCopy(copy: BookCopyDTO): void {
    // TODO: Implement edit modal
    console.log('Edit copy:', copy);
  }

  onDeleteCopy(copy: BookCopyDTO): void {
    if (confirm(`Bạn có chắc muốn xóa bản sao #${copy.copyNumber}?`)) {
      this.bookCopyService.deleteCopy(copy.id).subscribe({
        next: () => {
          alert('✅ Đã xóa bản sao thành công!');
          this.loadCopies();
        },
        error: (err) => {
          console.error('Error deleting copy:', err);
          alert('❌ Lỗi khi xóa bản sao: ' + (err.error?.error || 'Unknown error'));
        },
      });
    }
  }
}
