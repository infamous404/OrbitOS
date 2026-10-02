// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, startWith } from 'rxjs/operators';
import { brandText, getBranding, legalLinkKeys } from '@app/orbitos/branding';

/** Footer text and legal links (privacy, terms, accessibility, support) from branding.yml. */
@Component({
  selector: 'orbitos-legal-links',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (view().links.length) {
      <nav class="orbitos-legal-links" [attr.aria-label]="view().footer || null">
        @for (link of view().links; track link.url) {
          <a [href]="link.url" target="_blank" rel="noopener noreferrer">{{ link.label }}</a>
        }
      </nav>
    }
    @if (view().footer) {
      <small class="orbitos-footer-text">{{ view().footer }}</small>
    }
  `,
  styles: [`
    :host { display: flex; flex-direction: column; align-items: center; gap: 4px; padding-top: 16px; }
    .orbitos-legal-links { display: flex; flex-wrap: wrap; justify-content: center; gap: 4px 16px; font-size: 13px; }
    .orbitos-legal-links a { color: inherit; text-decoration: underline; }
    .orbitos-footer-text { opacity: 0.7; }
  `]
})
export class OrbitosLegalLinksComponent {

  private readonly translate = inject(TranslateService);

  readonly view = toSignal(this.translate.onLangChange.pipe(
    map(event => event.lang),
    startWith(this.translate.currentLang),
    map(lang => {
      const branding = getBranding();
      const links = legalLinkKeys
        .filter(key => !!branding.links?.[key])
        .map(key => ({url: branding.links[key], label: brandText(branding.linkLabels?.[key], lang) || key}));
      return {links, footer: brandText(branding.footer, lang)};
    })
  ), {requireSync: true});
}
