// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0

/*
 * Runtime branding. Loaded once before Angular starts, from the server's branding folder
 * (/api/noauth/branding). If the call fails, the built-in OrbitOS defaults are used.
 */
import { environment as env } from '@env/environment';

export type LocalizedText = { [lang: string]: string };

export interface OrbitosBranding {
  name: string;
  colors: { [key: string]: string };
  login: { title?: LocalizedText; subtitle?: LocalizedText };
  footer: LocalizedText;
  links: { website?: string; privacy?: string; terms?: string; accessibility?: string; support?: string };
  linkLabels: { [key: string]: LocalizedText };
  logoUrl?: string;
  faviconUrl?: string;
}

export const legalLinkKeys = ['privacy', 'terms', 'accessibility', 'support'] as const;

const defaultBranding: OrbitosBranding = {
  name: 'OrbitOS',
  colors: {},
  login: {},
  footer: {},
  links: {},
  linkLabels: {}
};

let branding: OrbitosBranding = defaultBranding;

export function getBranding(): OrbitosBranding {
  return branding;
}

export function brandLogoUrl(): string {
  return branding.logoUrl || 'assets/logo_title_white.svg';
}

/**
 * Picks the text for a language: exact match (fr_CA), then base language (fr),
 * then English, then any value. Replaces {brandName} and {year}.
 */
export function brandText(value: LocalizedText | undefined, lang: string): string {
  if (!value) {
    return '';
  }
  const code = (lang || 'en_US').replace('-', '_');
  const base = code.split('_')[0];
  const text = value[code] ?? value[base] ?? value.en ?? value.en_US ?? Object.values(value)[0] ?? '';
  return text
    .replace(/\{brandName\}/g, branding.name)
    .replace(/\{year\}/g, String(new Date().getFullYear()));
}

export async function loadBranding(): Promise<void> {
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 5000);
    const response = await fetch('/api/noauth/branding', {credentials: 'same-origin', signal: controller.signal});
    clearTimeout(timer);
    if (response.ok) {
      const loaded = await response.json();
      branding = {...defaultBranding, ...loaded};
    }
  } catch (e) {
    console.warn('Branding could not be loaded, using defaults', e);
  }
  applyBranding();
}

function applyBranding() {
  (env as { appTitle: string }).appTitle = branding.name;
  document.title = branding.name;
  if (branding.faviconUrl) {
    const icon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
    if (icon) {
      icon.type = '';
      icon.href = branding.faviconUrl;
    }
  }
}
