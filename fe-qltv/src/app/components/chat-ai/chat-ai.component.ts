import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GeminiService } from '../../services/gemini.service';

@Component({
  selector: 'app-chat-ai',
  standalone: true, // ⚡ Thêm dòng này
  imports: [CommonModule, FormsModule], // ⚡ Thêm dòng này
  templateUrl: './chat-ai.component.html',
  styleUrls: ['./chat-ai.component.css']
})
export class ChatAiComponent {
  userMessage = '';
  messages: { sender: string, text: string }[] = [];
  isOpen = false;

  constructor(private geminiService: GeminiService) {}

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  async sendMessage() {
    if (!this.userMessage.trim()) return;

    const userText = this.userMessage;
    this.messages.push({ sender: 'Bạn', text: userText });
    this.userMessage = '';

    const reply = await this.geminiService.chat(userText);
    this.messages.push({ sender: 'Gemini', text: reply });
  }
}
