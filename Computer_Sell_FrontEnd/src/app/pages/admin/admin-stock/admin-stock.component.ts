import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService, StockHistoryResponse, StockMovementType } from '../../../services/stock.service';
import { ProductService } from '../../../services/product.service';

@Component({
    standalone: true,
    selector: 'app-admin-stock',
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-stock.component.html',
    styleUrls: ['./admin-stock.component.scss']
})
export class AdminStockComponent implements OnInit {
    loading = false;
    error: string | null = null;
    successMessage: string | null = null;

    // Stock history
    stockHistory: StockHistoryResponse[] = [];

    // Filter options
    filterType: StockMovementType | '' = '';
    filterProductId: string = '';
    filterStartDate: string = '';
    filterEndDate: string = '';

    // Products for dropdown
    products: any[] = [];

    // Stock adjustment
    adjustProductId: string = '';
    adjustNewStock: number = 0;
    adjustNote: string = '';
    showAdjustModal = false;
    currentStock: number | null = null;

    constructor(
        private stockService: StockService,
        private productService: ProductService
    ) { }

    ngOnInit(): void {
        this.loadProducts();
        this.loadAllHistory();
    }

    loadProducts(): void {
        this.productService.listProducts({ size: 1000 }).subscribe({
            next: (res) => {
                this.products = res?.data?.items || [];
            },
            error: () => { }
        });
    }

    loadAllHistory(): void {
        // Load IMPORT history by default
        this.filterByType('IMPORT');
    }

    filterByType(type?: StockMovementType): void {
        const t = type || this.filterType as StockMovementType;
        if (!t) return;

        this.loading = true;
        this.error = null;
        this.stockService.getHistoryByType(t).subscribe({
            next: (res) => {
                this.stockHistory = res?.data || [];
                this.loading = false;
            },
            error: (err) => {
                this.error = err?.error?.message || 'Lỗi khi tải lịch sử kho';
                this.loading = false;
            }
        });
    }

    filterByProduct(): void {
        if (!this.filterProductId) return;

        this.loading = true;
        this.error = null;
        this.stockService.getHistoryByProduct(this.filterProductId).subscribe({
            next: (res) => {
                this.stockHistory = res?.data || [];
                this.loading = false;
            },
            error: (err) => {
                this.error = err?.error?.message || 'Lỗi khi tải lịch sử kho';
                this.loading = false;
            }
        });
    }

    filterByDateRange(): void {
        if (!this.filterStartDate || !this.filterEndDate) {
            this.error = 'Vui lòng chọn ngày bắt đầu và kết thúc';
            return;
        }

        this.loading = true;
        this.error = null;
        const start = new Date(this.filterStartDate).toISOString();
        const end = new Date(this.filterEndDate).toISOString();

        this.stockService.getHistoryByDateRange(start, end).subscribe({
            next: (res) => {
                this.stockHistory = res?.data || [];
                this.loading = false;
            },
            error: (err) => {
                this.error = err?.error?.message || 'Lỗi khi tải lịch sử kho';
                this.loading = false;
            }
        });
    }

    clearFilters(): void {
        this.filterType = '';
        this.filterProductId = '';
        this.filterStartDate = '';
        this.filterEndDate = '';
        this.loadAllHistory();
    }

    // Stock adjustment
    openAdjustModal(): void {
        this.showAdjustModal = true;
        this.adjustProductId = '';
        this.adjustNewStock = 0;
        this.adjustNote = '';
        this.currentStock = null;
    }

    closeAdjustModal(): void {
        this.showAdjustModal = false;
    }

    onProductSelect(): void {
        if (!this.adjustProductId) {
            this.currentStock = null;
            return;
        }
        this.stockService.checkStock(this.adjustProductId).subscribe({
            next: (res) => {
                this.currentStock = res?.data ?? 0;
                this.adjustNewStock = this.currentStock;
            },
            error: () => {
                this.currentStock = 0;
            }
        });
    }

    submitAdjustment(): void {
        if (!this.adjustProductId) {
            this.error = 'Vui lòng chọn sản phẩm';
            return;
        }
        if (this.adjustNewStock < 0) {
            this.error = 'Số lượng tồn kho không thể âm';
            return;
        }

        this.stockService.adjustStock({
            productId: this.adjustProductId,
            newStock: this.adjustNewStock,
            note: this.adjustNote || 'Điều chỉnh kho từ Admin',
            createdBy: 'admin'
        }).subscribe({
            next: () => {
                this.successMessage = 'Điều chỉnh tồn kho thành công';
                this.closeAdjustModal();
                this.loadAllHistory();
                setTimeout(() => this.successMessage = null, 3000);
            },
            error: (err) => {
                this.error = err?.error?.message || 'Lỗi khi điều chỉnh tồn kho';
            }
        });
    }

    getMovementTypeClass(type: string): string {
        switch (type) {
            case 'IMPORT': return 'bg-green-100 text-green-800';
            case 'EXPORT': return 'bg-blue-100 text-blue-800';
            case 'RETURN': return 'bg-yellow-100 text-yellow-800';
            case 'ADJUSTMENT': return 'bg-purple-100 text-purple-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    }

    getMovementTypeText(type: string): string {
        switch (type) {
            case 'IMPORT': return 'Nhập kho';
            case 'EXPORT': return 'Xuất kho';
            case 'RETURN': return 'Hoàn kho';
            case 'ADJUSTMENT': return 'Điều chỉnh';
            default: return type;
        }
    }
}
