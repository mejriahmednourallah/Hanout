import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { I18nService } from '../../core/i18n.service';
import { MoneyPipe } from '../../shared/ui/money.pipe';

interface GovOption { id: number; nameAr: string; nameFr: string; delegations: { id: number; nameAr: string; localities: string[] }[] }

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, MoneyPipe],
  template: `
    <div class="max-w-3xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-serif font-bold text-[#1E1B18] mb-6">
        {{ i18n.t('checkout.title') }}
      </h1>

      <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-6 shadow-sm space-y-6">
        <!-- 1. Customer Phone with +216 region validation -->
        <div>
          <label class="block text-xs font-semibold text-[#1E1B18] mb-2">
            {{ i18n.t('checkout.phone') }} <span class="text-[#B8432A]">*</span>
          </label>
          <div class="relative flex rounded-xl border border-[#E6DED2] overflow-hidden bg-[#FBF8F3]">
            <span class="inline-flex items-center px-4 bg-[#F3EEE6] text-xs font-bold text-[#1E1B18] border-e border-[#E6DED2] select-none">
              🇹🇳 +216
            </span>
            <input 
              type="tel"
              [(ngModel)]="phone"
              placeholder="20 111 222"
              class="w-full py-3 px-4 bg-transparent text-sm font-medium text-[#1E1B18] tabular-nums focus:outline-none"
            />
          </div>
          <span class="text-[11px] text-[#6B635A] mt-1 block">رقم تونسي معتمد للتحقق وتسليم الطرد</span>
        </div>

        <!-- 2. Address Cascade: Governorate -> Delegation -> Locality -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
          <!-- Governorate -->
          <div>
            <label class="block text-xs font-semibold text-[#1E1B18] mb-1.5">
              {{ i18n.t('checkout.governorate') }} <span class="text-[#B8432A]">*</span>
            </label>
            <select 
              [(ngModel)]="selectedGovId"
              (ngModelChange)="onGovChange($event)"
              class="w-full py-2.5 px-3 rounded-xl border border-[#E6DED2] bg-[#FBF8F3] text-sm text-[#1E1B18] focus:outline-none focus:border-[#B8432A]"
            >
              @for (g of governorates; track g.id) {
                <option [value]="g.id">{{ i18n.isRtl() ? g.nameAr : g.nameFr }}</option>
              }
            </select>
          </div>

          <!-- Delegation -->
          <div>
            <label class="block text-xs font-semibold text-[#1E1B18] mb-1.5">
              {{ i18n.t('checkout.delegation') }} <span class="text-[#B8432A]">*</span>
            </label>
            <select 
              [(ngModel)]="selectedDelegationId"
              (ngModelChange)="onDelegationChange($event)"
              class="w-full py-2.5 px-3 rounded-xl border border-[#E6DED2] bg-[#FBF8F3] text-sm text-[#1E1B18] focus:outline-none focus:border-[#B8432A]"
            >
              @for (d of currentDelegations(); track d.id) {
                <option [value]="d.id">{{ d.nameAr }}</option>
              }
            </select>
          </div>

          <!-- Locality -->
          <div>
            <label class="block text-xs font-semibold text-[#1E1B18] mb-1.5">
              {{ i18n.t('checkout.locality') }} <span class="text-[#B8432A]">*</span>
            </label>
            <select 
              [(ngModel)]="selectedLocality"
              class="w-full py-2.5 px-3 rounded-xl border border-[#E6DED2] bg-[#FBF8F3] text-sm text-[#1E1B18] focus:outline-none focus:border-[#B8432A]"
            >
              @for (loc of currentLocalities(); track loc) {
                <option [value]="loc">{{ loc }}</option>
              }
            </select>
          </div>
        </div>

        <!-- Detailed Street Address -->
        <div>
          <label class="block text-xs font-semibold text-[#1E1B18] mb-1.5">
            {{ i18n.t('checkout.address') }} <span class="text-[#B8432A]">*</span>
          </label>
          <textarea 
            [(ngModel)]="streetAddress"
            rows="2"
            class="w-full py-2.5 px-3.5 rounded-xl border border-[#E6DED2] bg-[#FBF8F3] text-sm text-[#1E1B18] focus:outline-none focus:border-[#B8432A]"
            placeholder="مثال: نهج ابن خلدون، عمارة الأندلس، الطابق الثاني..."
          ></textarea>
        </div>

        <!-- 3. Payment Method Radio (COD vs Online) -->
        <div class="pt-4 border-t border-[#E6DED2]">
          <label class="block text-xs font-semibold text-[#1E1B18] mb-3">
            {{ i18n.t('checkout.payment_method') }}
          </label>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <!-- Cash On Delivery (COD) -->
            <label 
              class="flex items-start gap-3 p-4 rounded-xl border cursor-pointer transition-all"
              [ngClass]="paymentGateway === 'COD' ? 'border-[#B8432A] bg-[#B8432A]/5' : 'border-[#E6DED2] bg-[#FBF8F3] hover:bg-[#F3EEE6]'"
            >
              <input 
                type="radio" 
                name="gateway" 
                value="COD" 
                [(ngModel)]="paymentGateway"
                class="mt-1 text-[#B8432A] focus:ring-[#B8432A]"
              />
              <div>
                <span class="text-sm font-bold text-[#1E1B18] block">{{ i18n.t('checkout.cod') }}</span>
                <span class="text-xs text-[#6B635A]">ادفع نقداً لعون التوصيل عند استلام الطرد وفحصه</span>
              </div>
            </label>

            <!-- Online Payment (Konnect / Flouci) -->
            <label 
              class="flex items-start gap-3 p-4 rounded-xl border cursor-pointer transition-all"
              [ngClass]="paymentGateway === 'KONNECT' ? 'border-[#B8432A] bg-[#B8432A]/5' : 'border-[#E6DED2] bg-[#FBF8F3] hover:bg-[#F3EEE6]'"
            >
              <input 
                type="radio" 
                name="gateway" 
                value="KONNECT" 
                [(ngModel)]="paymentGateway"
                class="mt-1 text-[#B8432A] focus:ring-[#B8432A]"
              />
              <div>
                <span class="text-sm font-bold text-[#1E1B18] block">{{ i18n.t('checkout.online') }}</span>
                <span class="text-xs text-[#6B635A]">بطاقة بنكية، بطاقة بريدية e-Dinar، أو محفظة Flouci</span>
              </div>
            </label>
          </div>
        </div>

        <!-- Total and Confirm Action -->
        <div class="pt-6 border-t border-[#E6DED2] flex items-center justify-between">
          <div>
            <span class="text-xs text-[#6B635A]">إجمالي الطلب مع التوصيل:</span>
            <div class="text-xl font-bold text-[#B8432A] tabular-nums">
              {{ api.cart().total | money }}
            </div>
          </div>

          <button 
            type="button"
            (click)="onConfirmOrder()"
            [disabled]="isProcessing()"
            class="py-3.5 px-8 rounded-xl text-sm font-bold bg-[#B8432A] text-[#FFFFFF] hover:bg-[#B8432A]/90 transition-all shadow-sm disabled:opacity-50 flex items-center gap-2"
          >
            @if (isProcessing()) {
              <span class="w-4 h-4 border-2 border-[#FFFFFF] border-t-transparent rounded-full animate-spin"></span>
            }
            {{ i18n.t('checkout.confirm_button') }}
          </button>
        </div>
      </div>

      <!-- 4. OTP Verification Modal (Triggered by Risk Engine when needed) -->
      @if (showOtpModal()) {
        <div class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 backdrop-blur-[2px]">
          <div class="bg-[#FFFFFF] border border-[#E6DED2] rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-4">
            <div class="text-center space-y-1">
              <span class="w-10 h-10 mx-auto rounded-full bg-[#B8432A]/10 text-[#B8432A] flex items-center justify-center text-lg">
                🔐
              </span>
              <h3 class="text-lg font-bold text-[#1E1B18]">{{ i18n.t('checkout.otp_title') }}</h3>
              <p class="text-xs text-[#6B635A]">{{ i18n.t('checkout.otp_desc') }} ({{ phone }})</p>
            </div>

            <!-- 6-digit OTP input -->
            <div class="flex justify-center gap-2 pt-2">
              <input 
                type="text" 
                maxlength="6"
                [(ngModel)]="otpCode"
                placeholder="123456"
                class="w-48 text-center tracking-[0.5em] py-3 text-2xl font-bold rounded-xl border-2 border-[#E6DED2] bg-[#FBF8F3] tabular-nums focus:outline-none focus:border-[#B8432A]"
              />
            </div>

            <div class="flex gap-2 pt-2">
              <button 
                type="button"
                (click)="verifyOtpAndComplete()"
                class="flex-1 py-3 text-sm font-bold rounded-xl bg-[#566B2F] text-[#FFFFFF] hover:bg-[#566B2F]/90"
              >
                {{ i18n.t('checkout.verify_otp') }}
              </button>
              <button 
                type="button"
                (click)="showOtpModal.set(false)"
                class="py-3 px-4 text-xs font-semibold rounded-xl border border-[#E6DED2] bg-[#FFFFFF]"
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
export class CheckoutComponent {
  readonly i18n = inject(I18nService);
  readonly api = inject(ApiService);
  private readonly router = inject(Router);

  phone: string = '+216 20 111 222';
  selectedGovId: number = 1;
  selectedDelegationId: number = 101;
  selectedLocality: string = 'باب سويقة';
  streetAddress: string = 'نهج الحبيب بورقيبة، تونس';
  paymentGateway: string = 'COD';

  readonly isProcessing = signal<boolean>(false);
  readonly showOtpModal = signal<boolean>(false);
  otpCode: string = '';

  // Seeded Tunisian geography cascade
  readonly governorates: GovOption[] = [
    {
      id: 1, nameAr: 'تونس', nameFr: 'Tunis',
      delegations: [
        { id: 101, nameAr: 'تونس المدينة', localities: ['باب سويقة', 'باب بنات', 'القصبة', 'الحفصية'] },
        { id: 102, nameAr: 'المرسى', localities: ['المرسى الشاطئ', 'قمرت', 'سيدي بوسعيد'] }
      ]
    },
    {
      id: 2, nameAr: 'صفاقس', nameFr: 'Sfax',
      delegations: [
        { id: 201, nameAr: 'صفاقس المدينة', localities: ['الناصرية', 'باب بحر', 'طريق المهدية'] },
        { id: 202, nameAr: 'طينة', localities: ['طينة المركز', 'سيدي سالم'] }
      ]
    },
    {
      id: 4, nameAr: 'نابل', nameFr: 'Nabeul',
      delegations: [
        { id: 401, nameAr: 'نابل المدينة', localities: ['سيدي عاشور', 'بئر بورقبة', 'الحمامات'] }
      ]
    },
    {
      id: 6, nameAr: 'توزر', nameFr: 'Tozeur',
      delegations: [
        { id: 601, nameAr: 'توزر المدينة', localities: ['الواحة', 'بلاد الحضر', 'حومة الهواونة'] }
      ]
    }
  ];

  readonly currentDelegations = signal(this.governorates[0].delegations);
  readonly currentLocalities = signal(this.governorates[0].delegations[0].localities);

  onGovChange(govId: number) {
    const gov = this.governorates.find(g => g.id == govId);
    if (gov) {
      this.currentDelegations.set(gov.delegations);
      this.selectedDelegationId = gov.delegations[0]?.id || 0;
      this.onDelegationChange(this.selectedDelegationId);
    }
  }

  onDelegationChange(delId: number) {
    const del = this.currentDelegations().find(d => d.id == delId);
    if (del) {
      this.currentLocalities.set(del.localities);
      this.selectedLocality = del.localities[0] || '';
    }
  }

  onConfirmOrder() {
    this.isProcessing.set(true);

    // If order value is high, trigger OTP verification dialog as simulated by risk engine
    const total = parseFloat(this.api.cart().total);
    if (total > 150.0) {
      this.isProcessing.set(false);
      this.showOtpModal.set(true);
      return;
    }

    this.completeCheckout();
  }

  verifyOtpAndComplete() {
    this.showOtpModal.set(false);
    this.completeCheckout();
  }

  private completeCheckout() {
    this.isProcessing.set(true);
    const cmd = {
      phone: this.phone,
      shippingAddress: `${this.selectedLocality}, ${this.streetAddress}`,
      governorateId: this.selectedGovId,
      paymentGateway: this.paymentGateway
    };

    const idempotencyKey = 'CHK-' + Date.now();
    this.api.checkout(cmd, idempotencyKey).subscribe({
      next: (order) => {
        this.isProcessing.set(false);
        this.router.navigate(['/orders']);
      },
      error: () => {
        this.isProcessing.set(false);
        this.router.navigate(['/orders']);
      }
    });
  }
}
