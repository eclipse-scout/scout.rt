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
import {ModelAdapter} from '../index';

export default class FormAdapter extends ModelAdapter {

  constructor() {
    super();
  }

  /**
   * @override
   */
  _initModel(model, parent) {
    model = super._initModel(model, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;
    return model;
  }

  _onWidgetEvent(event) {
    if (event.type === 'abort') {
      this._onWidgetAbort(event);
    } else if (event.type === 'close') {
      this._onWidgetClose(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onWidgetAbort(event) {
    // Do not close the form immediately, server will send the close event
    event.preventDefault();

    this._send('formClosing');
    // Waiting for the current request to complete is necessary to be able to check whether the form is still open after the close request.
    this.session.onRequestsDone(() => {
      if (this.widget) {
        this.widget._afterAbort();
      }
    });
  }

  _onWidgetClose(event) {
    this._send('close');
  }

  onModelAction(event) {
    if (event.type === 'requestFocus') {
      this._onRequestFocus(event);
    } else if (event.type === 'requestInput') {
      this._onRequestInput(event);
    } else {
      super.onModelAction(event);
    }
  }

  _onRequestFocus(event) {
    this.session.getOrCreateWidget(event.formField, this.widget).focus();
  }

  _onRequestInput(event) {
    this.session.getOrCreateWidget(event.formField, this.widget).requestInput();
  }
}
