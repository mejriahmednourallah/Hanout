import { Component, input, signal, OnInit, OnDestroy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-countdown-timer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex items-center gap-2.5 p-3 rounded-lg border border-[#E6DED2] bg-[#FBF8F3]">
      @if (!isExpired()) {
        <!-- SVG Progress Ring -->
        <div class="relative w-9 h-9 flex items-center justify-center shrink-0">
          <svg class="w-full h-full -rotate-90" viewBox="0 0 36 36">
            <circle
              cx="18" cy="18" r="15"
              fill="none"
              stroke="#E6DED2"
              stroke-width="3"
            />
            <circle
              cx="18" cy="18" r="15"
              fill="none"
              stroke="#D49A2A"
              stroke-width="3"
              stroke-dasharray="94.2"
              [style.stroke-dashoffset]="dashOffset()"
              stroke-linecap="round"
              class="transition-all duration-1000 ease-linear"
            />
          </svg>
          <span class="absolute text-[10px] font-semibold text-[#1E1B18] tabular-nums">
            {{ secondsLeft() }}s
          </span>
        </div>

        <div class="flex flex-col">
          <span class="text-xs font-semibold text-[#1E1B18]">السعر محجوز لك / Prix verrouillé</span>
          <span class="text-xs text-[#6B635A] tabular-nums">
            الوقت المتبقي: {{ formattedTime() }}
          </span>
        </div>
      } @else {
        <!-- Calm expiration explanation without aggressive error styling -->
        <div class="flex items-center gap-2 text-xs text-[#6B635A]">
          <span class="w-2 h-2 rounded-full bg-[#D49A2A]"></span>
          <span>انتهت مهلة حجز هذا السعر. تم تحرير المخزون ويمكنك تقديم عرض جديد بكل سهولة.</span>
        </div>
      }
    </div>
  `
})
export class CountdownTimerComponent implements OnInit, OnDestroy {
  readonly expiresAt = input.required<string | undefined>();
  readonly totalSeconds = input<number>(1800); // default 30 mins (1800s)

  readonly remaining = signal<number>(1800);
  private timerId?: any;

  readonly isExpired = computed(() => this.remaining() <= 0);

  readonly secondsLeft = computed(() => this.remaining() % 60);

  readonly formattedTime = computed(() => {
    const mins = Math.floor(this.remaining() / 60);
    const secs = this.remaining() % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  });

  readonly dashOffset = computed(() => {
    const total = this.totalSeconds();
    const curr = Math.max(0, this.remaining());
    const pct = curr / total;
    return (94.2 * (1 - pct)).toFixed(1);
  });

  ngOnInit() {
    this.updateRemaining();
    this.timerId = setInterval(() => this.updateRemaining(), 1000);
  }

  ngOnDestroy() {
    if (this.timerId) clearInterval(this.timerId);
  }

  private updateRemaining() {
    const exp = this.expiresAt();
    if (!exp) {
      this.remaining.set(0);
      return;
    }
    const diff = Math.floor((new Date(exp).getTime() - Date.now()) / 1000);
    this.remaining.set(Math.max(0, diff));
  }
}
