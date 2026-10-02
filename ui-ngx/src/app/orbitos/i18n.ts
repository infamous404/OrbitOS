// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0

/*
 * OrbitOS languages: base languages (en_US, fr_FR) plus regional variants (en_CA, fr_CA)
 * that only hold the strings that differ. Also sets date and number formats per language.
 */
import { getLocaleNumberSymbol, NumberSymbol, registerLocaleData } from '@angular/common';
import localeEnCa from '@angular/common/locales/en-CA';
import localeFr from '@angular/common/locales/fr';
import localeFrCa from '@angular/common/locales/fr-CA';
import _moment from 'moment';
import 'moment/locale/en-ca';
import 'moment/locale/fr';
import 'moment/locale/fr-ca';
import { environment as env } from '@env/environment';

registerLocaleData(localeEnCa, 'en-CA');
registerLocaleData(localeFr, 'fr');
registerLocaleData(localeFrCa, 'fr-CA');
// Importing moment locales changes moment's global locale; the user's language is applied later.
_moment.locale('en');

const STORAGE_KEY = 'orbitos.lang';
const BASE_LANGUAGES: { [language: string]: string } = {en: 'en_US', fr: 'fr_FR'};

/** The full upstream file that a language builds on: fr_CA -> fr_FR, en_CA -> en_US. */
export function baseLanguage(lang: string): string {
  return BASE_LANGUAGES[lang.split('_')[0]] ?? lang;
}

/** Files merged in order, later ones win. */
export function translationFiles(lang: string): string[] {
  const base = baseLanguage(lang);
  const files = [`/assets/locale/locale.constant-${base}.json`, `/assets/orbitos/locale/${base}.json`];
  if (lang !== base) {
    files.push(`/assets/orbitos/locale/${lang}.json`);
  }
  return files;
}

/** Angular locale id for a language code: fr_CA -> fr-CA, fr_FR -> fr, en_US -> en-US. */
export function angularLocale(lang: string): string {
  switch (lang) {
    case 'fr_FR':
      return 'fr';
    case 'fr_CA':
      return 'fr-CA';
    case 'en_CA':
      return 'en-CA';
    default:
      return 'en-US';
  }
}

/** Same matching as the upstream detection: exact code, then same language, then default. */
export function detectLanguage(tag: string): string {
  const langs: string[] = env.supportedLangs;
  const code = (tag || '').replace('-', '_');
  if (langs.includes(code)) {
    return code;
  }
  const language = code.split('_')[0];
  return langs.find(l => l.split('_')[0] === language) ?? env.defaultLang;
}

export function storedLanguage(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

export function storeLanguage(lang: string) {
  try {
    localStorage.setItem(STORAGE_KEY, lang);
  } catch {
    // storage blocked: the language still applies for this page
  }
}

/** Language used for dates and numbers on this page load (Angular's locale cannot change later). */
export const bootLanguage: string = (() => {
  const stored = storedLanguage();
  if (stored && env.supportedLangs.includes(stored)) {
    return stored;
  }
  return detectLanguage(navigator.language);
})();

export function bootLocaleId(): string {
  return angularLocale(bootLanguage);
}

/**
 * Called whenever the UI language is applied. Dates and numbers need a page reload to follow
 * a language change; reload once, and never loop.
 */
export function onLanguageApplied(lang: string) {
  storeLanguage(lang);
  if (angularLocale(lang) !== bootLocaleId()) {
    const guard = 'orbitos.lang-reload';
    try {
      if (sessionStorage.getItem(guard) !== lang) {
        sessionStorage.setItem(guard, lang);
        window.location.reload();
      }
    } catch {
      // no session storage: keep the page as it is
    }
  }
}

const decimalSeparator = getLocaleNumberSymbol(bootLocaleId(), NumberSymbol.Decimal);

/** Shows numbers with the language's decimal separator (12.5 -> 12,5 in French). Display only. */
export function localizeDecimal(formatted: string): string {
  if (decimalSeparator === '.' || typeof formatted !== 'string') {
    return formatted;
  }
  return formatted.replace(/^(-?\d+)\.(\d+)/, `$1${decimalSeparator}$2`);
}
