import { Component, input, output, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from '../../core/models';
import { MoneyPipe } from '../../shared/ui/money.pipe';
import { I18nService } from '../../core/i18n.service';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-offer-sheet',
  standalone: true,
  imports: [CommonModule, FormsModule, MoneyPipe],
  template: `
    @if (isOpen()) {
      <!-- Backdrop -->
      <div 
        class="fixed inset-0 bg-black/40 z-50 transition-opacity backdrop-blur-[2px]"
        (click)="onClose.emit()"
      ></div>

      <!-- Sheet Container: Bottom sheet on mobile, Right/Left side sheet on desktop -->
      <div 
        class="fixed z-50 bg-[#FFFFFF] border-t md:border-s border-[#E6DED2] shadow-2xl transition-transform duration-300 ease-out
               inset-x-0 bottom-0 md:inset-y-0 md:start-auto md:end-0 md:w-[420px] rounded-t-2xl md:rounded-s-2xl md:rounded-t-none
               max-h-[90vh] md:max-h-full flex flex-col overflow-hidden"
      >
        <!-- Header -->
        <div class="p-5 border-b border-[#E6DED2] flex items-center justify-between bg-[#FBF8F3]">
          <div>
            <h2 class="text-lg font-semibold text-[#1E1B18] flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-[#D49A2A]"></span>
              {{ i18n.t('haggle.title') }}
            </h2>
            <p class="text-xs text-[#6B635A] mt-0.5">{{ i18n.t('haggle.reply_estimate') }}</p>
          </div>
          <button 
            type="button" 
            (click)="onClose.emit()"
            class="w-8 h-8 rounded-full border border-[#E6DED2] flex items-center justify-center text-[#6B635A] hover:text-[#1E1B18] hover:bg-[#F3EEE6]"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Scrollable Content -->
        <div class="p-5 space-y-6 overflow-y-auto flex-1">
          <!-- Product Summary -->
          @if (product(); as prod) {
            <div class="flex items-center gap-3.5 p-3 rounded-xl bg-[#F3EEE6]/60 border border-[#E6DED2]">
              <img [src]="prod.imageUrl" [alt]="prod.name" class="w-14 h-18 object-cover rounded-lg shrink-0 aspect-[4/5]" />
              <div class="min-w-0 flex-1">
                <span class="text-xs text-[#6B635A] block">{{ prod.governorate }}</span>
                <h4 class="text-sm font-medium text-[#1E1B18] truncate">{{ prod.name }}</h4>
                <div class="mt-1 flex items-baseline gap-2">
                  <span class="text-xs text-[#6B635A]">{{ i18n.t('haggle.list_price') }}:</span>
                  <span class="text-sm font-semibold text-[#1E1B18] tabular-nums">{{ prod.price | money }}</span>
                </div>
              </div>
            </div>
          }

          <!-- Quantity Stepper -->
          <div>
            <label class="block text-xs font-semibold text-[#1E1B18] mb-2">
              {{ i18n.t('haggle.quantity') }}
            </label>
            <div class="flex items-center gap-3">
              <button 
                type="button" 
                (click)="decrementQty()"
                [disabled]="quantity() <= 1"
                class="w-10 h-10 rounded-lg border border-[#E6DED2] bg-[#FFFFFF] font-bold text-lg disabled:opacity-40 hover:bg-[#F3EEE6] transition-colors"
              >-</button>
              <span class="text-base font-bold text-[#1E1B18] tabular-nums w-8 text-center">{{ quantity() }}</span>
              <button 
                type="button" 
                (click)="incrementQty()"
                class="w-10 h-10 rounded-lg border border-[#E6DED2] bg-[#FFFFFF] font-bold text-lg hover:bg-[#F3EEE6] transition-colors"
              >+</button>
            </div>
          </div>

          <!-- Quick Discount Chips (-5%, -10%, -15%) -->
          <div>
            <label class="block text-xs font-semibold text-[#1E1B18] mb-2">
              {{ i18n.t('haggle.quick_chips') }}
            </label>
            <div class="flex gap-2">
              @for (chip of [-5, -10, -15]; track chip) {
                <button
                  type="button"
                  (click)="applyChip(chip)"
                  class="flex-1 py-2 px-3 text-xs font-semibold rounded-lg border border-[#E6DED2] bg-[#FFFFFF] hover:border-[#D49A2A] hover:bg-[#D49A2A]/10 text-[#1E1B18] transition-all tabular-nums"
                >
                  {{ chip }}%
                </button>
              }
            </div>
          </div>

          <!-- Proposed Price Stepper in TND -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="text-xs font-semibold text-[#1E1B18]">
                {{ i18n.t('haggle.your_offer') }} (TND)
              </label>
              <span class="text-xs font-medium text-[#D49A2A] tabular-nums">
                {{ discountPercent() }}% تخفيض
              </span>
            </div>

            <div class="flex items-center gap-3">
              <button 
                type="button" 
                (click)="stepDown()"
                class="w-11 h-11 rounded-lg border border-[#E6DED2] bg-[#FFFFFF] font-bold text-lg hover:bg-[#F3EEE6]"
              >-1</button>
              <div class="flex-1 relative">
                <input 
                  type="number" 
                  step="0.500"
                  [ngModel]="proposedAmount()"
                  (ngModelChange)="onPriceInput($event)"
                  class="w-full text-center py-2.5 px-3 rounded-lg border border-[#E6DED2] bg-[#FBF8F3] text-lg font-bold text-[#1E1B18] tabular-nums focus:outline-none focus:ring-2 focus:ring-[#B8432A]/20 focus:border-[#B8432A]"
                />
                <span class="absolute end-3 top-3 text-xs text-[#6B635A] pointer-events-none">DT</span>
              </div>
              <button 
                type="button" 
                (click)="stepUp()"
                class="w-11 h-11 rounded-lg border border-[#E6DED2] bg-[#FFFFFF] font-bold text-lg hover:bg-[#F3EEE6]"
              >+1</button>
            </div>
          </div>

          <!-- Total Calculation Banner -->
          <div class="p-3 rounded-lg border border-[#E6DED2] bg-[#FBF8F3] flex items-center justify-between">
            <span class="text-xs text-[#6B635A]">إجمالي السعر المقترح للكمية:</span>
            <span class="text-base font-bold text-[#1E1B18] tabular-nums">
              {{ (proposedAmount() * quantity()).toFixed(3) }} DT
            </span>
          </div>
        </div>

        <!-- Footer / Submit Action -->
        <div class="p-5 border-t border-[#E6DED2] bg-[#FBF8F3]">
          <button 
            type="button"
            (click)="submit()"
            [disabled]="isSubmitting()"
            class="w-full py-3.5 px-4 rounded-xl text-sm font-semibold bg-[#D49A2A] text-[#2A1F08] hover:bg-[#D49A2A]/90 transition-all shadow-sm flex items-center justify-center gap-2 disabled:opacity-50"
          >
            @if (isSubmitting()) {
              <span class="inline-block w-4 h-4 border-2 border-[#2A1F08] border-t-transparent rounded-full animate-spin"></span>
            }
            {{ i18n.t('haggle.submit_button') }} ({{ (proposedAmount() * quantity()).toFixed(3) }} DT)
          </button>
        </div>
      </div>
    }
  `
})
export class OfferSheetComponent {
  readonly product = input<Product | null>(null);
  readonly isOpen = input<boolean>(false);
  readonly onClose = output<void>();
  readonly onOfferSubmitted = output<any>();

  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);

  readonly quantity = signal<number>(1);
  readonly proposedAmount = signal<number>(0);
  readonly isSubmitting = signal<boolean>(false);

  // Initialize amount when product is selected
  constructor() {
    // Whenever a product arrives, default to -5%
    setInterval(() => {
      const p = this.product();
      if (p && this.proposedAmount() === 0) {
        const base = parseFloat(p.price);
        this.proposedAmount.set(Math.round(base * 0.95 * 2) / 2);
      }
    }, 200);
  }

  readonly listPrice = computed(() => {
    const p = this.product();
    return p ? parseFloat(p.price) : 0;
  });

  readonly discountPercent = computed(() => {
    const lp = this.listPrice();
    if (lp <= 0) return 0;
    const diff = lp - this.proposedAmount();
    return Math.max(0, Math.round((diff / lp) * 100));
  });

  incrementQty() {
    this.quantity.update(q => q + 1);
  }

  decrementQty() {
    this.quantity.update(q => Math.max(1, q - 1));
  }

  applyChip(pctDelta: number) {
    const lp = this.listPrice();
    const multiplier = 1 + (pctDelta / 100);
    const amount = Math.round(lp * multiplier * 2) / 2;
    this.proposedAmount.set(amount);
  }

  stepUp() {
    this.proposedAmount.update(a => a + 1);
  }

  stepDown() {
    this.proposedAmount.update(a => Math.max(1, a - 1));
  }

  onPriceInput(val: number) {
    this.proposedAmount.set(val || 0);
  }

  submit() {
    const prod = this.product();
    if (!prod) return;

    this.isSubmitting.set(true);
    const totalAmountDinar = (this.proposedAmount() * this.quantity()).toFixed(3);

    this.api.submitOffer(prod.skuId, this.quantity(), totalAmountDinar).subscribe({
      next: (offer) => {
        this.isSubmitting.set(false);
        this.onOfferSubmitted.emit(offer);
        this.onClose.emit();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.onClose.emit();
      }
    });
  }
}
