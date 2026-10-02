// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { TranslationObject } from '@ngx-translate/core';

/** Merges OrbitOS strings over upstream ones. Nested sections merge key by key; OrbitOS wins. */
export function deepMergeTranslations(base: TranslationObject, overlay: TranslationObject): TranslationObject {
  const result: TranslationObject = {...base};
  for (const [key, value] of Object.entries(overlay || {})) {
    const current = result[key];
    if (isObject(value) && isObject(current)) {
      result[key] = deepMergeTranslations(current as TranslationObject, value as TranslationObject);
    } else {
      result[key] = value;
    }
  }
  return result;
}

function isObject(value: unknown): boolean {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}
