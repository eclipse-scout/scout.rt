/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ModelAdapter, PopupManager} from '../index';

export class PopupManagerAdapter extends ModelAdapter {

  declare widget: PopupManager;

  protected _syncPopups(popups: any[]) {
    // Wait for every other event in the current response to be processed first.
    // This ensures the anchor will be created first and not by the popup. If the popup created it, the popup would be used as parent.
    // Example case: the popup anchor is a menu widget and its parent should be a menu bar.
    // If the parent was the popup it would generate an endless loop in Widget#isRemovalPending().
    this.session.onEventsProcessed(() => this.widget.setPopups(popups));
  }

}

