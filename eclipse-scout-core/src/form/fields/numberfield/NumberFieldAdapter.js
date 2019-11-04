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
import {App, BasicFieldAdapter, NumberField, objects} from '../../../index';

export default class NumberFieldAdapter extends BasicFieldAdapter {
  constructor() {
    super();
  }

  _onWidgetParseError(event) {
    // The parsing might fail on JS side, but it might succeed on server side -> Don't show an error status, instead let the server decide
    event.preventDefault();
  }

  _onWidgetEvent(event) {
    if (event.type === 'parseError') {
      this._onWidgetParseError(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  static modifyPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(NumberField, 'clearErrorStatus', function() {
      if (this.modelAdapter) {
        // Don't do anything -> let server handle it

      } else {
        return this.clearErrorStatusOrig();
      }
    }, true);
  }
}

App.addListener('bootstrap', NumberFieldAdapter.modifyPrototype);
