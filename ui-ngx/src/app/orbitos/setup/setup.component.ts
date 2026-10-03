// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SharedModule } from '@shared/shared.module';
import { AuthService } from '@core/auth/auth.service';
import { OrbitosLanguageSwitcherComponent } from '@app/orbitos/language-switcher.component';
import { getBranding } from '@app/orbitos/branding';
import { environment as env } from '@env/environment';

interface SetupResult {
  deviceId: string | null;
  accessToken: string | null;
  dashboardId: string | null;
  sysadminEmail: string;
  sysadminPassword: string;
}

/** Error codes sent by the server, and the text shown for each. */
const ERROR_TEXT: Record<string, string> = {
  'code': 'orbitos.setup.error.code',
  'language': 'orbitos.setup.error.failed',
  'organization': 'orbitos.setup.error.organization',
  'email': 'orbitos.setup.error.email',
  'sysadmin-email': 'orbitos.setup.error.sysadmin-email',
  'same-email': 'orbitos.setup.error.same-email',
  'password': 'orbitos.setup.error.password',
  'device': 'orbitos.setup.error.device',
  'already-done': 'orbitos.setup.error.already-done',
  'failed': 'orbitos.setup.error.failed'
};

export const MIN_PASSWORD = 12;

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirm')?.value;
  return password && confirm && password !== confirm ? {mismatch: true} : null;
}

/**
 * First-run setup page: shown once, on a new server, before anyone can sign in.
 * Steps: language and setup code, accounts, first device; then a test value and the dashboard.
 */
@Component({
  selector: 'orbitos-setup',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SharedModule, OrbitosLanguageSwitcherComponent],
  templateUrl: './setup.component.html',
  styleUrls: ['./setup.component.scss']
})
export class OrbitosSetupComponent implements OnInit {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly authService = inject(AuthService);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly fb = inject(FormBuilder);

  readonly brandName = getBranding().name;
  readonly minPassword = MIN_PASSWORD;
  readonly origin = window.location.origin;

  step = 1;
  busy = false;
  error: string | null = null;
  result: SetupResult | null = null;
  testSent = false;
  copied = false;
  passwordCopied = false;

  readonly codeForm = this.fb.group({
    setupCode: ['', [Validators.required, Validators.minLength(12)]]
  });

  readonly accountForm = this.fb.group({
    organizationName: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(MIN_PASSWORD), Validators.maxLength(72)]],
    confirm: ['', [Validators.required]],
    sysadminEmail: ['sysadmin@local.invalid', [Validators.required, Validators.email]]
  }, {validators: passwordsMatch});

  readonly deviceForm = this.fb.group({
    createDevice: [true],
    deviceName: ['', [Validators.maxLength(255)]]
  });

  ngOnInit() {
    this.http.get<{required: boolean}>('/api/noauth/orbitos/setup').subscribe({
      next: status => {
        if (!status.required) {
          this.router.navigateByUrl('/login');
        }
      }
    });
    this.deviceForm.patchValue({deviceName: this.translate.instant('orbitos.setup.device-name-default')});
  }

  get language(): string {
    return this.translate.getCurrentLang() || env.defaultLang;
  }

  get curlCommand(): string {
    const token = this.result?.accessToken ?? '';
    return `curl -X POST ${this.origin}/api/v1/${token}/telemetry -H "Content-Type: application/json" -d "{\\"temperature\\": 21.5, \\"humidity\\": 40}"`;
  }

  next() {
    const form = this.step === 1 ? this.codeForm : this.accountForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    this.error = null;
    this.step++;
  }

  back() {
    this.error = null;
    this.step--;
  }

  finish() {
    if (this.deviceForm.invalid || this.busy) {
      return;
    }
    const account = this.accountForm.getRawValue();
    const device = this.deviceForm.getRawValue();
    this.busy = true;
    this.error = null;
    this.http.post<SetupResult>('/api/noauth/orbitos/setup', {
      setupCode: this.codeForm.getRawValue().setupCode,
      language: this.language,
      organizationName: account.organizationName,
      email: account.email,
      password: account.password,
      sysadminEmail: account.sysadminEmail,
      deviceName: device.createDevice ? device.deviceName : ''
    }).subscribe({
      next: result => {
        this.busy = false;
        this.result = result;
        this.step = 4;
        this.cd.markForCheck();
      },
      error: err => {
        this.busy = false;
        const code: string = err?.error?.error ?? 'failed';
        this.error = ERROR_TEXT[code] ?? ERROR_TEXT['failed'];
        if (code === 'code') {
          this.step = 1;
        } else if (['organization', 'email', 'sysadmin-email', 'same-email', 'password'].includes(code)) {
          this.step = 2;
        }
        this.cd.markForCheck();
      }
    });
  }

  sendTestValue() {
    const token = this.result?.accessToken;
    if (!token) {
      return;
    }
    const value = {
      temperature: Math.round((18 + Math.random() * 8) * 10) / 10,
      humidity: Math.round(35 + Math.random() * 20)
    };
    // Plain fetch: this is the device API (token in the URL), not a signed-in user call,
    // so it must not go through the app's HTTP interceptor (which expects a signed-in user).
    fetch(`/api/v1/${encodeURIComponent(token)}/telemetry`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(value)
    }).then(response => {
      this.testSent = response.ok;
      this.error = response.ok ? null : 'orbitos.setup.error.test-value';
    }).catch(() => {
      this.error = 'orbitos.setup.error.test-value';
    }).finally(() => this.cd.markForCheck());
  }

  copyCommand() {
    navigator.clipboard?.writeText(this.curlCommand).then(() => {
      this.copied = true;
      this.cd.markForCheck();
    });
  }

  copyPassword() {
    navigator.clipboard?.writeText(this.result?.sysadminPassword ?? '').then(() => {
      this.passwordCopied = true;
      this.cd.markForCheck();
    });
  }

  signIn() {
    const account = this.accountForm.getRawValue();
    if (this.result?.dashboardId) {
      this.authService.redirectUrl = `/dashboards/${this.result.dashboardId}`;
    }
    this.busy = true;
    this.authService.login({username: account.email, password: account.password}).subscribe({
      error: () => {
        this.busy = false;
        this.router.navigateByUrl('/login');
      }
    });
  }
}
