import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
// RouterOutlet was unused in this template

@Component({
  standalone: true,
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent {
[x: string]: any;
  stats = [
    { title: 'Revenue', value: '₫128,4M', change: '+12.3%' },
    { title: 'Orders', value: '1,284', change: '+5.1%' },
    { title: 'Customers', value: '856', change: '+3.7%' },
    { title: 'Refunds', value: '12', change: '-0.8%' },
  ];
}
