import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class StatusTranslatorService {
  /**
   * Dịch trạng thái phiếu mượn sang tiếng Việt
   */
  translateLoanStatus(status: string): string {
    const translations: { [key: string]: string } = {
      PENDING_PAYMENT: 'Chờ thanh toán',
      BORROWED: 'Đang mượn',
      RETURNED: 'Đã trả',
      OVERDUE: 'Quá hạn',
      RENEWED: 'Đã gia hạn',
      PENDING_RETURN: 'Chờ xác nhận trả',
    };
    return translations[status?.toUpperCase()] || status;
  }

  /**
   * Dịch trạng thái thanh toán sang tiếng Việt
   */
  translatePaymentStatus(status: string): string {
    const translations: { [key: string]: string } = {
      PENDING: 'Chờ xác nhận',
      CONFIRMED: 'Đã xác nhận',
      FAILED: 'Thất bại',
      REFUNDED: 'Đã hoàn tiền',
    };
    return translations[status?.toUpperCase()] || status;
  }

  /**
   * Dịch phương thức thanh toán sang tiếng Việt
   */
  translatePaymentMethod(method: string): string {
    const translations: { [key: string]: string } = {
      CASH: 'Tiền mặt',
      VNPAY: 'VNPay',
    };
    return translations[method?.toUpperCase()] || method;
  }

  /**
   * Dịch trạng thái bản sao sang tiếng Việt
   */
  translateCopyStatus(status: string): string {
    const translations: { [key: string]: string } = {
      AVAILABLE: 'Có sẵn',
      BORROWED: 'Đang mượn',
      RESERVED: 'Đã đặt trước',
      LOST: 'Mất',
      REPAIRING: 'Đang sửa chữa',
    };
    return translations[status?.toUpperCase()] || status;
  }

  /**
   * Dịch tình trạng sách sang tiếng Việt
   */
  translateConditionStatus(status: string): string {
    const translations: { [key: string]: string } = {
      NEW: 'Mới',
      GOOD: 'Tốt',
      FAIR: 'Khá',
      POOR: 'Kém',
      DAMAGED: 'Hỏng',
    };
    return translations[status?.toUpperCase()] || status;
  }

  /**
   * Lấy CSS class cho trạng thái phiếu mượn
   */
  getLoanStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
      BORROWED: 'bg-blue-100 text-blue-800',
      RETURNED: 'bg-green-100 text-green-800',
      OVERDUE: 'bg-red-100 text-red-800',
      RENEWED: 'bg-purple-100 text-purple-800',
      PENDING_RETURN: 'bg-orange-100 text-orange-800',
    };
    return classes[status?.toUpperCase()] || 'bg-gray-100 text-gray-800';
  }

  /**
   * Lấy CSS class cho trạng thái thanh toán
   */
  getPaymentStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      CONFIRMED: 'bg-green-100 text-green-800',
      FAILED: 'bg-red-100 text-red-800',
      REFUNDED: 'bg-blue-100 text-blue-800',
    };
    return classes[status?.toUpperCase()] || 'bg-gray-100 text-gray-800';
  }
}
