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
import {FormFieldAdapter} from '../../../index';

export default class ImageFieldAdapter extends FormFieldAdapter {

  constructor() {
    super();
  }

  _onWidgetEvent(event) {
    if (event.type === 'fileUpload') {
      this._onFileUpload(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onFileUpload(event) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }
}
