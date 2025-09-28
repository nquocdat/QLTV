export interface Loan {
  id?: number;
  bookId: number;
  bookTitle?: string;
  bookAuthor?: string;
  patronId: number;
  patronName?: string;
  patronEmail?: string;
  loanDate: string;
  dueDate: string;
  returnDate?: string;
  status: string;
  fineAmount?: number;
  isRenewed?: boolean;
  renewalCount?: number;
}

export interface BorrowRequest {
  bookId: number;
  patronId: number;
}
