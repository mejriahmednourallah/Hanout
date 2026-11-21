import { Pipe, PipeTransform, inject } from '@angular/core';
import { I18nService } from '../../core/i18n.service';

@Pipe({
  name: 'money',
  standalone: true
})
export class MoneyPipe implements PipeTransform {
  private readonly i18n = inject(I18nService);

  transform(value: string | number | null | undefined): string {
    if (value === null || value === undefined || value === '') {
      return '0.000 DT';
    }

    const num = typeof value === 'string' ? parseFloat(value) : value;
    const formatted = num.toFixed(3);

    return this.i18n.isRtl() ? `${formatted} د.ت` : `${formatted} DT`;
  }
}
