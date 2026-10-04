// SPDX-FileCopyrightText: Copyright The Thingsboard Authors
// SPDX-License-Identifier: Apache-2.0
import { Component, Input, OnInit } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';
import { AppState } from '@core/core.state';
import { Store } from '@ngrx/store';
import { UrlTree } from '@angular/router';
import { getCurrentAuthState } from '@core/auth/auth.selectors';
import { UrlHolder } from '@shared/pipe/image.pipe';
import { brandLogoUrl, getBranding } from '@app/orbitos/branding';

@Component({
    selector: 'tb-logo',
    templateUrl: './logo.component.html',
    styleUrls: ['./logo.component.scss'],
    standalone: false
})
export class LogoComponent implements OnInit {

  @Input()
  src: string | UrlHolder = brandLogoUrl();

  @Input()
  link: string | UrlTree;

  @Input()
  target: string = null;

  isExternal = false;

  // OrbitOS: screen readers say the brand name, not "logo".
  readonly brandName = getBranding().name;

  constructor(private authService: AuthService,
              private store: Store<AppState>) {
  }

  ngOnInit() {
    if (!this.link) {
      const authState = getCurrentAuthState(this.store);
      // OrbitOS: the logo is also shown before sign-in (login, setup, docs pages).
      this.link = authState?.authUser ? this.authService.defaultUrl(true, authState) : '/';
    }
    if (typeof this.link === 'string' && this.link.startsWith('http')) {
      this.isExternal = true;
    }
  }
}
