import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex items-center gap-1">
      <div class="flex">
        <svg
          *ngFor="let star of stars; let i = index"
          [class]="
            star === 'full'
              ? 'text-yellow-400'
              : star === 'half'
              ? 'text-yellow-400'
              : 'text-gray-300'
          "
          class="w-5 h-5 fill-current"
          viewBox="0 0 24 24"
        >
          <defs *ngIf="star === 'half'">
            <linearGradient [id]="'half-' + i">
              <stop offset="50%" stop-color="currentColor" />
              <stop offset="50%" stop-color="#D1D5DB" />
            </linearGradient>
          </defs>
          <path
            [attr.fill]="star === 'half' ? 'url(#half-' + i + ')' : 'currentColor'"
            d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
          />
        </svg>
      </div>
      <span *ngIf="showRating" class="text-sm font-medium text-gray-700 ml-1">
        {{ rating | number : '1.1-1' }}
      </span>
      <span *ngIf="showCount && reviewCount > 0" class="text-sm text-gray-500 ml-1">
        ({{ reviewCount }})
      </span>
    </div>
  `,
  styles: [
    `
      :host {
        display: inline-block;
      }
    `,
  ],
})
export class StarRating {
  @Input() rating: number = 0;
  @Input() reviewCount: number = 0;
  @Input() showRating: boolean = true;
  @Input() showCount: boolean = true;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  get stars(): ('full' | 'half' | 'empty')[] {
    const fullStars = Math.floor(this.rating);
    const hasHalfStar = this.rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

    return [
      ...Array(fullStars).fill('full'),
      ...(hasHalfStar ? ['half'] : []),
      ...Array(emptyStars).fill('empty'),
    ];
  }
}
