import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { I18nService, Lang } from './core/i18n.service';
import { ApiService } from './core/api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly i18n = inject(I18nService);
  readonly api = inject(ApiService);

  setLanguage(lang: Lang) {
    this.i18n.setLang(lang);
  }

  getCartCount(): number {
    return this.api.cart().lines.reduce((acc, l) => acc + l.qty, 0);
  }
}
