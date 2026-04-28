/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCall, SmartField, SmartFieldLookupResult} from '../../../../index';

export class SpecSmartField extends SmartField<number> {
  declare _userWasTyping: boolean;
  declare _lastSearchText: string;
  declare _pendingOpenPopup: boolean;

  override _readSearchText(): string {
    return super._readSearchText();
  }

  override _onFieldKeyDown(event: JQuery.KeyDownEvent) {
    super._onFieldKeyDown(event);
  }

  override _onFieldKeyUp(event: JQuery.KeyUpEvent) {
    super._onFieldKeyUp(event);
  }

  override _acceptByText(sync: boolean, searchText: string) {
    super._acceptByText(sync, searchText);
  }

  override _lookupByTextOrAll(browse?: boolean, searchText?: string, searchAlways?: boolean): JQuery.Promise<any> {
    return super._lookupByTextOrAll(browse, searchText, searchAlways);
  }

  override _lookupByTextOrAllDone(result: SmartFieldLookupResult<number>) {
    super._lookupByTextOrAllDone(result);
  }

  override _executeLookup(lookupCall: LookupCall<number>, abortExisting?: boolean): JQuery.Promise<SmartFieldLookupResult<number>> {
    return super._executeLookup(lookupCall, abortExisting);
  }

  override _formatValue(value: number): string | JQuery.Promise<string> {
    return super._formatValue(value);
  }

  override _copyValuesFromField(otherField: SmartField<number>) {
    super._copyValuesFromField(otherField);
  }

  override _getLastSearchText(): string {
    return super._getLastSearchText();
  }
}
