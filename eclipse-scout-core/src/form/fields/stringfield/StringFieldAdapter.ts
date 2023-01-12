/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BasicFieldAdapter, Event, StringField, StringFieldModel, StringFieldSelectionChangeEvent} from '../../../index';

export class StringFieldAdapter extends BasicFieldAdapter {
  declare widget: StringField;

  protected override _initProperties(model: StringFieldModel & { insertText?: boolean }) {
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
