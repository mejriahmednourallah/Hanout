import { Component, input, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Product } from '../../core/models';
import { MoneyPipe } from '../../shared/ui/money.pipe';
import { I18nService } from '../../core/i18n.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterModule, MoneyPipe],
  template: `
    <div class="group relative bg-[#FFFFFF] rounded-xl border border-[#E6DED2] overflow-hidden flex flex-col transition-all hover:border-[#B8432A]/30">
      <!-- 4:5 Image Container with consistent ratio -->
      <div class="relative w-full aspect-[4/5] bg-[#F3EEE6] overflow-hidden">
        <img 
          [src]="product().imageUrl" 
          [alt]="product().name"
          class="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
          loading="lazy"
        />

        <!-- Badges Container -->
        <div class="absolute top-2.5 start-2.5 flex flex-col gap-1.5 z-10">
          @if (product().acceptsOffers) {
            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-[#D49A2A] text-[#2A1F08] shadow-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-[#2A1F08] me-1"></span>
              {{ i18n.t('product.accepts_offers') }}
            </span>
          }
          <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-[#FFFFFF]/90 backdrop-blur-sm text-[#1E1B18] border border-[#E6DED2] shadow-sm">
            {{ i18n.t('product.cod_badge') }}
          </span>
        </div>

        <!-- Desktop Hover Quick Action Overlay -->
        <div class="absolute inset-x-0 bottom-0 p-3 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-2">
          <button 
            type="button"
            (click)="$event.stopPropagation(); onMakeOffer.emit(product())"
            class="flex-1 py-2 px-3 text-xs font-semibold rounded-lg bg-[#D49A2A] text-[#2A1F08] hover:bg-[#D49A2A]/90 transition-colors shadow-sm"
          >
            {{ i18n.t('product.make_offer') }}
          </button>
          <button 
            type="button"
            (click)="$event.stopPropagation(); onAddToCart.emit(product())"
            class="flex-1 py-2 px-3 text-xs font-semibold rounded-lg bg-[#FFFFFF] text-[#1E1B18] hover:bg-[#F3EEE6] transition-colors shadow-sm"
          >
            {{ i18n.t('product.add_to_cart') }}
          </button>
        </div>
      </div>

      <!-- Card Content -->
      <div class="p-3.5 flex flex-col flex-1">
        <!-- Vendor & Governorate in small muted text -->
        <div class="flex items-center justify-between text-xs text-[#6B635A] mb-1">
          <span class="truncate">{{ product().governorate || 'تونس' }}</span>
          <span class="text-[11px] text-[#566B2F] font-medium">{{ i18n.t('product.in_stock') }}</span>
        </div>

        <!-- Product Title -->
        <h3 class="font-medium text-sm text-[#1E1B18] line-clamp-1 mb-2 hover:text-[#B8432A] transition-colors">
          {{ product().name }}
        </h3>

        <!-- Price on its own line -->
        <div class="mt-auto pt-2 border-t border-[#E6DED2]/60 flex items-baseline justify-between">
          <span class="text-base font-bold text-[#1E1B18] tabular-nums">
            {{ product().price | money }}
          </span>
        </div>

        <!-- Mobile Always-Visible CTA -->
        <div class="mt-3 md:hidden flex gap-2">
          <button 
            type="button"
            (click)="onMakeOffer.emit(product())"
            class="flex-1 py-2 text-xs font-semibold rounded-lg bg-[#D49A2A] text-[#2A1F08]"
          >
            {{ i18n.t('product.make_offer') }}
          </button>
          <button 
            type="button"
            (click)="onAddToCart.emit(product())"
            class="py-2 px-3 text-xs font-medium rounded-lg border border-[#E6DED2] bg-[#FFFFFF]"
          >
            +
          </button>
        </div>
      </div>
    </div>
  `
})
export class ProductCardComponent {
  readonly product = input.required<Product>();
  readonly onMakeOffer = output<Product>();
  readonly onAddToCart = output<Product>();

  readonly i18n = inject(I18nService);
}
