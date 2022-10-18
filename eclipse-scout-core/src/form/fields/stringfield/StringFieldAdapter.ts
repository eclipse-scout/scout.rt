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
import {BasicFieldAdapter, Event, StringField, StringFieldModel} from '../../../index';
import {StringFieldSelectionChangeEvent} from './StringFieldEventMap';

export default class StringFieldAdapter extends BasicFieldAdapter {
  declare widget: StringField;

  constructor() {
    super();
  }

  protected override _initProperties(model: StringFieldModel) {
    if (model.insertText !== undefined) {
      // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
      delete model.insertText;
    }
  }

  protected _syncInsertText(insertText: string) {
    this.widget.insertText(insertText);
  }

  protected _onWidgetSelectionChange(event: StringFieldSelectionChangeEvent) {
    // send delayed to avoid a lot of requests while selecting
    // coalesce: only send the latest selection changed event for a field
    this._send('selectionChange', {
      selectionStart: event.selectionStart,
      selectionEnd: event.selectionEnd
    }, {
      showBusyIndicator: false,
      delay: 500,
      coalesce: function(previous) {
        return this.target === previous.target && this.type === previous.type;
      }
    });
  }

  protected _onWidgetAction(event: Event<StringField>) {
    this._send('action');
  }

  protected override _onWidgetEvent(event: Event<StringField>) {
    if (event.type === 'selectionChange') {
      this._onWidgetSelectionChange(event as StringFieldSelectionChangeEvent);
    } else if (event.type === 'action') {
      this._onWidgetAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
