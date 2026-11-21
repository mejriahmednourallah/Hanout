import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Product, Offer } from '../../core/models';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { ProductCardComponent } from './product-card.component';
import { OfferSheetComponent } from '../offers/offer-sheet.component';
import { OfferThreadComponent } from '../offers/offer-thread.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ProductCardComponent, OfferSheetComponent, OfferThreadComponent],
  template: `
    <div class="space-y-12 pb-16">
      <!-- 1. Search Bar First (As designed: prominent, warm, no slop hero banner) -->
      <section class="pt-6 pb-4">
        <div class="max-w-3xl mx-auto text-center space-y-4">
          <h1 class="text-3xl md:text-4xl font-serif font-bold text-[#1E1B18] tracking-tight">
            {{ i18n.t('app.subtitle') }}
          </h1>

          <!-- Search Input with Trilingual / Arabizi support -->
          <div class="relative flex items-center">
            <input 
              type="text" 
              [(ngModel)]="searchQuery"
              (keyup.enter)="onSearch()"
              [placeholder]="i18n.t('search.placeholder')"
              class="w-full py-4 ps-5 pe-28 text-base bg-[#FFFFFF] border-2 border-[#E6DED2] rounded-2xl shadow-sm focus:border-[#B8432A] focus:outline-none transition-all placeholder:text-[#6B635A]/60"
            />
            <button 
              type="button"
              (click)="onSearch()"
              class="absolute end-2 top-2 bottom-2 px-6 rounded-xl font-semibold text-sm bg-[#B8432A] text-[#FFFFFF] hover:bg-[#B8432A]/90 transition-colors flex items-center gap-1.5"
            >
              {{ i18n.t('search.button') }}
            </button>
          </div>

          <!-- Quick Discovery Tags -->
          <div class="flex flex-wrap items-center justify-center gap-2 pt-1 text-xs text-[#6B635A]">
            <span>جرب البحث بـ:</span>
            <button (click)="searchTag('zitoun')" class="px-2.5 py-1 rounded-md bg-[#F3EEE6] hover:bg-[#E6DED2] text-[#1E1B18]">zitoun</button>
            <button (click)="searchTag('7rissa')" class="px-2.5 py-1 rounded-md bg-[#F3EEE6] hover:bg-[#E6DED2] text-[#1E1B18]">7rissa</button>
            <button (click)="searchTag('degla')" class="px-2.5 py-1 rounded-md bg-[#F3EEE6] hover:bg-[#E6DED2] text-[#1E1B18]">degla</button>
            <button (click)="searchTag('صفاقس')" class="px-2.5 py-1 rounded-md bg-[#F3EEE6] hover:bg-[#E6DED2] text-[#1E1B18]">صفاقس</button>
            <button (click)="searchTag('جربة')" class="px-2.5 py-1 rounded-md bg-[#F3EEE6] hover:bg-[#E6DED2] text-[#1E1B18]">جربة</button>
          </div>
        </div>
      </section>

      <!-- Active Negotiations Live Carousel (if user has active offers) -->
      @if (api.activeOffers().length > 0) {
        <section class="max-w-7xl mx-auto px-4">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-[#1E1B18] flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-[#D49A2A] animate-pulse"></span>
              مفاوضاتك المباشرة الحالية
            </h2>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            @for (off of api.activeOffers(); track off.id) {
              <app-offer-thread [offer]="off"></app-offer-thread>
            }
          </div>
        </section>
      }

      <!-- 2. Regional Shelf 1: "From Sfax" (من صفاقس - زيت الزيتون البكر) -->
      <section class="max-w-7xl mx-auto px-4">
        <div class="flex items-baseline justify-between mb-5 border-b border-[#E6DED2] pb-3">
          <div>
            <span class="text-xs font-semibold text-[#566B2F] tracking-wider uppercase">محصول الموسم</span>
            <h2 class="text-xl md:text-2xl font-serif font-bold text-[#1E1B18]">من صفاقس والساحل (Zit Zitoun)</h2>
          </div>
          <a routerLink="/search" [queryParams]="{region: 'Sfax'}" class="text-xs font-semibold text-[#2B5C9E] hover:underline">
            عرض الكل &larr;
          </a>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
          @for (prod of sfaxProducts(); track prod.productId) {
            <app-product-card 
              [product]="prod"
              (onMakeOffer)="openOfferSheet($event)"
              (onAddToCart)="addToCart($event)"
            ></app-product-card>
          }
        </div>
      </section>

      <!-- 3. Regional Shelf 2: "From Nabeul" (من نابل والوطن القبلي - الهريسة العربي) -->
      <section class="max-w-7xl mx-auto px-4">
        <div class="flex items-baseline justify-between mb-5 border-b border-[#E6DED2] pb-3">
          <div>
            <span class="text-xs font-semibold text-[#B8432A] tracking-wider uppercase">أصالة الوطن القبلي</span>
            <h2 class="text-xl md:text-2xl font-serif font-bold text-[#1E1B18]">من نابل (7rissa & Terroir)</h2>
          </div>
          <a routerLink="/search" [queryParams]="{region: 'Nabeul'}" class="text-xs font-semibold text-[#2B5C9E] hover:underline">
            عرض الكل &larr;
          </a>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
          @for (prod of nabeulProducts(); track prod.productId) {
            <app-product-card 
              [product]="prod"
              (onMakeOffer)="openOfferSheet($event)"
              (onAddToCart)="addToCart($event)"
            ></app-product-card>
          }
        </div>
      </section>

      <!-- 4. Regional Shelf 3: "From Tozeur & The South" (من توزر والجريد - دقلة النور والفخار) -->
      <section class="max-w-7xl mx-auto px-4">
        <div class="flex items-baseline justify-between mb-5 border-b border-[#E6DED2] pb-3">
          <div>
            <span class="text-xs font-semibold text-[#D49A2A] tracking-wider uppercase">خيرات الواحات والجنوب</span>
            <h2 class="text-xl md:text-2xl font-serif font-bold text-[#1E1B18]">من توزر وجربة (Deglet Nour & Artisanat)</h2>
          </div>
          <a routerLink="/search" [queryParams]="{region: 'Tozeur'}" class="text-xs font-semibold text-[#2B5C9E] hover:underline">
            عرض الكل &larr;
          </a>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
          @for (prod of tozeurProducts(); track prod.productId) {
            <app-product-card 
              [product]="prod"
              (onMakeOffer)="openOfferSheet($event)"
              (onAddToCart)="addToCart($event)"
            ></app-product-card>
          }
        </div>
      </section>

      <!-- Offer Bottom / Side Sheet Modal -->
      <app-offer-sheet
        [product]="selectedProductForOffer()"
        [isOpen]="isOfferSheetOpen()"
        (onClose)="isOfferSheetOpen.set(false)"
      ></app-offer-sheet>
    </div>
  `
})
export class HomeComponent implements OnInit {
  readonly i18n = inject(I18nService);
  readonly api = inject(ApiService);

