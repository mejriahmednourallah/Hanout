import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { Order, VendorSubOrder } from '../../core/models';
import { MoneyPipe } from '../../shared/ui/money.pipe';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, MoneyPipe],
  template: `
    <div class="max-w-4xl mx-auto space-y-8">
      <!-- Header -->
      <div class="pb-4 border-b border-border">
        <h1 class="font-serif text-3xl font-bold text-foreground">
          {{ i18n.t('orders.title') }}
        </h1>
        <p class="text-sm text-muted-foreground mt-1">
          متابعة مباشرة للشحنات والتوصيل عبر شركات البريد التونسي (Rapide-Poste) والفواتير الرسمية
        </p>
      </div>

      <!-- Orders List -->
      <div *ngIf="api.orders().length > 0; else noOrders" class="space-y-6">
        <div 
          *ngFor="let order of api.orders()"
          class="bg-card border border-border rounded-2xl p-6 space-y-6 shadow-sm"
        >
          <!-- Order Summary Header -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-border gap-4">
            <div class="space-y-1">
              <div class="flex items-center gap-3">
                <span class="text-base font-bold text-foreground font-mono">
                  طلب #{{ order.id }}
                </span>
                <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold"
                  [ngClass]="{
                    'bg-amber-100 text-amber-800 border border-amber-200': order.status === 'PLACED',
                    'bg-blue-100 text-blue-800 border border-blue-200': order.status === 'CONFIRMED' || order.status === 'SHIPPED',
                    'bg-emerald-100 text-emerald-800 border border-emerald-200': order.status === 'DELIVERED'
                  }"
                >
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <p class="text-xs text-muted-foreground">
                تاريخ الطلب: {{ order.createdAt | date:'mediumDate' }} • وجهة التسليم: {{ order.shippingAddress }}
              </p>
            </div>

            <div class="text-start sm:text-end">
              <div class="text-xs text-muted-foreground">المبلغ الجملي (COD)</div>
              <div class="font-mono text-xl font-black text-foreground tabular-nums">
                {{ order.totalAmount | money }}
              </div>
            </div>
          </div>

          <!-- Vendor Sub-Orders Section (One per Vendor) -->
          <div class="space-y-4">
            <h4 class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              الشحنات المنفصلة حسب الحرفيين والتجار ({{ order.subOrders?.length || 1 }})
            </h4>

            <div 
              *ngFor="let sub of order.subOrders; let i = index"
              class="bg-muted/40 border border-border rounded-xl p-4 space-y-4"
            >
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div class="flex items-center gap-2">
                  <span class="w-6 h-6 rounded-full bg-primary/10 text-primary text-xs font-bold flex items-center justify-center">
                    {{ i + 1 }}
                  </span>
                  <div>
                    <h5 class="text-sm font-semibold text-foreground">
                      {{ getVendorName(sub.vendorId) }}
                    </h5>
                    <p class="text-xs text-muted-foreground">
                      المجموع الفرعي للبائع: <span class="font-mono font-medium">{{ sub.subTotal | money }}</span>
                    </p>
                  </div>
                </div>

                <!-- Tracking Pill & Invoice Button -->
                <div class="flex items-center gap-3">
                  <span *ngIf="sub.trackingNo" class="px-2.5 py-1 bg-card border border-border rounded-lg text-xs font-mono text-info flex items-center gap-1.5 shadow-xs">
                    <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                    {{ sub.trackingNo }}
                  </span>

                  <button 
                    (click)="downloadInvoice(order.id, sub.id)"
                    class="px-3 py-1 bg-card hover:bg-muted border border-border text-foreground text-xs font-medium rounded-lg transition flex items-center gap-1.5 shadow-xs"
                  >
                    <svg class="w-3.5 h-3.5 text-muted-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                    {{ i18n.t('orders.invoice') }}
                  </button>
                </div>
              </div>

              <!-- Tracking Timeline Progression -->
              <div class="pt-2">
                <div class="grid grid-cols-4 gap-2 text-center">
                  <!-- Step 1: Placed -->
                  <div class="space-y-1">
                    <div class="w-6 h-6 rounded-full mx-auto flex items-center justify-center text-xs font-bold"
                      [ngClass]="getStepClass(sub.status, 1)">
                      ✓
                    </div>
                    <span class="text-[11px] font-medium block text-foreground">تم التسجيل</span>
                  </div>

                  <!-- Step 2: Confirmed -->
                  <div class="space-y-1">
                    <div class="w-6 h-6 rounded-full mx-auto flex items-center justify-center text-xs font-bold"
                      [ngClass]="getStepClass(sub.status, 2)">
                      {{ getStepNumber(sub.status, 2) }}
                    </div>
                    <span class="text-[11px] font-medium block text-foreground">تأكيد الحرفي</span>
                  </div>

                  <!-- Step 3: Shipped -->
                  <div class="space-y-1">
                    <div class="w-6 h-6 rounded-full mx-auto flex items-center justify-center text-xs font-bold"
                      [ngClass]="getStepClass(sub.status, 3)">
                      {{ getStepNumber(sub.status, 3) }}
                    </div>
                    <span class="text-[11px] font-medium block text-foreground">مع الشاحن</span>
                  </div>

                  <!-- Step 4: Delivered -->
                  <div class="space-y-1">
                    <div class="w-6 h-6 rounded-full mx-auto flex items-center justify-center text-xs font-bold"
                      [ngClass]="getStepClass(sub.status, 4)">
                      {{ getStepNumber(sub.status, 4) }}
                    </div>
                    <span class="text-[11px] font-medium block text-foreground">تم التسليم والتحصيل</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <ng-template #noOrders>
        <div class="bg-card border border-border rounded-2xl p-12 text-center space-y-4 max-w-md mx-auto">
          <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mx-auto text-muted-foreground">
            <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
          </div>
          <h3 class="text-base font-semibold text-foreground">
            {{ i18n.t('orders.empty') }}
          </h3>
          <p class="text-xs text-muted-foreground">
            تصفح منتوجاتنا، ساوم على السعر، وتابع شحنتك خطوة بخطوة حتى باب منزلك.
          </p>
          <a routerLink="/search" class="inline-block px-5 py-2.5 bg-primary text-white text-xs font-semibold rounded-xl">
            تصفح السوق الآن
          </a>
        </div>
      </ng-template>

      <!-- Simulated Invoice Modal -->
      <div *ngIf="activeInvoiceModal()" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
        <div class="bg-card border border-border rounded-2xl max-w-md w-full p-6 space-y-4 shadow-xl">
          <div class="flex items-center justify-between border-b border-border pb-3">
            <h3 class="font-serif font-bold text-foreground text-lg">
              فاتورة قانونية تونسية (Facture Légale)
            </h3>
            <button (click)="activeInvoiceModal.set(null)" class="text-muted-foreground hover:text-foreground">✕</button>
          </div>

          <div class="space-y-3 text-xs text-foreground font-mono bg-muted/30 p-4 rounded-xl border border-border">
            <div class="flex justify-between">
              <span class="text-muted-foreground">رقم الفاتورة:</span>
              <span class="font-bold">FAC-2026-0042</span>
            </div>
            <div class="flex justify-between">
              <span class="text-muted-foreground">المعرف الجبائي (MF):</span>
              <span class="font-bold">1234567/A/M/000</span>
            </div>
            <div class="flex justify-between">
              <span class="text-muted-foreground">المبلغ الخام دون أداء (HT):</span>
              <span>105.042 د.ت</span>
            </div>
            <div class="flex justify-between">
              <span class="text-muted-foreground">الأداء على القيمة المضافة (TVA 19%):</span>
              <span>19.958 د.ت</span>
            </div>
            <div class="flex justify-between">
              <span class="text-muted-foreground">معلوم الطابع الجبائي (Timbre fiscal):</span>
              <span>1.000 د.ت</span>
            </div>
            <div class="border-t border-border pt-2 flex justify-between font-bold text-sm text-primary">
              <span>المبلغ الإجمالي الصافي (TTC):</span>
              <span>126.000 د.ت</span>
            </div>
          </div>

          <p class="text-[11px] text-muted-foreground text-center">
            فاتورة رقمية رسمية ومطابقة لمنظومة الفوترة الإلكترونية للجمهورية التونسية.
          </p>

          <button 
            (click)="activeInvoiceModal.set(null)"
            class="w-full py-2 bg-primary text-white text-xs font-semibold rounded-xl"
          >
            إغلاق المعاينة
          </button>
        </div>
      </div>
    </div>
  `
})
export class OrdersComponent implements OnInit {
  readonly api = inject(ApiService);
  readonly i18n = inject(I18nService);

