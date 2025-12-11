import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../enviroment';

export interface ChatMessage {
    message: string;
}

export interface ChatResponse {
    message: string;
    timestamp: string;
}

@Injectable({
    providedIn: 'root'
})
export class ChatbotService {
    private apiUrl = `${environment.apiUrl}/api/chatbot`;

    constructor(private http: HttpClient) { }

    /**
     * Send a message to the chatbot
     * @param message User message
     * @param userId User ID (UUID string)
     * @returns Observable of ChatResponse
     */
    sendMessage(message: string, userId: string): Observable<ChatResponse> {
        const body: ChatMessage = { message };
        const params = new HttpParams().set('userId', userId);

        return this.http.post<ChatResponse>(
            `${this.apiUrl}/chat`,
            body,
            { params }
        );
    }

    /**
     * Get product availability
     * @param productName Product name to search
     * @returns Observable of availability message
     */
    getProductAvailability(productName: string): Observable<string> {
        const params = new HttpParams().set('productName', productName);

        return this.http.get<string>(
            `${this.apiUrl}/product-availability`,
            { params }
        );
    }

    /**
     * Get product price
     * @param productName Product name to search
     * @returns Observable of price message
     */
    getProductPrice(productName: string): Observable<string> {
        const params = new HttpParams().set('productName', productName);

        return this.http.get<string>(
            `${this.apiUrl}/product-price`,
            { params }
        );
    }
}
