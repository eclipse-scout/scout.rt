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
