import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { App } from './app';
import { MoneyPipe } from './shared/ui/money.pipe';
import { I18nService } from './core/i18n.service';

describe('Hanout Frontend Application', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        MoneyPipe
      ]
    }).compileComponents();
  });

  it('should create the Hanout shell component', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the brand title and Tunisian identity badge', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('حانوت');
    expect(compiled.textContent).toContain('مساومة تونسية');
  });

  it('should switch languages between Arabic, French, and English', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const i18n = TestBed.inject(I18nService);

    expect(i18n.currentLang()).toBe('ar');
    expect(i18n.isRtl()).toBe(true);

    app.setLanguage('fr');
    expect(i18n.currentLang()).toBe('fr');
    expect(i18n.isRtl()).toBe(false);

    app.setLanguage('en');
    expect(i18n.currentLang()).toBe('en');
    expect(i18n.isRtl()).toBe(false);
  });

  it('MoneyPipe should format millimes with 3 decimals and currency symbol', () => {
    const i18n = TestBed.inject(I18nService);
    const pipe = TestBed.inject(MoneyPipe);

    i18n.setLang('ar');
    expect(pipe.transform('12.500')).toBe('12.500 د.ت');
    expect(pipe.transform('125.000')).toBe('125.000 د.ت');
    expect(pipe.transform(18.5)).toBe('18.500 د.ت');

    i18n.setLang('fr');
    expect(pipe.transform('12.500')).toBe('12.500 DT');
  });
});
