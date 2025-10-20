import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormGroup,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { AuthorService } from '../../../services/author.service';
import { CategoryService } from '../../../services/category.service';
import { PublisherService } from '../../../services/publisher.service';
import { Book, BookCreateRequest } from '../../../models/book.model';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

interface Category {
  id?: number;
  name: string;
  description?: string;
}

interface Author {
  id?: number;
  name: string;
  biography?: string;
}

interface Publisher {
  id?: number;
  name: string;
  address?: string;
}

@Component({
  selector: 'app-book-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    MatDialogModule,
  ],
  templateUrl: './book-management.html',
  styleUrls: ['./book-management.css'],
})
export class BookManagement implements OnInit, OnDestroy {
  // Danh sách trạng thái Enum chuẩn
  statusOptions = [
    'AVAILABLE',
    'BORROWED',
    'MAINTENANCE',
    'DAMAGED',
    'LOST',
    'RESERVED',
    'ON_LOAN',
  ];
  books: Book[] = [];
  filteredBooks: Book[] = [];
  categories: Category[] = [];
  authors: Author[] = [];
  publishers: Publisher[] = [];

  // Statistics
  totalBooks = 0;
  availableBooks = 0;
  borrowedBooks = 0;
  overdueBooks = 0;

  // Search and filter
  searchTerm = '';
  selectedCategory = '';
  selectedStatus = '';

  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  // Modal states
  showModal = false;
  showDetailModal = false;
  showDeleteModal = false;
  isEditing = false;

  // Current book for operations
  currentBook: Book = this.createEmptyBook();
  selectedBook: Book | null = null;
  bookToDelete: Book | null = null;

  // Math for template
  Math = Math;

  // Autocomplete suggestions
  authorSuggestions: { name: string }[] = [];
  publisherSuggestions: { name: string }[] = [];
  categorySuggestions: { name: string }[] = [];

  // Search subjects for autocomplete
  authorSearchSubject = new Subject<string>();
  publisherSearchSubject = new Subject<string>();
  categorySearchSubject = new Subject<string>();

  // Selected values for dropdowns
  selectedAuthor: string = '';
  selectedPublisher: string = '';
  selectedCategoryForBook: string = '';

  // Filtered values for autocomplete
  filteredAuthors: Author[] = [];
  filteredCategories: Category[] = [];
  filteredPublishers: Publisher[] = [];

  // Form group for book form
  bookForm: FormGroup;

  // Hover state for suggestion dropdowns
  hoveredAuthor: any = null;
  hoveredCategory: any = null;
  hoveredPublisher: any = null;

  // File upload properties
  selectedFile: File | null = null;
  imagePreview: string | null = null;

