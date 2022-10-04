/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ActionAdapter, TableControl} from '../../index';

export default class TableControlAdapter extends ActionAdapter {
  declare widget: TableControl;

  constructor() {
    super();
  }

  protected override _goOffline() {
    if (this.widget.isContentAvailable()) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  protected override _goOnline() {
    if (this.widget.isContentAvailable()) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }
}
