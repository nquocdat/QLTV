import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../../../services/loan.service';
import { LoanPaymentService } from '../../../services/loan-payment.service';
import { StatusTranslatorService } from '../../../services/status-translator.service';
import { Loan } from '../../../models/loan.model';

@Component({
  selector: 'app-loan-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './loan-management.html',
  styleUrl: './loan-management.css',
})
export class LoanManagement implements OnInit {
  loans: Loan[] = [];
  filteredLoans: Loan[] = [];
  loanForm!: FormGroup;

  // Modal state
  showLoanModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  selectedLoan: Loan | null = null;

  // Loading state
  isLoading = false;

  // Filters
  searchQuery = '';
  selectedStatus = '';
  dateFrom = '';
  dateTo = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Stats
  activeLoanCount = 0;
  overdueLoanCount = 0;
  returnedLoanCount = 0;
  totalLoanCount = 0;

  // Return Modal
  showReturnModal = false;
  returningLoan: any = null;
  isDamaged = false;
  damageFine = 0;
  damageNotes = '';

  // Math for template
  Math = Math;

  constructor(
    private fb: FormBuilder,
    private loanService: LoanService,
    private loanPaymentService: LoanPaymentService,
    public statusTranslator: StatusTranslatorService
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadLoans();
  }

  initializeForm(): void {
    this.loanForm = this.fb.group({
      patronEmail: ['', [Validators.required, Validators.email]],
      bookIsbn: ['', Validators.required],
      loanDate: [new Date().toISOString().split('T')[0], Validators.required],
      dueDate: ['', Validators.required],
      notes: [''],
    });
  }

  loadLoans(): void {
    this.loanService.getAllLoans().subscribe({
      next: (loans: Loan[]) => {
        this.loans = loans;
        this.totalItems = this.loans.length;
        this.calculateTotalPages();
        this.calculateStats();
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading loans:', error);
        this.loans = [];
        this.totalItems = 0;
        this.calculateTotalPages();
        this.calculateStats();
        this.applyFilters();
      },
    });
  }

  calculateStats(): void {
    this.activeLoanCount = this.loans.filter((loan) => loan.status === 'ACTIVE').length;
    this.overdueLoanCount = this.loans.filter((loan) => loan.status === 'OVERDUE').length;
    this.returnedLoanCount = this.loans.filter((loan) => loan.status === 'RETURNED').length;
    this.totalLoanCount = this.loans.length;
  }