  constructor(
    private router: Router,
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService,
    private publisherService: PublisherService,
    private formBuilder: FormBuilder
  ) {
    // Initialize form group
    this.bookForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      authorInput: ['', [Validators.required]],
      categoryInput: ['', [Validators.required]],
      publisherInput: ['', [Validators.required]],
      isbn: [''],
      totalCopies: [1, [Validators.required, Validators.min(1)]],
      description: [''],
      publishYear: [new Date().getFullYear()],
      pages: [0],
      genre: [''],
      status: ['AVAILABLE'],
      coverImage: [''],
      categoryId: [0],
      publisher: [''],
      author: [''],
    });
  }

  ngOnInit() {
    this.loadCategories();
    this.loadAuthors();
    this.loadPublishers();
    this.loadBooks();
    this.updateStatistics();
    this.setupAutocomplete();

    // Listen for add book trigger from admin layout
    window.addEventListener('triggerAddBook', () => {
      this.openAddModal();
    });
  }

  ngOnDestroy(): void {
    this.authorSearchSubject.complete();
    this.categorySearchSubject.complete();
    this.publisherSearchSubject.complete();
  }

  loadCategories() {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.categories = [];
      },
    });
  }

  loadAuthors() {
    this.authorService.getAllAuthors().subscribe({
      next: (response) => {
        this.authors = response.content || [];
      },
      error: (error: any) => {
        console.error('Error loading authors:', error);
        this.authors = [];
      },
    });
  }

  loadPublishers() {
    this.publisherService.getAllPublishers().subscribe({
      next: (response) => {
        this.publishers = response.content || [];
      },
      error: (error: any) => {
        console.error('Error loading publishers:', error);
        this.publishers = [];
      },
    });
  }

  setupAutocomplete() {
    // Setup author search autocomplete
    this.authorSearchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => this.authorService.searchAuthors(term))
      )
      .subscribe({
        next: (authors) => {
          this.authorSuggestions = authors;
        },
        error: (error: any) => {
          console.error('Error getting author suggestions:', error);
        },
      });

    // Setup publisher search autocomplete
    this.publisherSearchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => this.publisherService.searchPublishers(term))
      )
      .subscribe({
        next: (publishers) => {
          this.publisherSuggestions = publishers;
        },
        error: (error: any) => {
          console.error('Error getting publisher suggestions:', error);
        },
      });

    // Setup category search autocomplete
    this.categorySearchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => this.categoryService.searchCategories(term))
      )
      .subscribe({
        next: (categories) => {
          this.categorySuggestions = categories;
        },
        error: (error: any) => {
          console.error('Error getting category suggestions:', error);
        },
      });
  }

  loadBooks() {
    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        this.books = books;
        this.filteredBooks = [...this.books];
        this.updatePagination();
        this.updateStatistics();
      },
      error: (error) => {
        console.error('Error loading books:', error);
        this.books = [];
        this.filteredBooks = [];
        this.updatePagination();
        this.updateStatistics();
      },
    });
  }

  updateStatistics() {
    this.totalBooks = this.books.length;
    this.availableBooks = this.books.filter((book) => book.status === 'available').length;
    this.borrowedBooks = this.books.filter((book) => book.status === 'borrowed').length;
    this.overdueBooks = this.books.filter((book) => book.status === 'maintenance').length;
  }

  filterBooks() {
    this.filteredBooks = this.books.filter((book) => {
      const title = book.title ? book.title.toLowerCase() : '';
      const author = book.author ? book.author.toLowerCase() : '';
      const isbn = book.isbn || '';
      const searchTerm = this.searchTerm ? this.searchTerm.toLowerCase() : '';

      const matchesSearch =
        !searchTerm ||
        title.includes(searchTerm) ||
        author.includes(searchTerm) ||
        isbn.includes(searchTerm);

      const matchesCategory =
        !this.selectedCategory ||
        (book.categoryId && book.categoryId.toString() === this.selectedCategory);

      // Convert status to uppercase for comparison (backend returns AVAILABLE, BORROWED)
      const matchesStatus =
        !this.selectedStatus ||
        (book.status && book.status.toUpperCase() === this.selectedStatus.toUpperCase());

      return matchesSearch && matchesCategory && matchesStatus;
    });

    this.currentPage = 1;
    this.updatePagination();
  }

  // Alias for filterBooks to match template usage
  applyFilters() {
    this.filterBooks();
  }

  updatePagination() {
    this.totalPages = Math.ceil(this.filteredBooks.length / this.itemsPerPage);
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToPage(page: number) {
    this.currentPage = page;
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  getCategoryColor(categoryId: number | undefined): string {
    if (!categoryId) return 'bg-gray-100 text-gray-800';
    const category = this.categories.find((cat) => cat.id === categoryId);
    // Generate color based on category name hash
    if (category) {
      const colors = [
        'bg-blue-100 text-blue-800',
        'bg-green-100 text-green-800',
        'bg-yellow-100 text-yellow-800',
        'bg-red-100 text-red-800',
        'bg-purple-100 text-purple-800',
        'bg-pink-100 text-pink-800',
      ];
      const hash = category.name.split('').reduce((a, b) => {
        a = (a << 5) - a + b.charCodeAt(0);
        return a & a;
      }, 0);
      return colors[Math.abs(hash) % colors.length];
    }
    return 'bg-gray-100 text-gray-800';
  }

  getCategoryName(categoryId: number | undefined): string {
    if (!categoryId) return 'Chưa phân loại';
    const category = this.categories.find((cat) => cat.id === categoryId);
    return category ? category.name : 'Chưa phân loại';
  }

  getStatusText(status: string | undefined): string {
    if (!status) return 'Không xác định';
    switch (status.toUpperCase()) {
      case 'AVAILABLE':
        return 'Có sẵn';
      case 'BORROWED':
        return 'Đang mượn';
      case 'MAINTENANCE':
        return 'Bảo trì';
      case 'DAMAGED':
        return 'Hư hỏng';
      case 'LOST':
        return 'Mất';
      case 'RESERVED':
        return 'Đã đặt trước';
      case 'ON_LOAN':
        return 'Đang cho mượn';
      default:
        return 'Không xác định';
    }
  }

  getStatusColor(status: string | undefined): string {
    if (!status) return 'bg-gray-100 text-gray-800';
    switch (status) {
      case 'AVAILABLE':
      case 'available':
        return 'bg-green-100 text-green-800';
      case 'BORROWED':
      case 'borrowed':
        return 'bg-yellow-100 text-yellow-800';
      case 'MAINTENANCE':
      case 'maintenance':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusBadgeClass(status: string | undefined): string {
    return this.getStatusColor(status);
  }

  // Create empty book object
  createEmptyBook(): Book {
    return {
      title: '',
      author: '',
      isbn: '',
      categoryId: 0,
      publisher: '',
      publishYear: new Date().getFullYear(),
      publicationYear: new Date().getFullYear(),
      pages: 0,
      genre: '',
      status: 'AVAILABLE',
      description: '',
      coverImage: '',
      totalCopies: 1,
      availableCopies: 1,
      addedDate: new Date(),
    };
  }

  // Modal methods
  openAddModal(): void {
    this.isEditing = false;
    this.currentBook = this.createEmptyBook();
    this.bookForm.reset({
      ...this.currentBook,
      authorInput: '',
      categoryInput: '',
      publisherInput: '',
    });
    this.selectedFile = null;
    this.imagePreview = null;
    this.showModal = true;
  }

  openEditModal(book: Book): void {
    this.isEditing = true;
    this.currentBook = { ...book };
    this.bookForm.patchValue({
      title: book.title || '',
      authorInput: book.author || '',
      categoryInput: this.getCategoryName(book.categoryId),
      publisherInput: book.publisher || '',
      isbn: book.isbn || '',
      publishYear: book.publishYear || book.publicationYear || new Date().getFullYear(),
      pages: book.pages || 0,
      genre: book.genre || '',
      status: book.status || 'AVAILABLE',
      coverImage: book.coverImage || '',
      description: book.description || '',
      totalCopies: book.totalCopies || 1,
      availableCopies: book.availableCopies || book.totalCopies || 1,
      categoryId: book.categoryId || 0,
      publisher: book.publisher || '',
      author: book.author || '',
    });
    this.selectedFile = null;
    this.imagePreview = book.coverImage || null;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentBook = this.createEmptyBook();
    this.isEditing = false;
    this.selectedFile = null;
    this.imagePreview = null;
  }

  openDetailModal(book: Book): void {
    this.selectedBook = book;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedBook = null;
  }

  openDeleteModal(book: Book): void {
    this.bookToDelete = book;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.bookToDelete = null;
  }

  // CRUD Operations
  saveBook(): void {
    if (this.bookForm.valid) {
      const formValue = this.bookForm.value;

      // If file selected, upload first
      if (this.selectedFile) {
        this.bookService.uploadCoverImage(this.selectedFile).subscribe({
          next: (response) => {
            // Set uploaded image URL
            formValue.coverImage = 'http://localhost:8081' + response.imageUrl;
            this.processSaveBook(formValue);
          },
          error: (error) => {
            console.error('Error uploading image:', error);
            alert('Lỗi khi upload ảnh: ' + (error.error?.error || 'Unknown error'));
          },
        });
      } else {
        // No file, just create/update book with URL (if provided)
        this.processSaveBook(formValue);
      }
    } else {
      alert('Vui lòng nhập đầy đủ thông tin bắt buộc!');
    }
  }

  private processSaveBook(formValue: any): void {
    // Tìm id tác giả từ tên nhập vào
    const authorName = formValue.authorInput || formValue.author || '';
    const authorObj = this.authors.find((a) => a.name === authorName);
    const authorIds = authorObj && authorObj.id ? [authorObj.id] : [];

    const categoryName = formValue.categoryInput || '';
    const categoryObj = this.categories.find((c) => c.name === categoryName);
    const categoryId = categoryObj && categoryObj.id ? categoryObj.id : 0;

    // Lấy publisherId từ dropdown
    const publisherName = formValue.publisherInput || '';
    const publisherObj = this.publishers.find((p) => p.name === publisherName);
    const publisherId = publisherObj && publisherObj.id ? publisherObj.id : null;

    if (this.isEditing && this.currentBook.id) {
      // Update existing book
      const bookToUpdate = {
        ...this.currentBook,
        ...formValue,
        author: authorName,
        authorIds: authorIds,
        category: categoryName,
        categoryId: categoryId,
        publisher: publisherName,
        publisherId: publisherId,
        publicationYear: formValue.publishYear || new Date().getFullYear(),
        totalCopies: formValue.totalCopies || 1,
        availableCopies: formValue.availableCopies || formValue.totalCopies || 1,
      };

      console.log('Updating book with data:', bookToUpdate);
      this.bookService.updateBook(this.currentBook.id, bookToUpdate).subscribe({
        next: (updatedBook) => {
          const index = this.books.findIndex((b) => b.id === updatedBook.id);
          if (index !== -1) {
            this.books[index] = updatedBook;
          }
          this.filteredBooks = [...this.books];
          this.updatePagination();
          this.updateStatistics();
          alert('Cập nhật sách thành công!');
          this.closeModal();
          // Clear file selection after successful save
          this.selectedFile = null;
          this.imagePreview = null;
        },
        error: (error) => {
          console.error('Error updating book:', error);
          alert('Có lỗi xảy ra khi cập nhật sách!');
        },
      });
    } else {
      // Thêm mới sách - Đảm bảo trường authorIds gửi lên
      const bookRequest = {
        title: formValue.title,
        author: authorName,
        authorIds: authorIds,
        category: categoryName,
        categoryId: categoryId,
        publisher: publisherName,
        publisherId: publisherId,
        publicationYear: formValue.publishYear || new Date().getFullYear(),
        genre: formValue.genre,
        totalCopies: formValue.totalCopies || 1,
        availableCopies: formValue.totalCopies || 1,
        isbn: formValue.isbn,
        description: formValue.description,
        pages: formValue.pages,
        status: formValue.status,
        coverImage: formValue.coverImage,
      };

      console.log('Sending book request:', bookRequest);

      this.bookService.createBook(bookRequest).subscribe({
        next: (newBook) => {
          this.books.push(newBook);
          this.filteredBooks = [...this.books];
          this.updatePagination();
          this.updateStatistics();
          alert('Thêm sách thành công!');
          this.closeModal();
          // Clear file selection after successful save
          this.selectedFile = null;
          this.imagePreview = null;
        },
        error: (error) => {
          console.error('Error creating book:', error);
          alert('Có lỗi xảy ra khi thêm sách!');
        },
      });
    }
  }

  editBook(book: Book): void {
    this.openEditModal(book);
  }

  viewBook(book: Book): void {
    this.openDetailModal(book);
  }

  confirmDelete(book: Book): void {
    this.openDeleteModal(book);
  }

  deleteBook(): void {
    if (this.bookToDelete && this.bookToDelete.id) {
      this.bookService.deleteBook(this.bookToDelete.id).subscribe({
        next: () => {
          const index = this.books.findIndex((b) => b.id === this.bookToDelete!.id);
          if (index !== -1) {
            this.books.splice(index, 1);
            this.filteredBooks = [...this.books];
            this.updatePagination();
            this.updateStatistics();
          }
          alert('Xóa sách thành công!');
          this.closeDeleteModal();

          // Close detail modal if it's the same book
          if (
            this.selectedBook &&
            this.bookToDelete &&
            this.selectedBook.id === this.bookToDelete.id
          ) {
            this.closeDetailModal();
          }
        },
        error: (error) => {
          console.error('Error deleting book:', error);
          alert('Có lỗi xảy ra khi xóa sách!');
          this.closeDeleteModal();
        },
      });
    } else {
      this.closeDeleteModal();
    }
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  // Override existing onAuthorInput method
  onAuthorInput(event: any): void {
    const query = event.target.value.trim();
    if (query.length >= 2) {
      this.bookService.getAuthorSuggestions(query).subscribe({
        next: (names: string[]) => {
          this.authorSuggestions = names.map((name) => ({ name }));
        },
        error: (error) => {
          console.error('Error getting author suggestions:', error);
          this.authorSuggestions = [];
        },
      });
    } else {
      this.authorSuggestions = [];
    }
  }

  // Override existing onCategoryInput method
  onCategoryInput(event: any): void {
    const query = event.target.value.trim();
    if (query.length >= 2) {
      this.bookService.getCategorySuggestions(query).subscribe({
        next: (names: string[]) => {
          this.categorySuggestions = names.map((name) => ({ name }));
        },
        error: (error) => {
          console.error('Error getting category suggestions:', error);
          this.categorySuggestions = [];
        },
      });
    } else {
      this.categorySuggestions = [];
    }
  }

  // Override existing onPublisherInput method
  onPublisherInput(event: any): void {
    const query = event.target.value.trim();
    if (query.length >= 2) {
      this.bookService.getPublisherSuggestions(query).subscribe({
        next: (names: string[]) => {
          this.publisherSuggestions = names.map((name) => ({ name }));
        },
        error: (error) => {
          console.error('Error getting publisher suggestions:', error);
          this.publisherSuggestions = [];
        },
      });
    } else {
      this.publisherSuggestions = [];
    }
  }

  selectAuthorSuggestion(suggestion: { name: string }): void {
    this.bookForm.patchValue({
      authorInput: suggestion.name,
      author: suggestion.name,
    });
    this.currentBook.author = suggestion.name;
    this.authorSuggestions = [];
  }

  selectCategorySuggestion(suggestion: { name: string }): void {
    // Tìm categoryId từ danh sách categories
    const categoryObj = this.categories.find((c) => c.name === suggestion.name);
    this.bookForm.patchValue({
      categoryInput: suggestion.name,
      categoryId: categoryObj ? categoryObj.id : 0,
    });
    this.currentBook.categoryId = categoryObj ? categoryObj.id : 0;
    this.categorySuggestions = [];
  }

  selectPublisherSuggestion(suggestion: { name: string }): void {
    this.bookForm.patchValue({
      publisherInput: suggestion.name,
      publisher: suggestion.name,
    });
    this.currentBook.publisher = suggestion.name;
    this.publisherSuggestions = [];
  }

  selectAuthorSuggestionByName(name: string): void {
    this.selectAuthorSuggestion({ name });
  }

  selectCategorySuggestionByName(event: Event): void {
    const value = (event.target as HTMLSelectElement)?.value || '';
    this.selectCategorySuggestion({ name: value });
  }

  selectPublisherSuggestionByName(name: string): void {
    this.selectPublisherSuggestion({ name });
  }

  // Sự kiện chọn tác giả từ dropdown
  onAuthorSelect(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const authorObj = this.authors.find((a) => a.name === select.value);
    if (authorObj) {
      this.selectAuthorSuggestion(authorObj);
    }
  }

  // Sự kiện chọn nhà xuất bản từ dropdown
  onPublisherSelect(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const publisherObj = this.publishers.find((p) => p.name === select.value);
    if (publisherObj) {
      this.selectPublisherSuggestion(publisherObj);
    }
  }

  /**
   * Handle file selection
   */
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;

      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Vui lòng chọn file ảnh!');
        this.selectedFile = null;
        return;
      }

      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        alert('Kích thước file phải nhỏ hơn 10MB!');
        this.selectedFile = null;
        return;
      }

      // Preview image
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * Clear selected image
   */
  clearImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.bookForm.patchValue({ coverImage: '' });
  }

  onSubmit() {
    if (this.bookForm.valid) {
      const formValue = this.bookForm.value;

      // If file selected, upload first
      if (this.selectedFile) {
        this.bookService.uploadCoverImage(this.selectedFile).subscribe({
          next: (response) => {
            // Set uploaded image URL
            formValue.coverImage = 'http://localhost:8081' + response.imageUrl;
            this.createOrUpdateBook(formValue);
          },
          error: (error) => {
            console.error('Error uploading image:', error);
            alert('Lỗi khi upload ảnh: ' + (error.error?.error || 'Unknown error'));
          },
        });
      } else {
        // No file, just create/update book with URL (if provided)
        this.createOrUpdateBook(formValue);
      }
    } else {
      alert('Vui lòng điền đầy đủ thông tin bắt buộc!');
    }
  }

  private createOrUpdateBook(formValue: any) {
    if (!this.isEditing) {
      // Add new book
      const newBook: BookCreateRequest = {
        title: formValue.title,
        author: formValue.authorInput,
        categoryId: formValue.categoryId,
        publisher: formValue.publisherInput,
        publicationYear: formValue.publishYear || new Date().getFullYear(),
        isbn: formValue.isbn,
        totalCopies: formValue.totalCopies,
        description: formValue.description,
        publishYear: formValue.publishYear,
        pages: formValue.pages,
        genre: formValue.genre,
        coverImage: formValue.coverImage,
      };

      this.bookService.createBook(newBook).subscribe({
        next: (book) => {
          this.books.push(book);
          this.filteredBooks = [...this.books];
          this.updatePagination();
          this.updateStatistics();
          alert('Thêm sách thành công!');
          this.closeModal();
        },
        error: (error) => {
          console.error('Error adding book:', error);
          alert('Có lỗi xảy ra khi thêm sách!');
        },
      });
    } else {
      // Update existing book
      if (!this.currentBook.id) {
        alert('Không tìm thấy ID sách để cập nhật!');
        return;
      }

      const updatedBook: Book = {
        ...this.currentBook,
        title: formValue.title,
        author: formValue.authorInput,
        categoryId: formValue.categoryId,
        publisher: formValue.publisherInput,
        isbn: formValue.isbn,
        totalCopies: formValue.totalCopies,
        description: formValue.description,
        publishYear: formValue.publishYear,
        pages: formValue.pages,
        genre: formValue.genre,
        status: formValue.status,
        coverImage: formValue.coverImage,
      };

      this.bookService.updateBook(this.currentBook.id, updatedBook).subscribe({
        next: (book) => {
          const index = this.books.findIndex((b) => b.id === book.id);
          if (index !== -1) {
            this.books[index] = book;
            this.filteredBooks = [...this.books];
            this.updatePagination();
            this.updateStatistics();
            alert('Cập nhật sách thành công!');
            this.closeModal();
          }
        },
        error: (error) => {
          console.error('Error updating book:', error);
          alert('Có lỗi xảy ra khi cập nhật sách!');
        },
      });
    }
  }
}
