import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../enviroment';

export interface RAGChatRequest {
    message: string;
    sessionId?: string;
}

export interface ProductSuggestion {
    id: string;
    name: string;
    price: number;
    category: string;
    brand: string;
    description: string;
    stock: number;
    warrantyPeriod: number;
    imageUrl?: string;
    similarityScore?: number;
}

export interface RAGChatResponse {
    answer: string;
    products: ProductSuggestion[];
    sessionId: string;
    timestamp: string;
}

export interface EmbeddingStatus {
    embeddingCount: number;
    timestamp: string;
}

export interface EmbeddingRebuildResponse {
    status: string;
    totalProducts: number;
    successCount: number;
    failedCount: number;
    durationMs: number;
    timestamp: string;
}

@Injectable({
    providedIn: 'root'
})
export class RAGChatbotService {
    private apiUrl = `${environment.apiUrl}/api/chat`;
    private embeddingUrl = `${environment.apiUrl}/api/embeddings`;

    private sessionId$ = new BehaviorSubject<string | null>(null);

    constructor(private http: HttpClient) {
        // Restore session from localStorage
        const savedSession = localStorage.getItem('rag_chat_session');
        if (savedSession) {
            this.sessionId$.next(savedSession);
        }
    }

    /**
     * Get current session ID
     */
    get currentSessionId(): string | null {
        return this.sessionId$.value;
    }

    /**
     * Send message to RAG chatbot
     */
    sendMessage(message: string, userId?: string): Observable<RAGChatResponse> {
        const request: RAGChatRequest = {
            message,
            sessionId: this.sessionId$.value || undefined
        };

        let url = `${this.apiUrl}/ask`;
        if (userId) {
            url += `?userId=${userId}`;
        }

        return this.http.post<RAGChatResponse>(url, request).pipe(
            tap(response => {
                if (response.sessionId) {
                    this.sessionId$.next(response.sessionId);
                    localStorage.setItem('rag_chat_session', response.sessionId);
                }
            }),
            map(response => {
                // Fix image URLs to use backend server
                if (response.products) {
                    response.products = response.products.map(product => ({
                        ...product,
                        imageUrl: product.imageUrl ? this.getFullImageUrl(product.imageUrl) : undefined
                    }));
                }
                return response;
            })
        );
    }

    /**
     * Get full image URL with backend base URL
     */
    private getFullImageUrl(imageUrl: string): string {
        if (!imageUrl) return '';
        // If already absolute URL, return as is
        if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
            return imageUrl;
        }
        // Prepend backend URL for relative paths
        return `${environment.apiUrl}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`;
    }

    /**
     * Create new chat session
     */
    createSession(userId?: string): Observable<string> {
        let url = `${this.apiUrl}/session`;
        if (userId) {
            url += `?userId=${userId}`;
        }

        return this.http.post(url, {}, { responseType: 'text' }).pipe(
            tap(sessionId => {
                this.sessionId$.next(sessionId);
                localStorage.setItem('rag_chat_session', sessionId);
            })
        );
    }

    /**
     * Clear current session
     */
    clearSession(): void {
        this.sessionId$.next(null);
        localStorage.removeItem('rag_chat_session');
    }

    /**
     * Get embedding status
     */
    getEmbeddingStatus(): Observable<EmbeddingStatus> {
        return this.http.get<EmbeddingStatus>(`${this.embeddingUrl}/status`);
    }

    /**
     * Rebuild all embeddings (admin only)
     */
    rebuildEmbeddings(): Observable<EmbeddingRebuildResponse> {
        return this.http.post<EmbeddingRebuildResponse>(`${this.embeddingUrl}/rebuild`, {});
    }

    /**
     * Format price to Vietnamese currency
     */
    formatPrice(price: number): string {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    }

    /**
     * Report an incorrect answer
     */
    reportIncorrectAnswer(reportData: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/report`, reportData);
    }
}
