// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { TranslateService } from '@ngx-translate/core';

/*
 * The server answers errors in English. Common messages are shown in the user's language;
 * anything not listed here is shown as the server sent it.
 */
const SERVER_MESSAGES: Array<[RegExp, string, string[]?]> = [
  [/^Invalid username or password\.?$/, 'orbitos.error.invalid-credentials'],
  [/^Authentication failed\.?$/, 'orbitos.error.authentication-failed'],
  [/^User account is not active\.?$/, 'orbitos.error.account-not-active'],
  [/^User account is locked due to security policy\.?$/, 'orbitos.error.account-locked'],
  [/^Token has expired\.?$/, 'orbitos.error.token-expired'],
  [/^User password expired!?$/, 'orbitos.error.password-expired'],
  [/^You don't have permission to perform this operation!?$/, 'orbitos.error.forbidden'],
  [/^Requested item wasn't found!?$/, 'orbitos.error.not-found'],
  [/^Invalid request body\.?$/, 'orbitos.error.invalid-request'],
  [/^Unable to connect$/, 'orbitos.error.unable-to-connect'],
  [/^Unauthorized!?$/, 'orbitos.error.unauthorized'],
  [/^Password was already used for the last (\d+) days\.?$/, 'orbitos.error.password-reused', ['days']],
  [/^Password must be (\d+) or more characters in length\.?$/, 'orbitos.error.password-too-short', ['count']],
  [/^Password must be no more than (\d+) characters in length\.?$/, 'orbitos.error.password-too-long', ['count']],
  [/^Password must contain (\d+) or more uppercase characters\.?$/, 'orbitos.error.password-uppercase', ['count']],
  [/^Password must contain (\d+) or more lowercase characters\.?$/, 'orbitos.error.password-lowercase', ['count']],
  [/^Password must contain (\d+) or more digit characters\.?$/, 'orbitos.error.password-digits', ['count']],
  [/^Password must contain (\d+) or more special characters\.?$/, 'orbitos.error.password-special', ['count']],
];

function translateLine(line: string, translate: TranslateService): string {
  const text = line.trim();
  for (const [pattern, key, names] of SERVER_MESSAGES) {
    const match = pattern.exec(text);
    if (match) {
      const params: { [name: string]: string } = {};
      (names || []).forEach((name, i) => params[name] = match[i + 1]);
      const translated = translate.instant(key, params);
      return translated !== key ? translated : line;
    }
  }
  return line;
}

/** Translates known server messages; keeps everything else unchanged. Multi-line messages are handled line by line. */
export function translateServerMessage(message: string, translate: TranslateService): string {
  if (typeof message !== 'string' || !message) {
    return message;
  }
  const separator = message.includes('<br/>') ? '<br/>' : '\n';
  return message.split(separator).map(line => translateLine(line, translate)).join(separator);
}
