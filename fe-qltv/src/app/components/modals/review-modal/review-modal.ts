import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../../services/review.service';

@Component({
  selector: 'app-review-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-modal.html',
  styleUrls: ['./review-modal.css'],
})
export class ReviewModal {
  @Input() bookId!: number;
  @Input() bookTitle: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() reviewSubmitted = new EventEmitter<void>();

  selectedRating: number = 0;
  hoverRating: number = 0;
  comment: string = '';
  isSubmitting: boolean = false;
  errorMessage: string = '';

  stars = [1, 2, 3, 4, 5];

  constructor(private reviewService: ReviewService) {}

  setRating(rating: number): void {
    this.selectedRating = rating;
  }

  setHoverRating(rating: number): void {
    this.hoverRating = rating;
  }

  clearHoverRating(): void {
    this.hoverRating = 0;
  }

  isStarFilled(star: number): boolean {
    return star <= (this.hoverRating || this.selectedRating);
  }

  getRatingText(rating: number): string {
    const texts: { [key: number]: string } = {
      1: '😞 Rất tệ',
      2: '😕 Tệ',
      3: '😐 Bình thường',
      4: '😊 Tốt',
      5: '😍 Xuất sắc',
    };
    return texts[rating] || '';
  }

  onSubmit(): void {
    if (this.selectedRating === 0) {
      this.errorMessage = 'Vui lòng chọn số sao đánh giá';
      return;
    }

    if (this.comment.trim().length === 0) {
      this.errorMessage = 'Vui lòng nhập nhận xét';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.reviewService
      .createReview({
        bookId: this.bookId,
        rating: this.selectedRating,
        comment: this.comment.trim(),
      })
      .subscribe({
        next: (response) => {
          console.log('Review created:', response);
          alert('✅ Đánh giá của bạn đã được gửi và đang chờ duyệt!');
          this.reviewSubmitted.emit();
          this.onCancel();
        },
        error: (error) => {
          console.error('Error creating review:', error);
          this.errorMessage = error.error?.message || 'Có lỗi xảy ra khi gửi đánh giá';
          this.isSubmitting = false;
        },
      });
  }

  onCancel(): void {
    this.close.emit();
  }
}
