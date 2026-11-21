import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { MoneyPipe } from '../../shared/ui/money.pipe';
import { Offer, Product } from '../../core/models';

@Component({
  selector: 'app-vendor-portal',
  standalone: true,
  imports: [CommonModule, FormsModule, MoneyPipe],
  template: `
    <div class="max-w-6xl mx-auto px-4 py-8 space-y-8">
      <!-- Portal Header -->
      <div class="flex items-center justify-between border-b border-[#E6DED2] pb-5">
        <div>
          <span class="text-xs font-semibold text-[#566B2F] uppercase tracking-wider">لوحة تحكم التاجر الحرفي</span>
          <h1 class="text-2xl font-serif font-bold text-[#1E1B18]">{{ i18n.t('vendor.title') }}</h1>
          <span class="text-xs text-[#6B635A]">معصرة الجنوب بصفاقس (معرّف جبائي: 1234567/A/M/000)</span>
        </div>
        <span class="px-3 py-1 rounded-full text-xs font-semibold bg-[#566B2F]/10 text-[#566B2F] border border-[#566B2F]/20">
          ✓ تاجر معتمد وخاضع للأداء (TVA 19%)
        </span>
      </div>

      <!-- 1. Metric Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-5 shadow-sm">
          <span class="text-xs text-[#6B635A] block mb-1">{{ i18n.t('vendor.metrics_sales') }}</span>
          <span class="text-2xl font-bold text-[#1E1B18] tabular-nums">1,840.000 DT</span>
          <span class="text-[11px] text-[#566B2F] block mt-1">+12% هذا الأسبوع</span>
        </div>

        <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-5 shadow-sm">
          <span class="text-xs text-[#6B635A] block mb-1">{{ i18n.t('vendor.metrics_offers') }}</span>
          <span class="text-2xl font-bold text-[#D49A2A] tabular-nums">
            {{ pendingOffers().length }}
          </span>
          <span class="text-[11px] text-[#6B635A] block mt-1">تتطلب قرارك بالقبول أو الرفض</span>
        </div>

        <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-5 shadow-sm">
          <span class="text-xs text-[#6B635A] block mb-1">{{ i18n.t('vendor.metrics_conversion') }}</span>
          <span class="text-2xl font-bold text-[#566B2F] tabular-nums">84%</span>
          <span class="text-[11px] text-[#6B635A] block mt-1">نسبة تحول العروض لطلبات ناجحة</span>
        </div>
      </div>

      <!-- 2. Offers Inbox as Home Screen (Keyboard shortcuts: [A]ccept, [R]eject, [C]ounter) -->
      <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl overflow-hidden shadow-sm">
        <div class="p-5 border-b border-[#E6DED2] bg-[#FBF8F3] flex items-center justify-between">
          <div>
            <h2 class="text-base font-bold text-[#1E1B18] flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-[#D49A2A]"></span>
              {{ i18n.t('vendor.offers_inbox') }}
            </h2>
            <p class="text-xs text-[#6B635A] mt-0.5">يمكنك الاستجابة السريعة لعروض المشترين مباشرة من هنا</p>
          </div>
          <span class="text-xs text-[#6B635A] hidden md:inline-block">
            اختصارات لوحة المفاتيح: [A] قبول | [R] رفض
          </span>
        </div>

        @if (pendingOffers().length === 0) {
          <div class="p-12 text-center bg-zellige space-y-2">
            <span class="text-2xl">✨</span>
            <h3 class="text-sm font-semibold text-[#1E1B18]">لا توجد عروض معلقة حالياً</h3>
            <p class="text-xs text-[#6B635A]">جميع عروض المساومة تمت معالجتها بنجاح.</p>
          </div>
        } @else {
          <div class="divide-y divide-[#E6DED2]">
            @for (offer of pendingOffers(); track offer.id) {
              <div class="p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 hover:bg-[#FBF8F3]/50 transition-colors">
                <div class="space-y-1">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold text-[#B8432A]">عرض #{{ offer.id }}</span>
                    <span class="text-xs text-[#6B635A]">&bull; الجولة {{ offer.round }}</span>
                    <span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-[#D49A2A]/20 text-[#2A1F08]">
                      الكمية: {{ offer.qty }} قطع
                    </span>
                  </div>
                  <h4 class="text-sm font-semibold text-[#1E1B18]">
                    طلب شراء: زيت زيتون بكر ممتاز صفاقس 5 لتر
                  </h4>
                  <div class="flex items-baseline gap-3 text-xs">
                    <span class="text-[#6B635A]">السعر الأصلي: <span class="line-through tabular-nums">{{ offer.listPrice | money }}</span></span>
                    <span class="text-sm font-bold text-[#D49A2A] tabular-nums">
                      السعر المقترح: {{ offer.currentAmount | money }}
                    </span>
                  </div>
                </div>

                <!-- Action Buttons: Accept / Counter / Reject -->
                <div class="flex items-center gap-2">
                  <button 
                    type="button"
                    (click)="accept(offer.id)"
                    class="py-2.5 px-4 rounded-xl text-xs font-semibold bg-[#566B2F] text-[#FFFFFF] hover:bg-[#566B2F]/90 transition-colors shadow-sm flex items-center gap-1.5"
                  >
                    ✓ {{ i18n.t('vendor.accept') }}
                  </button>
                  <button 
                    type="button"
                    (click)="openCounterDialog(offer)"
                    class="py-2.5 px-3.5 rounded-xl text-xs font-semibold border border-[#E6DED2] bg-[#FFFFFF] text-[#1E1B18] hover:bg-[#F3EEE6] transition-colors"
                  >
                    {{ i18n.t('vendor.counter') }}
                  </button>
                  <button 
                    type="button"
                    (click)="reject(offer.id)"
                    class="py-2.5 px-3.5 rounded-xl text-xs font-semibold border border-[#E6DED2] text-[#B8432A] hover:bg-[#B8432A]/10 transition-colors"
                  >
                    {{ i18n.t('vendor.reject') }}
                  </button>
                </div>
              </div>
            }
          </div>
        }
      </div>

      <!-- Counter Offer Modal -->
      @if (counterModalOpen()) {
        <div class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 backdrop-blur-[2px]">
          <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-6 max-w-sm w-full shadow-2xl space-y-4">
            <h3 class="text-base font-bold text-[#1E1B18]">تقديم عرض مقابل للمشتري</h3>
            <p class="text-xs text-[#6B635A]">اقترح السعر المناسب لك لإتمام عملية البيع</p>
            
            <div class="relative">
              <input 
                type="number" 
                step="0.500"
                [(ngModel)]="counterAmount"
                class="w-full py-2.5 px-4 rounded-xl border border-[#E6DED2] bg-[#FBF8F3] text-lg font-bold text-[#1E1B18] tabular-nums focus:outline-none focus:border-[#B8432A]"
                placeholder="السعر (DT)"
              />
              <span class="absolute end-3 top-3 text-xs text-[#6B635A]">DT</span>
            </div>

            <div class="flex gap-2 pt-2">
              <button 
                type="button"
                (click)="submitCounter()"
                class="flex-1 py-2.5 rounded-xl text-xs font-bold bg-[#D49A2A] text-[#2A1F08]"
              >
                إرسال العرض المقابل
              </button>
              <button 
                type="button"
                (click)="counterModalOpen.set(false)"
                class="py-2.5 px-4 rounded-xl text-xs font-semibold border border-[#E6DED2]"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class VendorPortalComponent implements OnInit {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);

  readonly pendingOffers = signal<Offer[]>([]);
  readonly counterModalOpen = signal<boolean>(false);
  activeOfferId?: number;
  counterAmount: number = 0;

  ngOnInit() {
    // Seed sample pending offer for vendor demo
    this.pendingOffers.set([
      {
        id: 101,
        skuId: 1,
        buyerId: 1,
        vendorId: 1,
        qty: 2,
        listPrice: '250.000',
        currentAmount: '215.000',
        status: 'PENDING',
        round: 1,
        expiresAt: new Date(Date.now() + 20 * 3600 * 1000).toISOString(),
        version: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    ]);
  }

  accept(offerId: number) {
    this.api.vendorAccept(offerId).subscribe(() => {
      this.pendingOffers.update(list => list.filter(o => o.id !== offerId));
    });
  }

  reject(offerId: number) {
    this.api.vendorReject(offerId).subscribe(() => {
      this.pendingOffers.update(list => list.filter(o => o.id !== offerId));
    });
  }

  openCounterDialog(offer: Offer) {
    this.activeOfferId = offer.id;
    this.counterAmount = parseFloat(offer.currentAmount) + 15;
    this.counterModalOpen.set(true);
  }

  submitCounter() {
    if (!this.activeOfferId || this.counterAmount <= 0) return;
    this.api.vendorCounter(this.activeOfferId, this.counterAmount.toFixed(3)).subscribe(() => {
      this.pendingOffers.update(list => list.filter(o => o.id !== this.activeOfferId));
      this.counterModalOpen.set(false);
    });
  }
}
