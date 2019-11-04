/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LookupFieldAdapter, scout} from '../../../index';

export default class TagFieldAdapter extends LookupFieldAdapter {

  constructor() {
    super();
  }

  _initProperties(model) {
    if (model.insertText !== undefined) {
      // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
      delete model.insertText;
    }
  }

  _postCreateWidget() {
    super._postCreateWidget();
    this.widget.lookupCall = scout.create('RemoteLookupCall', this);
  }

  _syncResult(result) {
    if (this.widget._currentLookupCall) {
      this.widget._currentLookupCall.resolveLookup(result);
    }
  }

  _onWidgetAcceptInput(event) {
    this._send('acceptInput', {
      displayText: event.displayText,
      value: event.value
    });
  }
}
