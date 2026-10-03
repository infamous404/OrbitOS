// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { marked } from 'marked';
import { SharedModule } from '@shared/shared.module';
import { OrbitosLanguageSwitcherComponent } from '@app/orbitos/language-switcher.component';

/** The guides, in menu order. Each is orbitos/docs/<en|fr>/<page>.md on the server. */
export const DOC_PAGES = ['index', 'quick-start', 'install', 'admin-guide', 'connect-devices', 'api',
  'privacy-security', 'branding', 'add-a-language', 'developer-guide'];

const PAGE_TITLES: Record<string, string> = {
  'index': 'orbitos.docs.pages.index',
  'quick-start': 'orbitos.docs.pages.quick-start',
  'install': 'orbitos.docs.pages.install',
  'admin-guide': 'orbitos.docs.pages.admin-guide',
  'connect-devices': 'orbitos.docs.pages.connect-devices',
  'api': 'orbitos.docs.pages.api',
  'privacy-security': 'orbitos.docs.pages.privacy-security',
  'branding': 'orbitos.docs.pages.branding',
  'add-a-language': 'orbitos.docs.pages.add-a-language',
  'developer-guide': 'orbitos.docs.pages.developer-guide'
};

/**
 * In-app documentation: renders the OrbitOS guides served by this server, in the current language.
 * Open to everyone (signed in or not); links between guides stay inside this page.
 */
@Component({
  selector: 'orbitos-docs-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SharedModule, RouterModule, TranslateModule, OrbitosLanguageSwitcherComponent],
  templateUrl: './docs-page.component.html',
  styleUrls: ['./docs-page.component.scss']
})
export class OrbitosDocsPageComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly translate = inject(TranslateService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly cd = inject(ChangeDetectorRef);

  readonly pages = DOC_PAGES;
  readonly titles = PAGE_TITLES;
  page = 'index';
  html: SafeHtml | null = null;
  missing = false;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const requested = params.get('page') || 'index';
      this.page = DOC_PAGES.includes(requested) ? requested : 'index';
      this.missing = !DOC_PAGES.includes(requested);
      this.load();
    });
  }

  get lang(): string {
    const fromQuery = this.route.snapshot.queryParamMap.get('lang');
    if (fromQuery === 'fr' || fromQuery === 'en') {
      return fromQuery;
    }
    return (this.translate.getCurrentLang() || 'en_US').startsWith('fr') ? 'fr' : 'en';
  }

  private async load() {
    this.html = null;
    try {
      const response = await fetch(`/api/noauth/orbitos/docs/${this.lang}/${this.page}`);
      const markdown = response.ok ? await response.text() : '';
      this.html = this.sanitizer.bypassSecurityTrustHtml(this.render(await marked.parse(markdown)));
    } catch {
      this.html = this.sanitizer.bypassSecurityTrustHtml('');
    }
    this.cd.markForCheck();
    const fragment = this.route.snapshot.fragment;
    if (fragment) {
      setTimeout(() => document.getElementById(fragment)?.scrollIntoView(), 50);
    }
  }

  /**
   * First-party content from this server's guides; still cleaned of scripts and event handlers.
   * Anchors: an HTML comment "<!-- #name -->" before a heading gives it id="name" (invisible on GitHub).
   */
  private render(html: string): string {
    const doc = new DOMParser().parseFromString(`<div>${html}</div>`, 'text/html');
    const root = doc.body.firstElementChild as HTMLElement;
    root.querySelectorAll('script, iframe, object, embed, style').forEach(el => el.remove());
    root.querySelectorAll('*').forEach(el => Array.from(el.attributes)
      .filter(a => a.name.startsWith('on') || (a.name === 'href' && /^\s*javascript:/i.test(a.value)))
      .forEach(a => el.removeAttribute(a.name)));
    const walker = doc.createTreeWalker(root, NodeFilter.SHOW_COMMENT);
    const anchors: Comment[] = [];
    while (walker.nextNode()) {
      anchors.push(walker.currentNode as Comment);
    }
    anchors.forEach(comment => {
      const id = comment.data.trim().match(/^#([a-z0-9-]+)$/)?.[1];
      let next = comment.nextSibling;
      while (next && next.nodeType !== Node.ELEMENT_NODE) {
        next = next.nextSibling;
      }
      if (id && next) {
        (next as Element).id = id;
      }
    });
    root.querySelectorAll('a[href]').forEach((a: HTMLAnchorElement) => {
      const href = a.getAttribute('href');
      const guide = href.match(/^(?:\.\.\/(en|fr)\/)?([a-z0-9-]+)\.md(#[a-z0-9-]+)?$/);
      if (guide) {
        const [, lang, page, anchor] = guide;
        a.setAttribute('href', `/docs/${page}${lang ? '?lang=' + lang : ''}${anchor || ''}`);
      } else if (/^https?:/i.test(href)) {
        a.setAttribute('target', '_blank');
        a.setAttribute('rel', 'noopener noreferrer');
      }
    });
    return root.innerHTML;
  }
}
