import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RAGChatbotService, RAGChatResponse, ProductSuggestion } from '../../services/rag-chatbot.service';
import { AuthService } from '../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';

interface Message {
    id: string;
    text: string;
    sender: 'user' | 'bot';
    timestamp: Date;
    isLoading?: boolean;
    products?: ProductSuggestion[];
    isMarkdown?: boolean;
    isWelcomeMessage?: boolean;
}

@Component({
    selector: 'app-chatbot',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chatbot.component.html',
    styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit, OnDestroy, AfterViewChecked {
    @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

    messages: Message[] = [];
    inputMessage: string = '';
    isOpen: boolean = false;
    isLoading: boolean = false;
    userId: string | undefined;  // UUID string
    sessionId: string | null = null;

    // Report modal state
    isReportModalOpen: boolean = false;
    selectedMessage: Message | null = null;
    reportDescription: string = '';
    isSubmittingReport: boolean = false;

    private destroy$ = new Subject<void>();
    private messageCounter = 0;
    private shouldScrollToBottom = false;

    // Quick action suggestions
    quickActions = [
        'Tư vấn laptop gaming dưới 20 triệu',
        'Laptop văn phòng nhẹ, pin trâu',
        'PC đồ họa render video',
        'Máy tính học lập trình'
    ];

    constructor(
        private ragChatbotService: RAGChatbotService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Get user ID from auth service (UUID string)
        const userIdStr = this.authService.getUserIdSafe();
        if (userIdStr) {
            this.userId = userIdStr;
        }

        // Get existing session
        this.sessionId = this.ragChatbotService.currentSessionId;

        // Initialize with welcome message
        this.addBotMessage(
            'Xin chào! 👋 Tôi là **trợ lý AI tư vấn máy tính** của THComputer.\n\n' +
            'Tôi có thể giúp bạn:\n' +
            '• Tư vấn chọn laptop/PC phù hợp nhu cầu\n' +
            '• So sánh các sản phẩm\n' +
            '• Giải đáp thắc mắc về cấu hình\n\n' +
            'Hãy cho tôi biết **ngân sách** và **nhu cầu sử dụng** của bạn nhé!',
            false,
            [],
            true,
            true // This is a welcome message
        );
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    ngAfterViewChecked(): void {
        if (this.shouldScrollToBottom) {
            this.scrollToBottom();
            this.shouldScrollToBottom = false;
        }
    }

    toggleChat(): void {
        this.isOpen = !this.isOpen;
        if (this.isOpen) {
            this.shouldScrollToBottom = true;
        }
    }

    sendMessage(message?: string): void {
        const messageToSend = message || this.inputMessage;

        if (!messageToSend.trim()) {
            return;
        }

        // Add user message
        this.addUserMessage(messageToSend);
        this.inputMessage = '';

        // Show loading state
        this.isLoading = true;
        const loadingMessageId = this.addBotMessage('Đang suy nghĩ...', true);

        // Send to RAG backend
        this.ragChatbotService.sendMessage(messageToSend, this.userId)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (response: RAGChatResponse) => {
                    this.isLoading = false;
                    this.sessionId = response.sessionId;

                    // Remove loading message
                    this.messages = this.messages.filter(m => m.id !== loadingMessageId);

                    // Add bot response with products
                    this.addBotMessage(
                        response.answer,
                        false,
                        response.products,
                        true
                    );
                },
                error: (error) => {
                    this.isLoading = false;
                    console.error('RAG Chatbot error:', error);
                    this.messages = this.messages.filter(m => m.id !== loadingMessageId);
                    this.addBotMessage(
                        'Xin lỗi, tôi gặp sự cố khi xử lý yêu cầu. Vui lòng thử lại sau.',
                        false,
                        [],
                        false
                    );
                }
            });
    }

    private addUserMessage(text: string): void {
        const message: Message = {
            id: `msg-${++this.messageCounter}`,
            text,
            sender: 'user',
            timestamp: new Date()
        };
        this.messages.push(message);
        this.shouldScrollToBottom = true;
    }

    private addBotMessage(
        text: string,
        isLoading: boolean = false,
        products: ProductSuggestion[] = [],
        isMarkdown: boolean = false,
        isWelcomeMessage: boolean = false
    ): string {
        const id = `msg-${++this.messageCounter}`;
        const message: Message = {
            id,
            text,
            sender: 'bot',
            timestamp: new Date(),
            isLoading,
            products,
            isMarkdown,
            isWelcomeMessage
        };
        this.messages.push(message);
        this.shouldScrollToBottom = true;
        return id;
    }

    private scrollToBottom(): void {
        try {
            if (this.messagesContainer) {
                this.messagesContainer.nativeElement.scrollTop =
                    this.messagesContainer.nativeElement.scrollHeight;
            }
        } catch (err) {
            console.error('Scroll error:', err);
        }
    }

    onKeyPress(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }

    clearChat(): void {
        this.ragChatbotService.clearSession();
        this.sessionId = null;
        this.messages = [];
        this.addBotMessage(
            'Đã xóa lịch sử chat. Tôi sẵn sàng hỗ trợ bạn! 🚀',
            false,
            [],
            false,
            true // This is a system message
        );
    }

    viewProduct(productId: string): void {
        this.router.navigate(['/product', productId]);
    }

    formatPrice(price: number): string {
        return this.ragChatbotService.formatPrice(price);
    }

    // Simple markdown to HTML converter
    parseMarkdown(text: string): string {
        if (!text) return '';

        return text
            // Bold
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            // Italic
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            // Line breaks
            .replace(/\n/g, '<br>')
            // Lists
            .replace(/^• /gm, '&bull; ');
    }

    trackByMessageId(index: number, message: Message): string {
        return message.id;
    }

    trackByProductId(index: number, product: ProductSuggestion): string {
        return product.id;
    }

    // --- Report Modal Methods ---

    openReportModal(message: Message): void {
        this.selectedMessage = message;
        this.isReportModalOpen = true;
    }

    closeReportModal(): void {
        this.isReportModalOpen = false;
        this.selectedMessage = null;
        this.reportDescription = '';
        this.isSubmittingReport = false;
    }

    submitReport(): void {
        if (!this.reportDescription.trim() || !this.selectedMessage || !this.sessionId) {
            return;
        }

        this.isSubmittingReport = true;

        const reportData = {
            sessionId: this.sessionId,
            messageId: this.selectedMessage.id,
            userFeedback: this.reportDescription,
            userId: this.userId,
            timestamp: new Date().toISOString()
        };

        this.ragChatbotService.reportIncorrectAnswer(reportData)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.isSubmittingReport = false;
                    this.closeReportModal();
                    // Optionally, show a success message
                    alert('Cảm ơn bạn đã gửi báo cáo!');
                },
                error: (error) => {
                    this.isSubmittingReport = false;
                    console.error('Failed to submit report:', error);
                    // Optionally, show an error message
                    alert('Gửi báo cáo thất bại. Vui lòng thử lại.');
                }
            });
    }
}