  activeInvoiceModal = signal<any>(null);

  ngOnInit() {
    this.api.getOrders().subscribe();
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'PLACED': return 'قيد المعالجة';
      case 'CONFIRMED': return 'مؤكد من التاجر';
      case 'SHIPPED': return 'في الطريق مع الشاحن';
      case 'DELIVERED': return 'تم التسليم بنجاح';
      default: return status;
    }
  }

  getVendorName(vendorId: number): string {
    if (vendorId === 1) return 'معصرة صفاقس للزيت الحيوي';
    if (vendorId === 2) return 'دار الفخار التقليدي بقلالة جربة';
    return `بائع معتمد #${vendorId}`;
  }

  getStepClass(status: string, step: number): string {
    const level = this.getStatusLevel(status);
    if (level >= step) {
      return 'bg-emerald-600 text-white';
    }
    return 'bg-muted text-muted-foreground border border-border';
  }

  getStepNumber(status: string, step: number): string {
    const level = this.getStatusLevel(status);
    return level >= step ? '✓' : `${step}`;
  }

  private getStatusLevel(status: string): number {
    switch (status) {
      case 'PLACED': return 1;
      case 'CONFIRMED': return 2;
      case 'SHIPPED': return 3;
      case 'DELIVERED': return 4;
      default: return 1;
    }
  }

  downloadInvoice(orderId: number, subOrderId: number) {
    this.activeInvoiceModal.set({ orderId, subOrderId });
  }
}