  onSearch(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = [...this.loans];

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (loan) =>
          (loan.patronName || '').toLowerCase().includes(query) ||
          (loan.patronEmail || '').toLowerCase().includes(query) ||
          (loan.bookTitle || '').toLowerCase().includes(query) ||
          (loan.bookAuthor || '').toLowerCase().includes(query) ||
          `LN${loan.id?.toString().padStart(3, '0')}`.toLowerCase().includes(query)
      );
    }

    // Status filter
    if (this.selectedStatus) {
      filtered = filtered.filter(
        (loan) => loan.status?.toUpperCase() === this.selectedStatus.toUpperCase()
      );
    }

    // Date range filter
    if (this.dateFrom) {
      const fromDate = new Date(this.dateFrom);
      filtered = filtered.filter((loan) => new Date(loan.loanDate) >= fromDate);
    }

    if (this.dateTo) {
      const toDate = new Date(this.dateTo);
      filtered = filtered.filter((loan) => new Date(loan.loanDate) <= toDate);
    }

    // Update totals
    this.totalItems = filtered.length;
    this.calculateTotalPages();

    // Apply pagination
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.filteredLoans = filtered.slice(startIndex, endIndex);
  }

  calculateTotalPages(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.applyFilters();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.applyFilters();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.applyFilters();
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

  openCreateLoanModal(): void {
    this.modalMode = 'create';
    this.selectedLoan = null;
    this.initializeForm();
    this.showLoanModal = true;
  }

  editLoan(loan: Loan): void {
    this.modalMode = 'edit';
    this.selectedLoan = loan;
    this.loanForm.patchValue({
      patronEmail: loan.patronEmail,
      bookIsbn: '', // Not available in Loan model
      loanDate: loan.loanDate.split('T')[0],
      dueDate: loan.dueDate.split('T')[0],
      notes: '', // Not available in Loan model
    });
    this.showLoanModal = true;
  }

  viewLoan(loan: Loan): void {
    this.modalMode = 'view';
    this.selectedLoan = loan;
    this.showLoanModal = true;
  }

  closeModal(): void {
    this.showLoanModal = false;
    this.selectedLoan = null;
    this.initializeForm();
  }

  onSubmit(): void {
    if (this.loanForm.valid) {
      this.isLoading = true;

      const formData = this.loanForm.value;

      if (this.modalMode === 'create') {
        const newLoan: Loan = {
          id: this.loans.length + 1,
          bookId: 1, // TODO: Get from book selection
          patronId: 1, // TODO: Get from patron selection
          patronName: 'Người dùng mới', // This would be fetched from user service
          patronEmail: formData.patronEmail,
          bookTitle: 'Sách mới', // This would be fetched from book service
          bookAuthor: 'Tác giả',
          loanDate: formData.loanDate,
          dueDate: formData.dueDate,
          status: 'ACTIVE',
        };

        this.loans.push(newLoan);
        console.log('Tạo phiếu mượn mới:', newLoan);
      } else if (this.modalMode === 'edit' && this.selectedLoan) {
        const loanIndex = this.loans.findIndex((l) => l.id === this.selectedLoan!.id);
        if (loanIndex !== -1) {
          this.loans[loanIndex] = {
            ...this.loans[loanIndex],
            patronEmail: formData.patronEmail,
            loanDate: formData.loanDate,
            dueDate: formData.dueDate,
          };
          console.log('Cập nhật phiếu mượn:', this.loans[loanIndex]);
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

  returnBook(loan: Loan): void {
    if (confirm(`Xác nhận trả sách "${loan.bookTitle}" của ${loan.patronName}?`)) {
      if (!loan.id) return;
      this.loanService.returnBook(loan.id).subscribe({
        next: () => {
          alert('Xác nhận trả sách thành công!');
          this.loadLoans();
        },
        error: (error) => {
          alert('Có lỗi khi xác nhận trả sách!');
        },
      });
    }
  }

  confirmReturn(loan: Loan): void {
    if (confirm(`Xác nhận đã nhận lại sách "${loan.bookTitle}" từ ${loan.patronName}?`)) {
      if (!loan.id) return;
      this.loanService.confirmReturnBook(loan.id).subscribe({
        next: () => {
          alert('Xác nhận trả sách thành công!');
          this.loadLoans();
        },
        error: (error) => {
          alert('Có lỗi khi xác nhận trả sách!');
        },
      });
    }
  }

  renewLoan(loan: Loan): void {
    if (confirm(`Gia hạn 14 ngày cho phiếu mượn "LN${loan.id?.toString().padStart(3, '0')}"?`)) {
      const loanIndex = this.loans.findIndex((l) => l.id === loan.id);
      if (loanIndex !== -1) {
        const currentDueDate = new Date(this.loans[loanIndex].dueDate);
        currentDueDate.setDate(currentDueDate.getDate() + 14);
        this.loans[loanIndex].dueDate = currentDueDate.toISOString();

        // Update status if was overdue
        if (this.loans[loanIndex].status === 'OVERDUE') {
          this.loans[loanIndex].status = 'ACTIVE';
        }

        this.calculateStats();
        this.applyFilters();
        console.log('Đã gia hạn phiếu mượn:', `LN${loan.id?.toString().padStart(3, '0')}`);
      }
    }
  }

  getUserInitials(name: string): string {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-blue-100 text-blue-800';
      case 'RETURNED':
        return 'bg-green-100 text-green-800';
      case 'OVERDUE':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusDisplayName(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Đang mượn';
      case 'RETURNED':
        return 'Đã trả';
      case 'OVERDUE':
        return 'Quá hạn';
      default:
        return status;
    }
  }

  getDueDateClass(dueDate: string | Date): string {
    const today = new Date();
    const due = new Date(dueDate);
    const diffTime = due.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
      return 'text-red-600 font-medium'; // Overdue
    } else if (diffDays <= 3) {
      return 'text-yellow-600 font-medium'; // Due soon
    }
    return 'text-gray-900'; // Normal
  }

  // Return Modal Methods
  openReturnModal(loan: any): void {
    this.returningLoan = loan;
    this.isDamaged = false;
    this.damageFine = 0;
    this.damageNotes = '';
    this.showReturnModal = true;
  }

  closeReturnModal(): void {
    this.showReturnModal = false;
    this.returningLoan = null;
  }

  calculateOverdueDays(): number {
    if (!this.returningLoan) return 0;
    const dueDate = new Date(this.returningLoan.dueDate);
    const today = new Date();
    if (today <= dueDate) return 0;

    const diffTime = today.getTime() - dueDate.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  calculateOverdueFine(): number {
    return this.calculateOverdueDays() * 5000; // 5,000 VND/ngày
  }

  calculateTotalFine(): number {
    return this.calculateOverdueFine() + (this.isDamaged ? this.damageFine : 0);
  }

  confirmReturnBook(): void {
    if (!this.returningLoan) return;

    if (this.isDamaged && this.damageFine > 0) {
      // Trả sách với phí hỏng
      this.loanService
        .returnBookWithDamage(this.returningLoan.id, this.damageFine, this.damageNotes)
        .subscribe({
          next: () => {
            alert(
              'Đã trả sách thành công! Phí phạt: ' +
                this.calculateTotalFine().toLocaleString() +
                ' VND'
            );
            this.closeReturnModal();
            this.loadLoans();
          },
          error: (err) => {
            console.error('Error:', err);
            alert('Lỗi: ' + (err.error?.error || err.message));
          },
        });
    } else {
      // Trả sách bình thường
      this.loanService.returnBook(this.returningLoan.id).subscribe({
        next: () => {
          const fine = this.calculateOverdueFine();
          const msg =
            fine > 0
              ? 'Đã trả sách thành công! Phí phạt quá hạn: ' + fine.toLocaleString() + ' VND'
              : 'Đã trả sách thành công!';
          alert(msg);
          this.closeReturnModal();
          this.loadLoans();
        },
        error: (err) => {
          console.error('Error:', err);
          alert('Lỗi: ' + (err.error?.error || err.message));
        },
      });
    }
  }

  // Confirm Cash Payment
  confirmCashPayment(loan: any): void {
    const currentUser = JSON.parse(sessionStorage.getItem('auth-user') || '{}');
    const confirmedBy = currentUser.id;

    if (!confirmedBy) {
      alert('Không tìm thấy thông tin người dùng!');
      return;
    }

    if (
      !confirm(
        'Xác nhận đã thu tiền mặt ' +
          loan.depositAmount?.toLocaleString() +
          ' VND từ ' +
          loan.patronName +
          '?'
      )
    ) {
      return;
    }

    // Cần lấy paymentId từ loan
    // Giả sử loan có trường paymentId hoặc cần gọi API để lấy
    this.loanPaymentService.getPaymentByLoanId(loan.id).subscribe({
      next: (payment: any) => {
        // Kiểm tra nếu không phải thanh toán tiền mặt
        if (payment.paymentMethod !== 'CASH') {
          alert('Chỉ có thể xác nhận thanh toán tiền mặt!');
          return;
        }

        // Kiểm tra nếu đã được xác nhận
        if (payment.paymentStatus === 'CONFIRMED') {
          alert('Thanh toán này đã được xác nhận trước đó!');
          return;
        }

        this.loanPaymentService.confirmCashPayment(payment.id, confirmedBy).subscribe({
          next: () => {
            alert('Đã xác nhận thanh toán thành công!');
            this.loadLoans();
          },
          error: (err) => {
            console.error('Error confirming payment:', err);
            alert('Lỗi xác nhận thanh toán: ' + (err.error?.message || err.message));
          },
        });
      },
      error: (err) => {
        console.error('Error getting payment:', err);
        alert('Lỗi lấy thông tin thanh toán: ' + (err.error?.message || err.message));
      },
    });
  }
}
