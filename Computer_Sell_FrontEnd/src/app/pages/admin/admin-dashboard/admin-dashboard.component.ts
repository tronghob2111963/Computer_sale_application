import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DashboardService, DashboardStats } from '../../../services/dashboard.service';

type PeriodType = 'today' | 'week' | 'month' | 'year';

@Component({
  standalone: true,
  selector: 'app-admin-dashboard',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  // Filter
  periods: PeriodType[] = ['today', 'week', 'month', 'year'];
  selectedPeriod: PeriodType = 'month';
  selectedYear: number = new Date().getFullYear();
  years: number[] = [];

  // Stats
  stats: DashboardStats = {
    totalRevenue: 0,
    todayRevenue: 0,
    weekRevenue: 0,
    monthRevenue: 0,
    totalOrders: 0,
    todayOrders: 0,
    pendingOrders: 0,
    confirmedOrders: 0,
    shippingOrders: 0,
    completedOrders: 0,
    cancelledOrders: 0,
    totalCustomers: 0,
    newCustomersToday: 0,
    newCustomersWeek: 0,
    newCustomersMonth: 0,
    revenueByMethod: {},
    monthlyRevenue: Array(12).fill(0),
    dailyRevenue: []
  };

  // Monthly revenue chart data
  monthLabels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];

  // Revenue by payment method
  revenueByMethod: { method: string; amount: number; percentage: number }[] = [];

  // Loading states
  loading = true;

  constructor(private dashboardService: DashboardService) {
    // Generate years for dropdown
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y >= currentYear - 5; y--) {
      this.years.push(y);
    }
  }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.dashboardService.getStatsByYear(this.selectedYear).subscribe({
      next: (res) => {
        if (res.data) {
          this.stats = res.data;

          // Calculate revenue by method percentages
          if (res.data.revenueByMethod) {
            const total = Object.values(res.data.revenueByMethod).reduce((a, b) => a + b, 0);
            this.revenueByMethod = Object.entries(res.data.revenueByMethod).map(([method, amount]) => ({
              method: this.formatPaymentMethod(method),
              amount: amount as number,
              percentage: total > 0 ? Math.round(((amount as number) / total) * 100) : 0
            }));
          }
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.loading = false;
      }
    });
  }

  onYearChange(): void {
    this.loadDashboardData();
  }

  onPeriodChange(): void {
    // Period change logic - could filter displayed data
  }

  selectPeriod(period: PeriodType): void {
    this.selectedPeriod = period;
    this.onPeriodChange();
  }

  getPeriodLabel(period: PeriodType): string {
    const labels: Record<PeriodType, string> = {
      'today': 'Hôm nay',
      'week': 'Tuần',
      'month': 'Tháng',
      'year': 'Năm'
    };
    return labels[period];
  }

  getDisplayRevenue(): number {
    switch (this.selectedPeriod) {
      case 'today': return this.stats.todayRevenue;
      case 'week': return this.stats.weekRevenue;
      case 'month': return this.stats.monthRevenue;
      case 'year': return this.stats.totalRevenue;
      default: return this.stats.totalRevenue;
    }
  }

  formatCurrency(value: number): string {
    if (!value) return '0';
    if (value >= 1000000000) {
      return (value / 1000000000).toFixed(1) + ' tỷ';
    } else if (value >= 1000000) {
      return (value / 1000000).toFixed(1) + ' triệu';
    } else if (value >= 1000) {
      return (value / 1000).toFixed(0) + 'K';
    }
    return value.toLocaleString('vi-VN');
  }

  formatPaymentMethod(method: string): string {
    const methods: Record<string, string> = {
      'VNPAY': 'VNPay',
      'VIETQR': 'VietQR',
      'CASH': 'Tiền mặt',
      'COD': 'COD',
      'BANK_TRANSFER': 'Chuyển khoản'
    };
    return methods[method] || method;
  }

  getMaxRevenue(): number {
    return Math.max(...(this.stats.monthlyRevenue || [0]), 1);
  }

  getBarHeight(value: number): number {
    const max = this.getMaxRevenue();
    return max > 0 ? (value / max) * 100 : 0;
  }

  getMethodColor(method: string): string {
    const colors: Record<string, string> = {
      'VNPay': 'bg-blue-500',
      'VietQR': 'bg-green-500',
      'Tiền mặt': 'bg-yellow-500',
      'COD': 'bg-orange-500',
      'Chuyển khoản': 'bg-purple-500'
    };
    return colors[method] || 'bg-gray-500';
  }

  getTotalYearRevenue(): number {
    return (this.stats.monthlyRevenue || []).reduce((a, b) => a + b, 0);
  }

  getProcessingOrders(): number {
    return this.stats.confirmedOrders + this.stats.shippingOrders;
  }

  hasDailyRevenue(): boolean {
    return !!(this.stats.dailyRevenue && this.stats.dailyRevenue.length > 0);
  }
}
