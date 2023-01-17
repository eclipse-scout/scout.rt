/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {OutlineViewButton, ViewButtonAdapter} from '../../index';

export class OutlineViewButtonAdapter extends ViewButtonAdapter {
  declare widget: OutlineViewButton;

  protected override _goOffline() {
    // Disable only if outline has not been loaded yet
    if (this.widget.outline) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  protected override _goOnline() {
    if (this.widget.outline) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }
}
