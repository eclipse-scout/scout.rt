/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Event, ModelAdapter} from '../index';

export class ActionAdapter extends ModelAdapter {
  declare widget: Action;

  constructor() {
    super();
    this._addRemoteProperties(['selected']);
  }

  protected override _goOffline() {
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  protected override _goOnline() {
    this.widget.setEnabled(this._enabledBeforeOffline);
  }

  protected _onWidgetAction(event: Event<Action>) {
    if (this.widget.isToggleAction()) {
      return;
    }
    this._send('action');
  }

  protected override _onWidgetEvent(event: Event<Action>) {
    if (event.type === 'action') {
      this._onWidgetAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
