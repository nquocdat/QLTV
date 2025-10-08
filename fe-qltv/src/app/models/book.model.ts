export interface Book {
  id?: number;
  title: string;
  author: string;
  isbn?: string;
  categoryId?: number;
  publisherId?: number;
  publisher?: string;
  publishYear?: number;
  publicationYear?: number;
  pages?: number;
  genre?: string;
  status?: string;
  description?: string;
  coverImage?: string;
  totalCopies: number;
  availableCopies?: number;
  addedDate?: Date;
  createdDate?: Date;
  categoryName?: string; // Thêm thuộc tính categoryName vào interface Book
  publisherName?: string; // Thêm thuộc tính publisherName vào interface Book
  fee?: number; // Giá mượn sách
}

export interface BookCreateRequest {
  title: string;
  author: string;
  isbn?: string;
  categoryId?: number;
  publisher?: string;
  publishYear?: number;
  publicationYear: number;
  pages?: number;
  genre?: string;
  description?: string;
  coverImage?: string;
  totalCopies: number;
}
