import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { PageRequest, PageResponse } from '../../models/pagination.model';
import { ActivatedRoute, Router } from '@angular/router';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.css'],
})
export class BookListComponent implements OnInit {
  books: Book[] = [];
  categories: any[] = [];
  quickFilters: any[] = [];
  selectedCategory = '';
  selectedStatus = '';
  activeQuickFilter = '';
  filteredBooks: Book[] = [];
  paginatedBooks: Book[] = [];
  totalBooks = 0;
  totalPages = 1;
  currentPage = 1;
  itemsPerPage = 10;
  searchQuery = '';
  Math = Math;
  isLoading = false;

  constructor(
    private bookService: BookService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['search'] || '';
      this.currentPage = params['page'] ? +params['page'] : 1;
      this.fetchBooks();
    });
    // Dummy data for categories and quickFilters
    this.categories = [
      { id: 1, name: 'Văn học' },
      { id: 2, name: 'Khoa học' },
      { id: 3, name: 'Lịch sử' },
    ];
    this.quickFilters = [
      { label: 'Tất cả', value: '' },
      { label: 'Sách mới', value: 'new' },
      { label: 'Có sẵn', value: 'available' },
      { label: 'Phổ biến', value: 'popular' },
    ];
  }

  fetchBooks(): void {
    this.isLoading = true;
    const pageRequest: PageRequest = {
      page: this.currentPage - 1,
      size: this.itemsPerPage,
      sortBy: 'title',
      sortDir: 'asc',
    };
    // Luôn lấy sách AVAILABLE để cho phép mượn tất cả sách có sẵn
    this.bookService.getBooksWithPagination({ ...pageRequest, status: 'AVAILABLE' }).subscribe({
      next: (res: PageResponse<Book>) => {
        this.books = res.content;
        this.filteredBooks = res.content;
        this.paginatedBooks = res.content;
        this.totalBooks = res.totalElements;
        this.totalPages = res.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.books = [];
        this.filteredBooks = [];
        this.paginatedBooks = [];
        this.totalBooks = 0;
        this.totalPages = 1;
        this.isLoading = false;
      },
    });
  }

  onSearch(): void {
    this.currentPage = 1;
    this.router.navigate([], {
      queryParams: { search: this.searchQuery, page: this.currentPage },
      queryParamsHandling: 'merge',
    });
    this.fetchBooks();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.router.navigate([], {
      queryParams: { page: this.currentPage },
      queryParamsHandling: 'merge',
    });
    this.fetchBooks();
  }

  onCategoryChange() {}
  onStatusChange() {}
  applyQuickFilter(value: string) {
    this.activeQuickFilter = value;
  }
  onItemsPerPageChange() {}
  getStatusBadgeClass(status: string) {
    return '';
  }
  getStatusText(status: string) {
    return status;
  }
  viewBookDetail(book: Book) {}
  borrowBook(book: Book) {}
  clearFilters() {}
  getPageNumbers() {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
}
