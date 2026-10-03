// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { getCurrentAuthState } from '@core/auth/auth.selectors';
import { environment as env } from '@env/environment';
import { storeLanguage } from '@app/orbitos/i18n';
import { ActionSettingsChangeLanguage } from '@core/settings/settings.actions';

/**
 * Language menu. The choice is saved in the user's profile (when signed in) and in this browser,
 * then the page reloads so text, dates and numbers all switch together.
 */
@Component({
  selector: 'orbitos-language-switcher',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule, MatMenuModule, MatTooltipModule, TranslateModule],
  template: `
    <button mat-button type="button" class="orbitos-language-button" [class.dark]="dark"
            [matMenuTriggerFor]="menu"
            [attr.aria-label]="'orbitos.language' | translate"
            [matTooltip]="'orbitos.language' | translate">
      <mat-icon>language</mat-icon>
      <span class="orbitos-language-code">{{ shortName(current) }}</span>
    </button>
    <mat-menu #menu="matMenu">
      @for (lang of languages; track lang) {
        <button mat-menu-item type="button" [attr.lang]="lang.replace('_', '-')"
                [attr.aria-current]="lang === current ? 'true' : null" (click)="select(lang)">
          @if (lang === current) {
            <mat-icon>check</mat-icon>
          } @else {
            <mat-icon></mat-icon>
          }
          <span>{{ 'language.locales.' + lang | translate }}</span>
        </button>
      }
    </mat-menu>
  `,
  styles: [`
    .orbitos-language-button { color: inherit; min-width: 0; }
    .orbitos-language-code { font-weight: 500; letter-spacing: 0.5px; }
  `]
})
export class OrbitosLanguageSwitcherComponent {

  /** Use on light backgrounds (keeps inherited color by default). */
  @Input() dark = false;

  private readonly translate = inject(TranslateService);
  private readonly http = inject(HttpClient);
  private readonly store = inject(Store<AppState>);

  // Canada first in the menu; detection order is defined in assets/orbitos/languages.json.
  readonly languages: string[] = [...env.supportedLangs].sort((a, b) =>
    Number(b.endsWith('_CA')) - Number(a.endsWith('_CA')) || a.localeCompare(b));

  get current(): string {
    return this.translate.getCurrentLang() || env.defaultLang;
  }

  shortName(lang: string): string {
    const [language, region] = (lang || '').split('_');
    return region ? `${language.toUpperCase()}-${region}` : language.toUpperCase();
  }

  select(lang: string) {
    if (lang === this.current) {
      return;
    }
    storeLanguage(lang);
    if (getCurrentAuthState(this.store)?.isAuthenticated) {
      // Save to the profile first: applying the language can reload the page at once, and after
      // a reload the profile language wins.
      this.http.get<any>('/api/auth/user').subscribe({
        next: user => {
          user.additionalInfo = {...(user.additionalInfo || {}), lang};
          this.http.post('/api/user', user).subscribe({
            next: () => this.apply(lang),
            error: () => this.apply(lang)
          });
        },
        error: () => this.apply(lang)
      });
    } else {
      this.apply(lang);
    }
  }

  private apply(lang: string) {
    // Also replaces the language remembered from the last signed-in user (upstream settings),
    // which otherwise wins on the login and setup pages after a sign-out.
    this.store.dispatch(new ActionSettingsChangeLanguage({userLang: lang}));
    window.location.reload();
  }
}
