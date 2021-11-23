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
import {ModelAdapter} from '../../index';

export default class FormFieldAdapter extends ModelAdapter {

  constructor() {
    super();

    /**
     * Set this property to true when the form-field should stay enabled in offline case.
     * By default the field will be disabled.
     */
    this.enabledWhenOffline = false;
  }

  _goOffline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  _goOnline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }

  _onWidgetEvent(event) {
    if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
