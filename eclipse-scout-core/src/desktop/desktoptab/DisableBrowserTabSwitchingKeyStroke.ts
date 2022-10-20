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
import {keys, RangeKeyStroke} from '../../index';

/**
 * KeyStroke to prevent the browser from switching between browser tabs.
 *
 * See DisableBrowserTabSwitchingKeyStroke.js where switching between views is implemented, but only up to the current number of open views.
 * That means, that if 3 views are open, ctrl-4 is prevented by this keystroke.
 */
export default class DisableBrowserTabSwitchingKeyStroke extends RangeKeyStroke {

  constructor(desktop) {
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

  /**
   * @override KeyStroke.js
   */
  _isEnabled() {
    let enabled = super._isEnabled();
    return enabled && this.field.selectViewTabsKeyStrokesEnabled;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    // NOOP
  }
}
