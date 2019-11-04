/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BasicFieldAdapter} from '../../../index';

export default class StringFieldAdapter extends BasicFieldAdapter {

  constructor() {
    super();
  }

  _initProperties(model) {
    if (model.insertText !== undefined) {
      // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
      delete model.insertText;
    }
  }

  _syncInsertText(insertText) {
    this.widget.insertText(insertText);
  }

  _onWidgetSelectionChange(event) {
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

  _onWidgetAction(event) {
    this._send('action');
  }

  _onWidgetEvent(event) {
    if (event.type === 'selectionChange') {
      this._onWidgetSelectionChange(event);
    } else if (event.type === 'action') {
      this._onWidgetAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
