import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { Product } from '../../core/models';
import { OfferSheetComponent } from '../offers/offer-sheet.component';
import { MoneyPipe } from '../../shared/ui/money.pipe';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, OfferSheetComponent, MoneyPipe],
  template: `
    <div *ngIf="product(); else notFound" class="space-y-10">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-xs text-muted-foreground">
        <a routerLink="/" class="hover:text-foreground transition">{{ i18n.t('nav.home') }}</a>
        <span>/</span>
        <a routerLink="/search" class="hover:text-foreground transition">{{ i18n.t('nav.catalog') }}</a>
        <span>/</span>
        <span class="text-foreground font-medium truncate max-w-xs">{{ product()?.name }}</span>
      </nav>

      <!-- Main Product Hero Section -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">
        <!-- 4:5 Real Photography Image Showcase (5 cols) -->
        <div class="lg:col-span-5 space-y-4">
          <div class="relative aspect-4/5 rounded-2xl overflow-hidden bg-muted border border-border shadow-sm group">
            <img 
              [src]="product()?.imageUrl" 
              [alt]="product()?.name"
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            />
            <!-- Badges -->
            <div class="absolute top-4 start-4 flex flex-col gap-2">
              <span *ngIf="product()?.acceptsOffers" class="px-3 py-1 bg-haggle text-haggle-foreground text-xs font-semibold rounded-full shadow-sm flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-amber-900 animate-pulse"></span>
                {{ i18n.t('product.accepts_offers') }}
              </span>
              <span class="px-3 py-1 bg-card/90 backdrop-blur text-foreground text-xs font-medium rounded-full border border-border shadow-sm">
                {{ product()?.governorate || 'تونس' }}
              </span>
            </div>

            <div class="absolute bottom-4 end-4">
              <span class="px-3 py-1 bg-emerald-900/80 text-white text-xs font-medium rounded-full backdrop-blur">
                {{ i18n.t('product.cod_badge') }}
              </span>
            </div>
          </div>

          <!-- Small Thumbnails -->
          <div class="grid grid-cols-4 gap-3">
            <div class="aspect-4/5 rounded-xl overflow-hidden border-2 border-primary cursor-pointer">
              <img [src]="product()?.imageUrl" class="w-full h-full object-cover" />
            </div>
          </div>
        </div>

        <!-- Product Purchase & Details (7 cols) -->
        <div class="lg:col-span-7 space-y-6">
          <!-- Title & Terroir Location -->
          <div class="space-y-2">
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-info uppercase tracking-wider">
                منتوج أصيل من {{ product()?.governorate }}
              </span>
              <span class="text-muted-foreground">•</span>
              <span class="text-xs text-muted-foreground font-mono">
                {{ product()?.skuCode }}
              </span>
            </div>
            <h1 class="font-serif text-2xl sm:text-3xl lg:text-4xl font-bold text-foreground leading-tight">
              {{ product()?.name }}
            </h1>
          </div>

          <!-- Price & Stock Indicator -->
          <div class="flex items-baseline gap-4 pb-4 border-b border-border">
            <div class="font-mono text-3xl sm:text-4xl font-black text-foreground tabular-nums">
              {{ product()?.price | money }}
            </div>
            <div class="flex items-center gap-2 text-xs font-medium text-success">
              <span class="w-2 h-2 rounded-full bg-success"></span>
              {{ i18n.t('product.in_stock') }} ({{ product()?.availableStock }} {{ i18n.t('product.unit') }})
            </div>
          </div>

          <!-- Description -->
          <p class="text-foreground/80 text-sm sm:text-base leading-relaxed">
            {{ product()?.description }}
          </p>

          <!-- Delivery Estimate Simulator -->
          <div class="bg-muted/50 border border-border rounded-xl p-4 space-y-3">
            <div class="flex items-center justify-between text-xs">
              <span class="font-semibold text-foreground flex items-center gap-2">
                <svg class="w-4 h-4 text-info" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0" />
                </svg>
                تقدير مدة التوصيل السريع (Rapide-Poste)
              </span>
              <span class="text-primary font-mono font-bold">7.000 د.ت</span>
            </div>

            <div class="flex items-center gap-3">
              <select 
                [value]="selectedGov()"
                (change)="onGovChange($event)"
                class="bg-card border border-border rounded-lg px-3 py-1.5 text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
              >
                <option value="تونس">تونس الكبرى (24 ساعة)</option>
                <option value="صفاقس">صفاقس (24 ساعة)</option>
                <option value="سوسة">سوسة والساحل (24 ساعة)</option>
                <option value="نابل">نابل والوطن القبلي (24 ساعة)</option>
                <option value="توزر">توزر والجنوب (48 ساعة)</option>
                <option value="جربة">جربة ومدنين (48 ساعة)</option>
              </select>
              <span class="text-xs text-muted-foreground">
                التسليم حتى باب المنزل والدفع عند الاستلام.
              </span>
            </div>
          </div>

          <!-- Quantity Stepper & Call to Actions -->
          <div class="space-y-4 pt-2">
            <div class="flex items-center gap-4">
              <span class="text-xs font-semibold text-muted-foreground">الكمية:</span>
              <div class="flex items-center border border-border rounded-xl bg-card overflow-hidden">
                <button 
                  (click)="decrementQty()"
                  [disabled]="quantity() <= 1"
                  class="px-3 py-1.5 text-foreground hover:bg-muted transition disabled:opacity-30"
                >
                  -
                </button>
                <span class="px-4 py-1.5 text-sm font-mono font-bold text-foreground">
                  {{ quantity() }}
                </span>
                <button 
                  (click)="incrementQty()"
                  [disabled]="quantity() >= (product()?.availableStock || 10)"
                  class="px-3 py-1.5 text-foreground hover:bg-muted transition disabled:opacity-30"
                >
                  +
                </button>
              </div>
            </div>

            <!-- Two Main Action Buttons -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <!-- Make an offer (Haggle) -->
              <button
                *ngIf="product()?.acceptsOffers"
                (click)="openHaggleSheet()"
                class="w-full py-3.5 px-4 bg-haggle hover:bg-haggle/90 text-haggle-foreground font-semibold rounded-xl transition flex items-center justify-center gap-2 shadow-sm text-sm"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
                {{ i18n.t('product.make_offer') }}
              </button>

              <!-- Add to Cart Directly -->
              <button
                (click)="onAddToCart()"
                class="w-full py-3.5 px-4 bg-primary hover:bg-primary/90 text-white font-semibold rounded-xl transition flex items-center justify-center gap-2 shadow-sm text-sm"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
                {{ i18n.t('product.add_to_cart') }}
              </button>
            </div>
          </div>

          <!-- Vendor Card -->
          <div class="bg-card border border-border rounded-xl p-4 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold">
                {{ product()?.name?.charAt(0) || 'ح' }}
              </div>
              <div>
                <h4 class="text-sm font-semibold text-foreground">
                  تعاونية خيرات {{ product()?.governorate }} الحرفية
                </h4>
                <div class="flex items-center gap-2 text-xs text-muted-foreground">
                  <span class="text-success font-medium">✓ تاجر موثق وخاضع للضريبة</span>
                  <span>•</span>
                  <span>يرد خلال أقل من ساعتين</span>
                </div>
              </div>
            </div>
            <a routerLink="/vendor" class="text-xs text-info hover:underline font-medium">
              زيارة المتجر ←
            </a>
          </div>
        </div>
      </div>

      <!-- Haggle Bottom / Side Sheet -->
      <app-offer-sheet
        [product]="product()"
        [isOpen]="isOfferSheetOpen()"
        (closeSheet)="isOfferSheetOpen.set(false)"
      />
    </div>

    <!-- 404 Not Found Template -->
    <ng-template #notFound>
      <div class="bg-card border border-border rounded-2xl p-12 text-center space-y-4 max-w-lg mx-auto">
        <h2 class="text-xl font-bold text-foreground">المنتوج غير متوفر</h2>
        <p class="text-sm text-muted-foreground">لم يتم العثور على المنتوج المطلوب في السوق الحالي.</p>
        <a routerLink="/search" class="inline-block px-5 py-2.5 bg-primary text-white text-xs font-semibold rounded-xl">
          العودة لدليل المنتوجات
        </a>
      </div>
    </ng-template>
  `
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly api = inject(ApiService);
  readonly i18n = inject(I18nService);

  product = signal<Product | null>(null);
  quantity = signal<number>(1);
  selectedGov = signal<string>('تونس');
  isOfferSheetOpen = signal<boolean>(false);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.api.getProductBySlug(slug).subscribe(p => {
          this.product.set(p || null);
        });
      }
    });
  }

  incrementQty() {
    this.quantity.update(q => q + 1);
  }

  decrementQty() {
    this.quantity.update(q => Math.max(1, q - 1));
  }

  onGovChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.selectedGov.set(select.value);
  }

  openHaggleSheet() {
    this.isOfferSheetOpen.set(true);
  }

  onAddToCart() {
    const p = this.product();
    if (!p) return;
    this.api.addToCart(p.skuId, this.quantity()).subscribe(() => {
      this.router.navigate(['/cart']);
    });
  }
}
