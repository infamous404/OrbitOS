// SPDX-FileCopyrightText: Copyright The Thingsboard Authors
// SPDX-License-Identifier: Apache-2.0
import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from '@app/app.module';
import { environment } from '@env/environment';
import { loadBranding } from '@app/orbitos/branding';

import $ from 'jquery';

(window as any).jQuery = $;
(window as any).$ = $;

if (environment.production) {
  enableProdMode();
}

// OrbitOS: apply runtime branding (name, logo, favicon) before the app starts.
loadBranding().finally(() =>
  platformBrowserDynamic().bootstrapModule(AppModule)
    .catch(err => console.error(err)));