  searchQuery: string = '';
  readonly allProducts = signal<Product[]>([]);
  readonly selectedProductForOffer = signal<Product | null>(null);
  readonly isOfferSheetOpen = signal<boolean>(false);

  ngOnInit() {
    this.api.searchProducts().subscribe(prods => this.allProducts.set(prods));
  }

  readonly sfaxProducts = () => this.allProducts().filter(p => p.region === 'Sfax' || p.slug.includes('olive'));
  readonly nabeulProducts = () => this.allProducts().filter(p => p.region === 'Nabeul' || p.slug.includes('harissa'));
  readonly tozeurProducts = () => this.allProducts().filter(p => p.region === 'Tozeur' || p.region === 'Djerba' || p.slug.includes('dates') || p.slug.includes('tagine'));

  onSearch() {
    this.api.searchProducts(this.searchQuery, this.i18n.currentLang()).subscribe(prods => {
      this.allProducts.set(prods);
    });
  }

  searchTag(tag: string) {
    this.searchQuery = tag;
    this.onSearch();
  }

  openOfferSheet(prod: Product) {
    this.selectedProductForOffer.set(prod);
    this.isOfferSheetOpen.set(true);
  }

  addToCart(prod: Product) {
    this.api.addToCart(prod.skuId, 1).subscribe();
  }
}
