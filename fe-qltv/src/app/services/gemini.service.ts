import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class GeminiService {
  private readonly API_URL = 'http://localhost:8081/api/gemini/chat'; // ✅ endpoint backend

  constructor() {}

  async chat(message: string): Promise<string> {
    try {
      const response = await fetch(this.API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message }),
      });

      // Parse JSON phản hồi từ backend Spring Boot
      const data = await response.json();

      if (data.success) {
        return data.message; // ✅ backend trả đúng field này
      } else {
        return data.error || '❌ Có lỗi từ Gemini backend.';
      }
    } catch (error) {
      console.error('❌ Lỗi kết nối Gemini:', error);
      return '❌ Không thể kết nối tới máy chủ Gemini.';
    }
  }
}
