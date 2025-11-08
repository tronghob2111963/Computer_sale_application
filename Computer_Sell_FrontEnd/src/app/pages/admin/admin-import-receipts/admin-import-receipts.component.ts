import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ImportReceiptService, ResponseEnvelope } from '../../../services/import-receipt.service';

@Component({
  standalone: true,
  selector: 'app-admin-import-receipts',
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-import-receipts.component.html',
  styleUrls: ['./admin-import-receipts.component.scss']
})
export class AdminImportReceiptsComponent implements OnInit {
  loading = false;
  error: string | null = null;
  receipts: any[] = [];

  constructor(private svc: ImportReceiptService, private router: Router) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.svc.list().subscribe({
      next: (res: ResponseEnvelope<any[]>) => {
        this.receipts = Array.isArray(res?.data) ? res.data : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Không tải được danh sách phiếu nhập';
        this.loading = false;
      }
    });
  }

  create(): void {
    this.router.navigate(['/admin/import-receipts/create']);
  }

  view(r: any): void {
    const id = r?.id || r?.receiptId || r?.uuid;
    if (id) {
      this.router.navigate(['/admin/import-receipts', id]);
    }
  }
}

