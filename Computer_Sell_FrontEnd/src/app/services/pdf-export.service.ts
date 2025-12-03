import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { ImportReceiptResponse } from './import-receipt.service';
import { NOTO_SANS_FONT_BASE64 } from './noto-sans-font';

@Injectable({ providedIn: 'root' })
export class PdfExportService {

    private setupVietnameseFont(doc: jsPDF): void {
        // Thêm font Noto Sans vào jsPDF - hỗ trợ tiếng Việt đầy đủ
        doc.addFileToVFS('NotoSans-Regular.ttf', NOTO_SANS_FONT_BASE64);
        doc.addFont('NotoSans-Regular.ttf', 'NotoSans', 'normal');
        doc.setFont('NotoSans');
    }

    private formatCurrency(amount: number): string {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    }

    private formatDate(dateStr: string): string {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }

    private getStatusText(status: string): string {
        switch (status) {
            case 'COMPLETED': return 'Hoàn thành';
            case 'PENDING': return 'Đang xử lý';
            case 'CANCELLED': return 'Đã hủy';
            default: return status;
        }
    }

    // Xuất phiếu nhập kho ra PDF
    async exportImportReceipt(receipt: ImportReceiptResponse): Promise<void> {
        const doc = new jsPDF();

        // Setup font tiếng Việt
        this.setupVietnameseFont(doc);

        // Header - Tên công ty
        doc.setFontSize(18);
        doc.text('THComputer', 105, 20, { align: 'center' });

        doc.setFontSize(10);
        doc.text('Địa chỉ: 3/2 Xuân Khánh, Ninh Kiều, Cần Thơ', 105, 27, { align: 'center' });
        doc.text('Hotline: 0987 654 321 | Email: contact@thcomputer.vn', 105, 32, { align: 'center' });

        // Đường kẻ
        doc.setLineWidth(0.5);
        doc.line(15, 38, 195, 38);

        // Tiêu đề phiếu
        doc.setFontSize(16);
        doc.text('PHIẾU NHẬP KHO', 105, 48, { align: 'center' });

        // Mã phiếu
        doc.setFontSize(11);
        doc.text(`Mã phiếu: ${receipt.receiptCode || receipt.receiptId}`, 105, 55, { align: 'center' });

        // Thông tin phiếu
        const startY = 65;
        doc.setFontSize(10);

        // Cột trái
        doc.text(`Ngày nhập: ${this.formatDate(receipt.receiptDate || receipt.createdAt)}`, 15, startY);
        doc.text(`Nhân viên: ${receipt.employeeName || 'N/A'}`, 15, startY + 7);
        doc.text(`Ghi chú: ${receipt.note || 'Không có'}`, 15, startY + 14);

        // Cột phải
        doc.text(`Trạng thái: ${this.getStatusText(receipt.status)}`, 120, startY);

        // Bảng chi tiết sản phẩm
        const tableData = receipt.details.map((item, index) => [
            (index + 1).toString(),
            item.productName || 'N/A',
            item.quantity.toString(),
            this.formatCurrency(item.importPrice),
            this.formatCurrency(item.quantity * item.importPrice)
        ]);

        autoTable(doc, {
            startY: startY + 25,
            head: [['STT', 'Tên sản phẩm', 'Số lượng', 'Giá nhập', 'Thành tiền']],
            body: tableData,
            theme: 'grid',
            headStyles: {
                fillColor: [59, 130, 246],
                textColor: 255,
                fontStyle: 'normal',
                halign: 'center',
                font: 'NotoSans'
            },
            columnStyles: {
                0: { halign: 'center', cellWidth: 15 },
                1: { halign: 'left', cellWidth: 70 },
                2: { halign: 'center', cellWidth: 25 },
                3: { halign: 'right', cellWidth: 35 },
                4: { halign: 'right', cellWidth: 35 }
            },
            styles: {
                fontSize: 9,
                cellPadding: 3,
                font: 'NotoSans'
            },
            bodyStyles: {
                font: 'NotoSans'
            },
            foot: [[
                '', '', '', 'TỔNG CỘNG:',
                this.formatCurrency(receipt.totalAmount)
            ]],
            footStyles: {
                fillColor: [243, 244, 246],
                textColor: 0,
                fontStyle: 'normal',
                font: 'NotoSans'
            }
        });

        // Lấy vị trí Y sau bảng
        const finalY = (doc as any).lastAutoTable.finalY + 20;

        // Chữ ký
        doc.setFontSize(10);
        doc.text('Người lập phiếu', 40, finalY, { align: 'center' });
        doc.text('Người duyệt', 160, finalY, { align: 'center' });

        doc.setFontSize(8);
        doc.text('(Ký, ghi rõ họ tên)', 40, finalY + 5, { align: 'center' });
        doc.text('(Ký, ghi rõ họ tên)', 160, finalY + 5, { align: 'center' });

        // Footer
        doc.setFontSize(8);
        doc.setTextColor(128);
        doc.text(`In ngày: ${new Date().toLocaleDateString('vi-VN')} ${new Date().toLocaleTimeString('vi-VN')}`, 105, 285, { align: 'center' });

        // Lưu file
        doc.save(`Phieu_Nhap_${receipt.receiptCode || receipt.receiptId}.pdf`);
    }
}
