// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
import { Router } from '@angular/router';

/** On a new server nobody can sign in yet: send the visitor from the login page to the setup page. */
export function redirectToSetupIfNeeded(router: Router): void {
  fetch('/api/noauth/orbitos/setup', {headers: {Accept: 'application/json'}})
    .then(response => response.ok ? response.json() : null)
    .then(status => {
      if (status?.required) {
        router.navigateByUrl('/setup');
      }
    })
    .catch(() => { /* server unreachable: the login page stays */ });
}
