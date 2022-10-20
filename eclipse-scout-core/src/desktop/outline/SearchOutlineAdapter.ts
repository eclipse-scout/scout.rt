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
import {OutlineAdapter} from '../../index';

export default class SearchOutlineAdapter extends OutlineAdapter {

  constructor() {
    super();
  }

  _initProperties(model) {
    if (model.requestFocusQueryField !== undefined) {
      // ignore pseudo property initially (to prevent the function SearchOutlineAdapter#requestFocusQueryField() to be replaced)
      delete model.requestFocusQueryField;
    }
  }

  _syncRequestFocusQueryField() {
    this.widget.focusQueryField();
  }

  _onWidgetSearch(event) {
    this._send('search', {
      query: event.query
    }, {
      showBusyIndicator: false
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'search') {
      this._onWidgetSearch(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
