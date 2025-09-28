import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { Book } from '../../../models/book.model';
import { PageRequest } from '../../../models/pagination.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-books',
  templateUrl: './books.component.html',
  styleUrls: ['./books.component.css'],
  imports: [CommonModule],
})
export class BooksComponent implements OnInit {
  books: Book[] = [];
  searchQuery: string = '';
  loading = false;
  error = '';

  constructor(private route: ActivatedRoute, private bookService: BookService) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['search'] || '';
      this.fetchBooks();
    });
  }

  fetchBooks(): void {
    this.loading = true;
    this.error = '';
    const pageRequest: PageRequest = {
      page: 0,
      size: 20,
      sortBy: 'title',
      sortDir: 'asc',
    };
    this.bookService.searchBooksWithPagination(this.searchQuery, pageRequest).subscribe({
      next: (res) => {
        this.books = res.content;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không tìm thấy sách phù hợp.';
        this.books = [];
        this.loading = false;
      },
    });
  }
}
