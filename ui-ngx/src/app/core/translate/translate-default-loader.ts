// SPDX-FileCopyrightText: Copyright The Thingsboard Authors
// SPDX-License-Identifier: Apache-2.0
import { translationFiles } from '@app/orbitos/i18n';
import { TranslateLoader, TranslationObject } from '@ngx-translate/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { deepMergeTranslations } from '@app/orbitos/translations';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TranslateDefaultLoader implements TranslateLoader {

  constructor(private http: HttpClient) {

  }

  getTranslation(lang: string): Observable<TranslationObject> {
    // OrbitOS: upstream base language, then OrbitOS strings, then regional differences (fr_CA, en_CA).
    return forkJoin(translationFiles(lang).map((file, index) => {
      const request = this.http.get<TranslationObject>(file);
      return index === 0 ? request : request.pipe(catchError(() => of({})));
    })).pipe(map(files => files.reduce((merged, file) => deepMergeTranslations(merged, file), {})));
  }
}
