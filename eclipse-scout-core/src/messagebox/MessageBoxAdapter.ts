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
import {Event, ModelAdapter} from '../index';
import {MessageBoxActionEvent} from './MessageBoxEventMap';

export default class MessageBoxAdapter extends ModelAdapter {

  constructor() {
    super();
  }

  protected _onWidgetAction(event: MessageBoxActionEvent) {
    this._send('action', {
      option: event.option
    });
  }

  protected override _onWidgetEvent(event: Event) {
    if (event.type === 'action') {
      this._onWidgetAction(event as MessageBoxActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
