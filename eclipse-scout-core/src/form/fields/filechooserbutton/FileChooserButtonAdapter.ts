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
import {ValueFieldAdapter} from '../../../index';

export default class FileChooserButtonAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  _onWidgetPropertyChange(event) {
    super._onWidgetPropertyChange(event);

    if (event.propertyName === 'value') {
      this._onValueChange(event);
    }
  }

  _onValueChange(event) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }

  /**
   * @override
   */
  _syncDisplayText(displayText) {
    this.widget.setDisplayText(displayText);
    // When displayText comes from the server we must not call parseAndSetValue here.
  }
}
