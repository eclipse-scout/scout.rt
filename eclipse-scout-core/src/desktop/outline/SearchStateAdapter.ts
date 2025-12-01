/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ModelAdapter, SearchState} from '../../index';

export class SearchStateAdapter extends ModelAdapter {
  declare widget: SearchState;

  constructor() {
    super();
    this._addRemoteProperties(['resultCount', 'limited', 'pending']);
  }

  protected _sendResultCount(resultCount: number) {
    this._send('property', {resultCount}, {showBusyIndicator: false});
  }

  protected _sendLimited(limited: boolean) {
    this._send('property', {limited}, {showBusyIndicator: false});
  }

  protected _sendPending(pending: boolean) {
    this._send('property', {pending}, {showBusyIndicator: false});
  }
}
