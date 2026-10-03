// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0

/*
 * Help links in the upstream interface point to the upstream product's website (about 150 places).
 * Instead of editing each one, this rewrites them, as they appear, to the matching OrbitOS guide on
 * this server (/docs/<page>). Works offline and never sends people to another product's site.
 */

const VENDOR_DOCS = /^https?:\/\/(www\.)?thingsboard\.io(\/|$)/i;

/** First match wins. Pages are files in orbitos/docs/<lang>/; "#x" is an anchor in that page. */
const TOPICS: [RegExp, string][] = [
  [/getting-started|helloworld|\/docs\/?$|^\/?$/, 'quick-start'],
  [/installation|upgrade|backup/, 'install'],
  [/mqtt|coap|http-api|lwm2m|snmp|sparkplug|connectivity|iot-gateway|gateway|ssl\/(mqtt|coap)/, 'connect-devices'],
  [/mail-settings|sms-provider|slack-settings|notification/, 'admin-guide#notifications'],
  [/security-settings|two-factor|oauth|audit|ssl\//, 'privacy-security'],
  [/rule-engine|rule-chain|\/nodes\//, 'admin-guide#rules'],
  [/\/api\/?$|rest-api|swagger|websocket/, 'api'],
  [/alarm/, 'admin-guide#alarms'],
  [/dashboard|widget|scada/, 'admin-guide#dashboards'],
  [/tenant|customer|user|device-profile|asset|entit/, 'admin-guide'],
];

export function orbitosDocsUrl(vendorUrl: string): string {
  let path = '';
  try {
    path = new URL(vendorUrl).pathname.toLowerCase();
  } catch {
    path = vendorUrl.toLowerCase();
  }
  const match = TOPICS.find(([pattern]) => pattern.test(path));
  const [page, anchor] = (match ? match[1] : 'index').split('#');
  return `/docs/${page}${anchor ? '#' + anchor : ''}`;
}

function rewrite(root: ParentNode) {
  root.querySelectorAll?.('a[href]').forEach((a: HTMLAnchorElement) => {
    const href = a.getAttribute('href');
    if (href && VENDOR_DOCS.test(href)) {
      a.setAttribute('href', orbitosDocsUrl(href));
      a.setAttribute('target', '_blank');
      a.removeAttribute('rel');
    }
  });
}

/** Call once at startup. */
export function installDocLinkRedirect() {
  const originalOpen = window.open.bind(window);
  window.open = ((url?: string | URL, target?: string, features?: string) => {
    const text = url ? url.toString() : '';
    return originalOpen(VENDOR_DOCS.test(text) ? orbitosDocsUrl(text) : url, target, features);
  }) as typeof window.open;

  const observer = new MutationObserver(mutations => {
    for (const mutation of mutations) {
      if (mutation.type === 'attributes') {
        rewrite(mutation.target.parentNode || document);
      }
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) {
          const element = node as Element;
          if (element.matches('a[href]')) {
            rewrite(element.parentNode || document);
          } else {
            rewrite(element);
          }
        }
      });
    }
  });
  const start = () => {
    rewrite(document);
    observer.observe(document.body, {childList: true, subtree: true, attributes: true, attributeFilter: ['href']});
  };
  if (document.body) {
    start();
  } else {
    document.addEventListener('DOMContentLoaded', start, {once: true});
  }
}
