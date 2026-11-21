import { Component, input, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Offer } from '../../core/models';
import { MoneyPipe } from '../../shared/ui/money.pipe';
import { CountdownTimerComponent } from '../../shared/ui/countdown-timer.component';
import { I18nService } from '../../core/i18n.service';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-offer-thread',
  standalone: true,
  imports: [CommonModule, FormsModule, MoneyPipe, CountdownTimerComponent],
  template: `
    @if (offer(); as currentOffer) {
      <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl overflow-hidden shadow-sm flex flex-col">
        <!-- Thread Header -->
        <div class="p-4 border-b border-[#E6DED2] bg-[#FBF8F3] flex items-center justify-between">
          <div class="flex items-center gap-3">
            <span class="w-3 h-3 rounded-full" 
                  [ngClass]="{
                    'bg-[#D49A2A]': currentOffer.status === 'PENDING' || currentOffer.status === 'COUNTERED',
                    'bg-[#566B2F]': currentOffer.status === 'ACCEPTED' || currentOffer.status === 'CONVERTED',
                    'bg-[#B8432A]': currentOffer.status === 'REJECTED',
                    'bg-[#6B635A]': currentOffer.status === 'EXPIRED' || currentOffer.status === 'WITHDRAWN'
                  }"></span>
            <div>
              <h3 class="text-sm font-semibold text-[#1E1B18]">
                مفاوضة السعر #{{ currentOffer.id }} (الجولة {{ currentOffer.round }})
              </h3>
              <span class="text-xs text-[#6B635A]">الكمية: {{ currentOffer.qty }} قطع</span>
            </div>
          </div>

          <!-- Status Badge -->
          <span 
            class="px-2.5 py-1 rounded-full text-xs font-semibold"
            [ngClass]="{
              'bg-[#D49A2A]/20 text-[#2A1F08]': currentOffer.status === 'PENDING' || currentOffer.status === 'COUNTERED',
              'bg-[#566B2F]/20 text-[#566B2F]': currentOffer.status === 'ACCEPTED' || currentOffer.status === 'CONVERTED',
              'bg-[#B8432A]/20 text-[#B8432A]': currentOffer.status === 'REJECTED',
              'bg-[#F3EEE6] text-[#6B635A]': currentOffer.status === 'EXPIRED'
            }"
          >
            @switch (currentOffer.status) {
              @case ('PENDING') { {{ i18n.t('haggle.status.pending') }} }
              @case ('COUNTERED') { {{ i18n.t('haggle.status.countered') }} }
              @case ('ACCEPTED') { {{ i18n.t('haggle.status.accepted') }} }
              @case ('REJECTED') { {{ i18n.t('haggle.status.rejected') }} }
              @case ('EXPIRED') { {{ i18n.t('haggle.status.expired') }} }
              @default { {{ currentOffer.status }} }
            }
          </span>
        </div>

        <!-- Chat Bubble Thread (Buyer on end side, Vendor on start side, naturally flipping under RTL!) -->
        <div class="p-4 space-y-4 max-h-[320px] overflow-y-auto bg-[#FBF8F3]/40">
          <!-- Buyer Initial Offer Bubble -->
          <div class="flex flex-col items-end">
            <div class="max-w-[80%] p-3.5 rounded-2xl rounded-te-none bg-[#B8432A] text-[#FFFFFF] shadow-sm">
              <span class="block text-[11px] opacity-80 mb-0.5">أنت (المشتري)</span>
              <p class="text-sm font-semibold tabular-nums">
                اقترح شراء {{ currentOffer.qty }} قطع بسعر {{ currentOffer.currentAmount | money }}
              </p>
            </div>
            <span class="text-[10px] text-[#6B635A] mt-1">السعر الأصلي: {{ currentOffer.listPrice | money }}</span>
          </div>

          <!-- Vendor Response Bubble -->
          @if (currentOffer.status === 'COUNTERED') {
            <div class="flex flex-col items-start">
              <div class="max-w-[80%] p-3.5 rounded-2xl rounded-ts-none bg-[#FFFFFF] border border-[#E6DED2] text-[#1E1B18] shadow-sm">
                <span class="block text-[11px] text-[#6B635A] mb-0.5">التاجر (Huilerie du Sud)</span>
                <p class="text-sm font-semibold tabular-nums text-[#D49A2A]">
                  عرض مقابل من التاجر: {{ currentOffer.currentAmount | money }}
                </p>
                <p class="text-xs text-[#6B635A] mt-1">"هذا أفضل سعر يمكنني تقديمه لهذه الجودة الممتازة."</p>
              </div>
            </div>
          }

          @if (currentOffer.status === 'ACCEPTED') {
            <div class="flex flex-col items-start">
              <div class="max-w-[80%] p-3.5 rounded-2xl rounded-ts-none bg-[#566B2F]/10 border border-[#566B2F]/30 text-[#1E1B18]">
                <span class="block text-[11px] text-[#566B2F] font-semibold mb-0.5">التاجر (Huilerie du Sud)</span>
                <p class="text-sm font-bold text-[#566B2F]">
                  مبروك! تم قبول عرضك بمبلغ {{ currentOffer.currentAmount | money }}
                </p>
              </div>
            </div>
          }
        </div>

        <!-- Accepted State: Countdown Ring & Sticky Add to Cart Button -->
        @if (currentOffer.status === 'ACCEPTED') {
          <div class="p-4 border-t border-[#E6DED2] bg-[#FFFFFF] space-y-3">
            <app-countdown-timer [expiresAt]="currentOffer.lockExpiresAt"></app-countdown-timer>

            <button 
              type="button"
              (click)="addLockedToCart()"
              class="w-full py-3.5 px-4 rounded-xl text-sm font-bold bg-[#566B2F] text-[#FFFFFF] hover:bg-[#566B2F]/90 transition-all shadow-sm flex items-center justify-center gap-2"
            >
              {{ i18n.t('haggle.add_locked_cart') }} ({{ currentOffer.currentAmount | money }})
            </button>
          </div>
        }

        <!-- Countered State Actions for Buyer -->
        @if (currentOffer.status === 'COUNTERED') {
          <div class="p-4 border-t border-[#E6DED2] bg-[#FFFFFF] flex gap-2">
            <button 
              type="button" 
              (click)="acceptCountered()"
              class="flex-1 py-2.5 px-3 rounded-lg text-xs font-semibold bg-[#566B2F] text-[#FFFFFF] hover:bg-[#566B2F]/90"
            >
              {{ i18n.t('haggle.accept_button') }}
            </button>
            <button 
              type="button" 
              (click)="showCounterInput.set(!showCounterInput())"
              class="flex-1 py-2.5 px-3 rounded-lg text-xs font-semibold border border-[#E6DED2] bg-[#FFFFFF] text-[#1E1B18] hover:bg-[#F3EEE6]"
            >
              {{ i18n.t('haggle.counter_button') }}
            </button>
          </div>

          @if (showCounterInput()) {
            <div class="p-4 border-t border-[#E6DED2] bg-[#FBF8F3] flex gap-2">
              <input 
                type="number"
                step="0.500"
                [(ngModel)]="newCounterAmount"
                class="flex-1 py-2 px-3 text-sm rounded-lg border border-[#E6DED2] bg-[#FFFFFF] font-bold tabular-nums"
                placeholder="السعر الجديد (DT)"
              />
              <button 
                type="button"
                (click)="sendBuyerCounter()"
                class="py-2 px-4 rounded-lg text-xs font-semibold bg-[#D49A2A] text-[#2A1F08]"
              >
                إرسال
              </button>
            </div>
          }
        }
      </div>
    }
  `
})
export class OfferThreadComponent {
  readonly offer = input<Offer | null>(null);

  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);

  readonly showCounterInput = signal<boolean>(false);
  newCounterAmount: number = 0;

  addLockedToCart() {
    const o = this.offer();
    if (!o) return;
    this.api.addToCart(o.skuId, o.qty, o.id).subscribe();
  }

  acceptCountered() {
    const o = this.offer();
    if (!o) return;
    this.api.submitOffer(o.skuId, o.qty, o.currentAmount).subscribe();
  }

  sendBuyerCounter() {
    const o = this.offer();
    if (!o || this.newCounterAmount <= 0) return;
    this.api.submitOffer(o.skuId, o.qty, this.newCounterAmount.toFixed(3)).subscribe(() => {
      this.showCounterInput.set(false);
    });
  }
}
