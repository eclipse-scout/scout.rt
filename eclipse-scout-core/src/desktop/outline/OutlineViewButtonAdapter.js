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
import {ViewButtonAdapter} from '../../index';

export default class OutlineViewButtonAdapter extends ViewButtonAdapter {

  constructor() {
    super();
  }

  _goOffline() {
    // Disable only if outline has not been loaded yet
    if (this.widget.outline) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  _goOnline() {
    if (this.widget.outline) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }
}
