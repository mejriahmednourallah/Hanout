import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { Product } from '../../core/models';
import { ProductCardComponent } from './product-card.component';
import { OfferSheetComponent } from '../offers/offer-sheet.component';
import { MoneyPipe } from '../../shared/ui/money.pipe';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ProductCardComponent, OfferSheetComponent, MoneyPipe],
  template: `
    <div class="space-y-6">
      <!-- Breadcrumb & Title -->
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 class="font-serif text-3xl font-bold text-foreground">
            {{ i18n.t('search.title') }}
          </h1>
          <p class="text-sm text-muted-foreground mt-1">
            {{ filteredProducts().length }} {{ i18n.t('search.results') }}
          </p>
        </div>

        <!-- Search Input Bar -->
        <div class="relative w-full md:w-96">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (ngModelChange)="onSearchChange()"
            [placeholder]="i18n.t('search.placeholder')"
            class="w-full ps-10 pe-4 py-2.5 bg-card border border-border rounded-xl text-foreground placeholder:text-muted-foreground/60 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition"
          />
          <svg class="absolute start-3 top-3 w-4 h-4 text-muted-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <!-- Main Layout: Sidebar Facets + Product Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8 items-start">
        <!-- Sidebar Facets -->
        <aside class="bg-card border border-border rounded-2xl p-5 space-y-6 lg:sticky lg:top-24">
          <div class="flex items-center justify-between pb-3 border-b border-border">
            <h3 class="font-semibold text-foreground text-sm flex items-center gap-2">
              <svg class="w-4 h-4 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
              </svg>
              {{ i18n.t('search.filter_price') }} & الفلاتر
            </h3>
            <button 
              *ngIf="hasActiveFilters()" 
              (click)="resetFilters()"
              class="text-xs text-primary hover:underline font-medium">
              إعادة تعيين
            </button>
          </div>

          <!-- Haggling Toggle -->
          <div class="bg-haggle/10 border border-haggle/20 rounded-xl p-3">
            <label class="flex items-center gap-3 cursor-pointer select-none">
              <input 
                type="checkbox" 
                [(ngModel)]="onlyAcceptsOffers"
                class="w-4 h-4 text-haggle rounded border-border focus:ring-haggle/30 focus:ring-offset-0 accent-haggle"
              />
              <span class="text-xs font-semibold text-haggle-foreground flex items-center gap-1.5">
                <span class="w-2 h-2 rounded-full bg-haggle animate-pulse"></span>
                {{ i18n.t('search.accepts_offers') }}
              </span>
            </label>
          </div>

          <!-- Governorate Facet -->
          <div class="space-y-2">
            <label class="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
              الولاية / الجهة الأصلية
            </label>
            <div class="space-y-1">
              <button 
                *ngFor="let gov of governorateList"
                (click)="selectedGovernorate.set(gov.id)"
                class="w-full text-start px-3 py-1.5 rounded-lg text-xs font-medium transition flex items-center justify-between"
                [ngClass]="selectedGovernorate() === gov.id ? 'bg-primary text-white' : 'text-foreground hover:bg-muted/60'"
              >
                <span>{{ gov.label }}</span>
                <span class="text-[10px]" [ngClass]="selectedGovernorate() === gov.id ? 'text-white/80' : 'text-muted-foreground'">
                  {{ getCountForGovernorate(gov.id) }}
                </span>
              </button>
            </div>
          </div>

          <!-- Max Price Range -->
          <div class="space-y-2">
            <div class="flex items-center justify-between text-xs font-semibold text-muted-foreground">
              <span>أقصى سعر</span>
              <span class="text-foreground font-mono font-bold">{{ maxPrice() | money }}</span>
            </div>
            <input 
              type="range" 
              min="10" 
              max="200" 
              step="5"
              [(ngModel)]="maxPrice"
              class="w-full accent-primary h-1.5 bg-muted rounded-lg appearance-none cursor-pointer"
            />
            <div class="flex justify-between text-[10px] text-muted-foreground">
              <span>10.000 د.ت</span>
              <span>200.000 د.ت</span>
            </div>
          </div>
        </aside>

        <!-- Product Grid -->
        <main class="lg:col-span-3 space-y-6">
          <div *ngIf="filteredProducts().length > 0; else noProducts" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
            <app-product-card 
              *ngFor="let product of filteredProducts()"
              [product]="product"
              (onMakeOffer)="onOpenHaggle($event)"
              (onAddToCart)="onAddToCart($event)"
            />
          </div>

          <ng-template #noProducts>
            <div class="bg-card border border-border rounded-2xl p-12 text-center space-y-4">
              <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mx-auto text-muted-foreground">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 class="text-base font-semibold text-foreground">
                {{ i18n.t('search.no_results') }}
              </h3>
              <p class="text-xs text-muted-foreground max-w-sm mx-auto">
                جرب تغيير مصطلحات البحث مثل "زيت", "هريسة", "توزر", أو إزالة الفلاتر المحددة.
              </p>
              <button 
                (click)="resetFilters()"
                class="px-4 py-2 bg-muted text-foreground text-xs font-medium rounded-xl hover:bg-muted/80 transition"
              >
                مسح جميع الفلاتر
              </button>
            </div>
          </ng-template>
        </main>
      </div>

      <!-- Haggle Bottom / Side Sheet -->
      <app-offer-sheet
        [product]="selectedHaggleProduct()"
        [isOpen]="isOfferSheetOpen()"
        (closeSheet)="isOfferSheetOpen.set(false)"
      />
    </div>
  `
})
export class SearchComponent implements OnInit {
  readonly api = inject(ApiService);
  readonly i18n = inject(I18nService);
  private readonly route = inject(ActivatedRoute);

