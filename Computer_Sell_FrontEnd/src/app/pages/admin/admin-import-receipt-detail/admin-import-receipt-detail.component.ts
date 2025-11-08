import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ImportReceiptService, ResponseEnvelope } from '../../../services/import-receipt.service';

@Component({
  standalone: true,
  selector: 'app-admin-import-receipt-detail',
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-import-receipt-detail.component.html',
  styleUrls: ['./admin-import-receipt-detail.component.scss']
})
export class AdminImportReceiptDetailComponent implements OnInit {
  loading = false;
  error: string | null = null;
  data: any = null;

  constructor(private route: ActivatedRoute, private svc: ImportReceiptService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetch(id);
    }
  }

  fetch(id: string): void {
    this.loading = true;
    this.svc.getById(id).subscribe({
      next: (res: ResponseEnvelope<any>) => {
        this.data = res?.data ?? null;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Không tải được phiếu nhập';
        this.loading = false;
      }
    });
  }

  items(): any[] {
    const items = this.data?.items || this.data?.details || this.data?.receiptDetails;
    return Array.isArray(items) ? items : [];
  }
}

