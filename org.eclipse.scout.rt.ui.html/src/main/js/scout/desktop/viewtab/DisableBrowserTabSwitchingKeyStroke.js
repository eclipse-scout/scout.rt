/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * KeyStroke to prevent the browser from switching between browser tabs.
 *
 * See DisableBrowserTabSwitchingKeyStroke.js where switching between views is implemented, but only up to the current number of open views.
 * That means, that if 3 views are open, ctrl-4 is prevented by this keystroke.
 */
scout.DisableBrowserTabSwitchingKeyStroke = function(desktop) {
  scout.DisableBrowserTabSwitchingKeyStroke.parent.call(this);
  this.field = desktop;

  // modifier
  this.parseAndSetKeyStroke(desktop.selectViewTabsKeyStrokeModifier);

  // range [1..9]
  this.registerRange(
    scout.keys['1'], // range from
    scout.keys['9'] // range to
  );

  // rendering hints
  this.renderingHints.render = false;

  this.preventDefault = true;
};
scout.inherits(scout.DisableBrowserTabSwitchingKeyStroke, scout.RangeKeyStroke);

/**
 * @override KeyStroke.js
 */
scout.DisableBrowserTabSwitchingKeyStroke.prototype._isEnabled = function() {
  var enabled = scout.DisableBrowserTabSwitchingKeyStroke.parent.prototype._isEnabled.call(this);
  return enabled && this.field.selectViewTabsKeyStrokesEnabled;
};

/**
 * @override KeyStroke.js
 */
scout.DisableBrowserTabSwitchingKeyStroke.prototype.handle = function(event) {
  // NOOP
};