  searchQuery = signal<string>('');
  selectedGovernorate = signal<string>('ALL');
  maxPrice = signal<number>(200);
  onlyAcceptsOffers = signal<boolean>(false);

  isOfferSheetOpen = signal<boolean>(false);
  selectedHaggleProduct = signal<Product | null>(null);

  readonly governorateList = [
    { id: 'ALL', label: 'جميع الولايات' },
    { id: 'صفاقس', label: 'صفاقس (Sfax)' },
    { id: 'نابل', label: 'نابل (Nabeul)' },
    { id: 'توزر', label: 'توزر (Tozeur)' },
    { id: 'مدنين', label: 'مدنين / جربة (Djerba)' }
  ];

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.searchQuery.set(params['q']);
      }
      if (params['gov']) {
        this.selectedGovernorate.set(params['gov']);
      }
    });
  }

  onSearchChange() {
    // Reactive signal handles updates automatically
  }

  readonly filteredProducts = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const gov = this.selectedGovernorate();
    const max = this.maxPrice();
    const haggleOnly = this.onlyAcceptsOffers();

    return this.api.initialProducts.filter(p => {
      // 1. Text & Arabizi match
      if (query) {
        const nameMatch = p.name.toLowerCase().includes(query);
        const descMatch = p.description.toLowerCase().includes(query);
        const arabiziMatch = 
          (query.includes('zit') && p.slug.includes('olive')) ||
          (query.includes('7rissa') && p.slug.includes('harissa')) ||
          (query.includes('degla') && p.slug.includes('dates')) ||
          (query.includes('fokhar') && p.slug.includes('pottery'));
        
        if (!nameMatch && !descMatch && !arabiziMatch) return false;
      }

      // 2. Governorate match
      if (gov !== 'ALL' && p.governorate !== gov) {
        return false;
      }

      // 3. Price filter
      const priceNum = parseFloat(p.price);
      if (priceNum > max) {
        return false;
      }

      // 4. Haggle toggle
      if (haggleOnly && !p.acceptsOffers) {
        return false;
      }

      return true;
    });
  });

  hasActiveFilters(): boolean {
    return this.searchQuery() !== '' || 
           this.selectedGovernorate() !== 'ALL' || 
           this.maxPrice() < 200 || 
           this.onlyAcceptsOffers();
  }

  resetFilters(): void {
    this.searchQuery.set('');
    this.selectedGovernorate.set('ALL');
    this.maxPrice.set(200);
    this.onlyAcceptsOffers.set(false);
  }

  getCountForGovernorate(govId: string): number {
    if (govId === 'ALL') return this.api.initialProducts.length;
    return this.api.initialProducts.filter(p => p.governorate === govId).length;
  }

  onOpenHaggle(product: Product) {
    this.selectedHaggleProduct.set(product);
    this.isOfferSheetOpen.set(true);
  }

  onAddToCart(product: Product) {
    this.api.addToCart(product.skuId, 1).subscribe();
  }
}
