import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { PublisherService } from '../../../services/publisher.service';
import { Publisher } from '../../../models/publisher.model';

@Component({
  selector: 'app-publisher-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './publisher-management.html',
  styleUrls: ['./publisher-management.css'],
})
export class PublisherManagementComponent implements OnInit {
  private publisherService = inject(PublisherService);
  private fb = inject(FormBuilder);

  publishers: Publisher[] = [];
  filteredPublishers: Publisher[] = [];
  searchQuery = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Modal
  showPublisherModal = false;
  modalMode: 'add' | 'edit' | 'view' = 'add';
  selectedPublisher: Publisher | null = null;

  // Form
  publisherForm: FormGroup;
  isLoading = false;

  constructor() {
    this.publisherForm = this.fb.group({
      name: ['', [Validators.required]],
      address: [''],
      phoneNumber: [''],
      email: ['', [Validators.email]],
      website: [''],
    });
  }

  ngOnInit(): void {
    this.loadPublishers();
  }

  loadPublishers(): void {
    this.isLoading = true;
    this.publisherService
      .getAllPublishers(this.currentPage - 1, this.pageSize, this.searchQuery)
      .subscribe({
        next: (response) => {
          this.publishers = response.content || [];
          this.totalItems = response.totalElements || 0;
          this.totalPages = response.totalPages || 0;
          this.filteredPublishers = [...this.publishers];
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading publishers:', error);
          this.publishers = [];
          this.filteredPublishers = [];
          this.isLoading = false;
        },
      });
  }

  onSearch(): void {
    this.currentPage = 1;
    this.loadPublishers();
  }

  // Pagination methods
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadPublishers();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadPublishers();
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadPublishers();
    }
  }

  getVisiblePages(): number[] {
    const delta = 2;
    const range = [];
    const rangeWithDots = [];

    for (
      let i = Math.max(2, this.currentPage - delta);
      i <= Math.min(this.totalPages - 1, this.currentPage + delta);
      i++
    ) {
      range.push(i);
    }

    if (this.currentPage - delta > 2) {
      rangeWithDots.push(1, -1);
    } else {
      rangeWithDots.push(1);
    }

    rangeWithDots.push(...range);

    if (this.currentPage + delta < this.totalPages - 1) {
      rangeWithDots.push(-1, this.totalPages);
    } else {
      rangeWithDots.push(this.totalPages);
    }

    return rangeWithDots.filter((v) => v > 0);
  }

  // Modal methods
  openAddPublisherModal(): void {
    this.modalMode = 'add';
    this.selectedPublisher = null;
    this.publisherForm.reset();
    this.showPublisherModal = true;
  }

  editPublisher(publisher: Publisher): void {
    this.modalMode = 'edit';
    this.selectedPublisher = publisher;
    this.publisherForm.patchValue({
      name: publisher.name,
      address: publisher.address || '',
      phoneNumber: publisher.phoneNumber || '',
      email: publisher.email || '',
      website: publisher.website || '',
    });
    this.showPublisherModal = true;
  }

  viewPublisher(publisher: Publisher): void {
    this.modalMode = 'view';
    this.selectedPublisher = publisher;
    this.showPublisherModal = true;
  }

  closeModal(): void {
    this.showPublisherModal = false;
    this.selectedPublisher = null;
    this.publisherForm.reset();
  }

  onSubmit(): void {
    if (this.publisherForm.valid) {
      this.isLoading = true;
      const formData = this.publisherForm.value;

      if (this.modalMode === 'add') {
        this.publisherService.createPublisher(formData).subscribe({
          next: (response) => {
            console.log('Publisher created successfully:', response);
            this.closeModal();
            this.loadPublishers();
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error creating publisher:', error);
            this.isLoading = false;
          },
        });
      } else if (this.modalMode === 'edit' && this.selectedPublisher) {
        this.publisherService.updatePublisher(this.selectedPublisher.id, formData).subscribe({
          next: (response) => {
            console.log('Publisher updated successfully:', response);
            this.closeModal();
            this.loadPublishers();
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error updating publisher:', error);
            this.isLoading = false;
          },
        });
      }
    }
  }

  deletePublisher(publisher: Publisher): void {
    if (confirm(`Bạn có chắc chắn muốn xóa nhà xuất bản "${publisher.name}"?`)) {
      this.isLoading = true;
      this.publisherService.deletePublisher(publisher.id).subscribe({
        next: () => {
          console.log('Publisher deleted successfully');
          this.loadPublishers();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error deleting publisher:', error);
          this.isLoading = false;
        },
      });
    }
  }

  getPublisherInitials(name: string): string {
    return name
      .split(' ')
      .map((word) => word.charAt(0).toUpperCase())
      .join('')
      .substring(0, 2);
  }

  // Math object for template
  Math = Math;
}
