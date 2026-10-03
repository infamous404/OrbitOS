// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { ActionNotificationShow } from '@core/notification/notification.actions';
import { DialogService } from '@core/services/dialog.service';
import { CellActionDescriptor } from '@home/models/entity/entities-table-config.models';
import { User } from '@shared/models/user.model';
import { getCurrentAuthUser } from '@core/auth/auth.selectors';
import { Authority } from '@shared/models/authority.enum';

/**
 * Privacy requests from the users list: export everything about one account (JSON file),
 * or erase the account. See orbitos/docs/en/privacy-security.md.
 */
@Injectable({providedIn: 'root'})
export class OrbitosPrivacyService {

  constructor(private http: HttpClient,
              private translate: TranslateService,
              private dialogService: DialogService,
              private store: Store<AppState>) {
  }

  /** Two row actions for a users table; `refresh` reloads the table after an erasure. */
  cellActions(refresh: () => void): CellActionDescriptor<User>[] {
    return [
      {
        name: this.translate.instant('orbitos.privacy.export'),
        icon: 'download',
        isEnabled: () => true,
        onAction: ($event, user) => this.export($event, user)
      },
      {
        name: this.translate.instant('orbitos.privacy.erase'),
        icon: 'person_remove',
        isEnabled: user => user.id?.id !== getCurrentAuthUser(this.store)?.userId
          && user.authority !== Authority.SYS_ADMIN,
        onAction: ($event, user) => this.erase($event, user, refresh)
      }
    ];
  }

  private export($event: Event, user: User) {
    $event?.stopPropagation();
    this.http.get(`/api/orbitos/privacy/users/${user.id.id}/export`).subscribe(data => {
      const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `personal-data-${user.email}.json`;
      link.click();
      URL.revokeObjectURL(link.href);
      this.toast('orbitos.privacy.exported');
    });
  }

  private erase($event: Event, user: User, refresh: () => void) {
    $event?.stopPropagation();
    this.dialogService.confirm(
      this.translate.instant('orbitos.privacy.erase-title', {email: user.email}),
      this.translate.instant('orbitos.privacy.erase-text'),
      this.translate.instant('action.cancel'),
      this.translate.instant('orbitos.privacy.erase-confirm')
    ).subscribe(confirmed => {
      if (confirmed) {
        this.http.post(`/api/orbitos/privacy/users/${user.id.id}/erase`, null).subscribe(() => {
          this.toast('orbitos.privacy.erased');
          refresh();
        });
      }
    });
  }

  private toast(key: string) {
    this.store.dispatch(new ActionNotificationShow({
      message: this.translate.instant(key), type: 'success', duration: 3000, verticalPosition: 'top', horizontalPosition: 'right'
    }));
  }
}
