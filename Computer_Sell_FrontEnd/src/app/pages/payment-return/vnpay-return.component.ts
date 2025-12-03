import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { PaymentDetail, PaymentService } from '../../services/payment.service';

type ViewState = 'pending' | 'success' | 'failed';

@Component({
  selector: 'app-vnpay-return',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './vnpay-return.component.html',
  styleUrls: ['./vnpay-return.component.scss']
})
export class VnpayReturnComponent implements OnInit {
  state: ViewState = 'pending';
  message = 'Dang kiem tra ket qua thanh toan...';
  paymentId = '';
  orderId = '';
  amount?: number;
  txnNo?: string;
  statusText = '';
  responseCode = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private title: Title
  ) {}

  ngOnInit(): void {
    this.title.setTitle('VNPay - THComputer');
    const params = this.route.snapshot.queryParamMap;
    this.paymentId = params.get('vnp_TxnRef') || params.get('paymentId') || '';
    this.responseCode = params.get('vnp_ResponseCode') || params.get('vnp_TransactionStatus') || '';
    this.txnNo = params.get('vnp_TransactionNo') || undefined;

    if (!this.paymentId) {
      this.state = 'failed';
      this.message = 'Khong tim thay ma thanh toan VNPay.';
      return;
    }

    this.loadPayment();
  }

  goToOrder(): void {
    if (this.orderId) {
      this.router.navigate(['/order', this.orderId]);
    } else {
      this.router.navigate(['/orders']);
    }
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  private loadPayment(): void {
    this.state = 'pending';
    this.paymentService.getPaymentDetail(this.paymentId).subscribe({
      next: (res) => {
        const payment = res?.data as PaymentDetail;
        this.orderId = payment?.orderId || '';
        this.amount = payment?.amount as number | undefined;
        const status = (payment?.paymentStatus || '').toUpperCase();
        this.statusText = payment?.paymentStatus || '';

        if (status === 'SUCCESS' || status === 'PAID') {
          this.state = 'success';
          this.message = 'Thanh toan thanh cong.';
        } else if (status === 'PENDING') {
          this.state = 'pending';
          this.message = 'Giao dich dang duoc xac thuc. Vui long doi trong giay lat.';
        } else {
          this.state = 'failed';
          this.message = 'Thanh toan khong thanh cong. Vui long thu lai.';
        }
      },
      error: (e) => {
        this.state = 'failed';
        this.message = e?.error?.message || 'Khong tra cuu duoc giao dich.';
      }
    });
  }
}
