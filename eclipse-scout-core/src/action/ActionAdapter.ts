/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, Event, ModelAdapter} from '../index';

export default class ActionAdapter<A extends Action = Action> extends ModelAdapter<A> {

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

  protected _onWidgetAction(event: Event<A>) {
    if (this.widget.isToggleAction()) {
      return;
    }
    this._send('action');
  }

  protected override _onWidgetEvent(event: Event<A>) {
    if (event.type === 'action') {
      this._onWidgetAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
