import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { AdminLayoutComponent } from './pages/admin/admin-layout/admin-layout.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './pages/admin/admin-users/admin-users.component';
import { AdminOrdersComponent } from './pages/admin/admin-orders/admin-orders.component';
import { AdminShippingOrdersComponent } from './pages/admin/admin-shipping-orders/admin-shipping-orders.component';
import { AdminShippingOrderDetailComponent } from './pages/admin/admin-shipping-order-detail/admin-shipping-order-detail.component';
import { AdminProductsComponent } from './pages/admin/admin-products/admin-products.component';
import { AdminSettingsComponent } from './pages/admin/admin-settings/admin-settings.component';
import { AdminBrandsComponent } from './pages/admin/admin-brands/admin-brands.component';
import { AdminPaymentsComponent } from './pages/admin/admin-payments/admin-payments.component';
import { AdminPromotionsComponent } from './pages/admin/admin-promotions/admin-promotions.component';
import { AdminWarrantyComponent } from './pages/admin/admin-warranty/admin-warranty.component';
import { AdminFeedbackComponent } from './pages/admin/admin-feedback/admin-feedback.component';
import { AdminNotificationsComponent } from './pages/admin/admin-notifications/admin-notifications.component';
import { AdminLogsComponent } from './pages/admin/admin-logs/admin-logs.component';
import { AdminCategoriesComponent } from './pages/admin/admin-categories/admin-categories.component';
import { AdminEmployeesComponent } from './pages/admin/admin-employees/admin-employees.component';
import { AdminImportReceiptsComponent } from './pages/admin/admin-import-receipts/admin-import-receipts.component';
import { AdminImportReceiptCreateComponent } from './pages/admin/admin-import-receipt-create/admin-import-receipt-create.component';
import { AdminImportReceiptDetailComponent } from './pages/admin/admin-import-receipt-detail/admin-import-receipt-detail.component';
import { AdminStockComponent } from './pages/admin/admin-stock/admin-stock.component';
import { HomeComponent } from './pages/home/home.component';
import { ProductDetailComponent } from './pages/product-detail/product-detail.component';
import { CartComponent } from './pages/cart/cart.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { OrdersComponent } from './pages/orders/orders.component';
import { OrderDetailComponent } from './pages/order-detail/order-detail.component';
import { AdminCustommerComponent } from './pages/admin/admin-custommer/admin-custommer.component';
import { VnpayReturnComponent } from './pages/payment-return/vnpay-return.component';
import { PcBuilderComponent } from './pages/pc-builder/pc-builder.component';
import { MyBuildsComponent } from './pages/my-builds/my-builds.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { ProfileComponent } from './pages/profile/profile.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'cart', component: CartComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'orders', component: OrdersComponent },
  { path: 'order/:id', component: OrderDetailComponent },
  { path: 'payment/vnpay-return', component: VnpayReturnComponent },
  { path: 'pc-builder', component: PcBuilderComponent },
  { path: 'my-builds', component: MyBuildsComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'profile', component: ProfileComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'products', component: AdminProductsComponent },
      { path: 'categories', component: AdminCategoriesComponent },
      { path: 'brands', component: AdminBrandsComponent },
      { path: 'orders', component: AdminOrdersComponent },
      { path: 'shipping-orders', component: AdminShippingOrdersComponent },
      { path: 'shipping-orders/:id', component: AdminShippingOrderDetailComponent },
      { path: 'employees', component: AdminEmployeesComponent },
      { path: 'import-receipts', component: AdminImportReceiptsComponent },
      { path: 'import-receipts/create', component: AdminImportReceiptCreateComponent },
      { path: 'import-receipts/:id', component: AdminImportReceiptDetailComponent },
      { path: 'stock', component: AdminStockComponent },
      { path: 'payments', component: AdminPaymentsComponent },
      { path: 'promotions', component: AdminPromotionsComponent },
      { path: 'warranty', component: AdminWarrantyComponent },
      { path: 'feedback', component: AdminFeedbackComponent },
      { path: 'notifications', component: AdminNotificationsComponent },
      { path: 'logs', component: AdminLogsComponent },
      { path: 'settings', component: AdminSettingsComponent },
      { path: 'staffs', component: AdminEmployeesComponent },
      { path: 'customers', component: AdminCustommerComponent },
    ],
  },

  { path: '**', redirectTo: '' }
];
