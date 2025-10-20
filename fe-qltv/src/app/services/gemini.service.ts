import { Injectable } from '@angular/core';
// @ts-ignore
import { GoogleGenerativeAI } from "@google/generative-ai";



@Injectable({
  providedIn: 'root'
})
export class GeminiService {
  private genAI: GoogleGenerativeAI;
  private model: any;

  constructor() {
    const API_KEY = 'YOUR_API_KEY'; // 🔑 Thay bằng key bạn lấy ở Google AI Studio
    this.genAI = new GoogleGenerativeAI(API_KEY);
    this.model = this.genAI.getGenerativeModel({ model: 'gemini-1.5-flash' });
  }

  async chat(message: string): Promise<string> {
    try {
      const result = await this.model.generateContent(message);
      return result.response.text();
    } catch (err) {
      console.error('Gemini error:', err);
      return 'Xin lỗi, tôi đang gặp sự cố khi xử lý yêu cầu.';
    }
  }
}
