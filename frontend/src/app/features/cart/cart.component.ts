import { Component, OnInit, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { MoneyPipe } from '../../shared/ui/money.pipe';
import { CartLine } from '../../core/models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, MoneyPipe],
  template: `
    <div class="max-w-4xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-serif font-bold text-[#1E1B18] mb-6 flex items-center gap-2">
        <span>{{ i18n.t('cart.title') }}</span>
        <span class="text-sm font-sans font-normal text-[#6B635A]">({{ totalItems() }} قطع)</span>
      </h1>

      @if (cart().lines.length === 0) {
        <div class="p-12 text-center rounded-2xl border border-[#E6DED2] bg-[#FFFFFF] bg-zellige space-y-4">
          <div class="w-12 h-12 mx-auto rounded-full bg-[#F3EEE6] flex items-center justify-center text-xl text-[#6B635A]">
            🛒
          </div>
          <h3 class="text-base font-semibold text-[#1E1B18]">{{ i18n.t('cart.empty') }}</h3>
          <p class="text-xs text-[#6B635A] max-w-sm mx-auto">{{ i18n.t('cart.empty_desc') }}</p>
          <a routerLink="/" class="inline-block px-5 py-2.5 rounded-xl text-xs font-semibold bg-[#B8432A] text-[#FFFFFF] hover:bg-[#B8432A]/90 transition-colors">
            تصفح السوق التونسي
          </a>
        </div>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- Cart Items Grouped By Vendor (Matching Backend Sub-orders!) -->
          <div class="md:col-span-2 space-y-6">
            @for (group of vendorGroups(); track group.vendorId) {
              <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl overflow-hidden shadow-sm">
                <!-- Vendor Sub-Order Header -->
                <div class="p-4 border-b border-[#E6DED2] bg-[#FBF8F3] flex items-center justify-between">
                  <div>
                    <span class="text-[11px] text-[#6B635A]">طلب فرعي من التاجر:</span>
                    <h3 class="text-sm font-bold text-[#1E1B18]">
                      {{ group.vendorName }}
                    </h3>
                  </div>
                  <span class="px-2 py-0.5 rounded text-[11px] font-medium bg-[#566B2F]/10 text-[#566B2F]">
                    شحن مباشر
                  </span>
                </div>

                <!-- Lines for this vendor -->
                <div class="divide-y divide-[#E6DED2]/60 p-4">
                  @for (line of group.lines; track line.cartItemId) {
                    <div class="py-3 flex items-center justify-between gap-4">
                      <div class="flex-1">
                        <div class="flex items-center gap-2">
                          <h4 class="text-sm font-semibold text-[#1E1B18]">
                            {{ line.productName || line.skuCode }}
                          </h4>
                          @if (line.isOfferLocked) {
                            <span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-[#D49A2A]/20 text-[#2A1F08]">
                              🔒 {{ i18n.t('cart.locked_price_badge') }}
                            </span>
                          }
                        </div>
                        <div class="text-xs text-[#6B635A] mt-1 tabular-nums">
                          {{ line.qty }} × {{ line.unitPrice | money }}
                        </div>
                      </div>

                      <div class="text-end">
                        <span class="text-sm font-bold text-[#1E1B18] tabular-nums">
                          {{ line.lineTotal | money }}
                        </span>
                      </div>
                    </div>
                  }
                </div>

                <!-- Per-Vendor Shipping Line (Rapide-Poste) -->
                <div class="p-3.5 bg-[#FBF8F3]/60 border-t border-[#E6DED2] flex items-center justify-between text-xs text-[#6B635A]">
                  <span>{{ i18n.t('cart.shipping') }} ({{ group.vendorName }})</span>
                  <span class="font-semibold text-[#1E1B18] tabular-nums">7.000 DT</span>
                </div>
              </div>
            }
          </div>

          <!-- Order Summary Sidebar -->
          <div class="md:col-span-1">
            <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-5 shadow-sm space-y-4 sticky top-6">
              <h2 class="text-base font-bold text-[#1E1B18] border-b border-[#E6DED2] pb-3">
                ملخص الطلب
              </h2>

              <div class="space-y-2.5 text-xs">
                <div class="flex justify-between text-[#6B635A]">
                  <span>{{ i18n.t('cart.subtotal') }}</span>
                  <span class="font-medium text-[#1E1B18] tabular-nums">{{ cart().total | money }}</span>
                </div>
                <div class="flex justify-between text-[#6B635A]">
                  <span>مجموع التوصيل ({{ vendorGroups().length }} طرود)</span>
                  <span class="font-medium text-[#1E1B18] tabular-nums">{{ (vendorGroups().length * 7).toFixed(3) }} DT</span>
                </div>
                <div class="flex justify-between text-[#6B635A]">
                  <span>الطابع الجبائي (Timbre fiscal)</span>
                  <span class="font-medium text-[#1E1B18] tabular-nums">1.000 DT</span>
                </div>
                <div class="pt-3 border-t border-[#E6DED2] flex justify-between text-sm font-bold text-[#1E1B18]">
                  <span>{{ i18n.t('cart.total') }}</span>
                  <span class="text-[#B8432A] text-base tabular-nums">
                    {{ grandTotal() | money }}
                  </span>
                </div>
              </div>

              <a 
                routerLink="/checkout"
                class="block w-full py-3.5 px-4 rounded-xl text-center text-sm font-bold bg-[#B8432A] text-[#FFFFFF] hover:bg-[#B8432A]/90 transition-all shadow-sm"
              >
                {{ i18n.t('cart.checkout') }} &larr;
              </a>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class CartComponent implements OnInit {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);

  readonly cart = this.api.cart;

  ngOnInit() {
    this.api.getCart().subscribe();
  }

  readonly totalItems = computed(() => 
    this.cart().lines.reduce((acc, l) => acc + l.qty, 0)
  );

  readonly vendorGroups = computed(() => {
    const lines = this.cart().lines;
    const map = new Map<number, { vendorId: number; vendorName: string; lines: CartLine[] }>();

    for (const l of lines) {
      if (!map.has(l.vendorId)) {
        const name = l.vendorId === 1 ? 'معصرة الجنوب بصفاقس' : 'حرفيي جربة وقلالة';
        map.set(l.vendorId, { vendorId: l.vendorId, vendorName: name, lines: [] });
      }
      map.get(l.vendorId)!.lines.push(l);
    }
    return Array.from(map.values());
  });

  readonly grandTotal = computed(() => {
    const sub = parseFloat(this.cart().total);
    const shipping = this.vendorGroups().length * 7.0;
    const stamp = 1.0;
    return (sub + shipping + stamp).toFixed(3);
  });
}
