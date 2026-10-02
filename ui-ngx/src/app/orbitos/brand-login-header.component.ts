// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, startWith } from 'rxjs/operators';
import { brandText, getBranding } from '@app/orbitos/branding';

/** Login page title and subtitle from branding.yml, in the current language. */
@Component({
  selector: 'orbitos-login-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (texts().title) {
      <h1 class="orbitos-login-title">{{ texts().title }}</h1>
    }
    @if (texts().subtitle) {
      <p class="orbitos-login-subtitle">{{ texts().subtitle }}</p>
    }
  `,
  styles: [`
    :host { display: block; text-align: center; padding: 0 8px 8px; }
    .orbitos-login-title { font-size: 20px; font-weight: 500; line-height: 28px; margin: 8px 0 4px; }
    .orbitos-login-subtitle { font-size: 14px; line-height: 20px; margin: 0; opacity: 0.8; }
  `]
})
export class OrbitosLoginHeaderComponent {

  private readonly translate = inject(TranslateService);

  readonly texts = toSignal(this.translate.onLangChange.pipe(
    map(event => event.lang),
    startWith(this.translate.currentLang),
    map(lang => {
      const login = getBranding().login || {};
      return {title: brandText(login.title, lang), subtitle: brandText(login.subtitle, lang)};
    })
  ), {requireSync: true});
}
