/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, keys, RangeKeyStroke} from '../../index';

/**
 * KeyStroke to prevent the browser from switching between browser tabs.
 *
 * See DisableBrowserTabSwitchingKeyStroke.js where switching between views is implemented, but only up to the current number of open views.
 * That means, that if 3 views are open, ctrl-4 is prevented by this keystroke.
 */
export class DisableBrowserTabSwitchingKeyStroke extends RangeKeyStroke {
  declare field: Desktop;

  constructor(desktop: Desktop) {
    super();
    this.field = desktop;

    // modifier
    this.parseAndSetKeyStroke(desktop.selectViewTabsKeyStrokeModifier);

    // range [1..9]
    this.registerRange(
      keys['1'], // range from
      keys['9'] // range to
    );

    // rendering hints
    this.renderingHints.render = false;
    this.preventDefault = true;
    this.inheritAccessibility = false;
  }

  protected override _isEnabled(): boolean {
    let enabled = super._isEnabled();
    return enabled && this.field.selectViewTabsKeyStrokesEnabled;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    // NOP
  }
}
