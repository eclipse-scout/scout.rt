/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, MessageBox, MessageBoxActionEvent, ModelAdapter} from '../index';

export class MessageBoxAdapter extends ModelAdapter {
  declare widget: MessageBox;

  protected _onWidgetAction(event: MessageBoxActionEvent) {
    this._send('action', {
      option: event.option
    });
  }

  protected override _onWidgetEvent(event: Event<MessageBox>) {
    if (event.type === 'action') {
      this._onWidgetAction(event as MessageBoxActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
