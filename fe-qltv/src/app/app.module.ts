import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaymentService } from './services/payment.service';

@NgModule({
  imports: [CommonModule],
  providers: [PaymentService],
})
export class AppModule {}
