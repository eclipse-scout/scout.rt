/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * This KeyStroke works as "catch all" on the top-most DOM element of the Scout application.
 * It prevents that key-strokes from the Scout application (like F5 for reload) 'leak' into the
 * browser which typically happens when the glasspane/busy-indicator is visible. Thus this
 * key-stroke only accepts the event when busy-indicator is active.
 */
scout.DesktopKeyStroke = function(session) {
  scout.DesktopKeyStroke.parent.call(this);

  var keys = scout.keys;
  this.preventDefault = false;
  this.session = session;

  // this key-stroke handles key-shortcuts with and without Ctrl key
  this.whiteListCtrlKeys = [keys.N, keys.R, keys.T, keys.PAGE_UP, keys.PAGE_DOWN];
  this.whiteListKeys = [keys.F11, keys.F12];

  // Never delegate these keys to the browser (F5 is used to reload tables or forms, ctrl-r may be used to reload the browser)
  this.blackListKeys = [keys.F5];
};
scout.inherits(scout.DesktopKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.DesktopKeyStroke.prototype._accept = function(event) {
  if (scout.isOneOf(event.which, this.blackListKeys)) {
    this.preventDefault = true;
    return true;
  }

  // only apply "catch-all" when busy indicator is displayed
  if (this.session._busyIndicator) {
    var whiteList, delegateToBrowser;
    if (event.ctrlKey || event.metaKey) {
      whiteList = this.whiteListCtrlKeys;
    } else {
      whiteList = this.whiteListKeys;
    }
    delegateToBrowser = scout.isOneOf(event.which, whiteList);
    this.preventDefault = !delegateToBrowser;
    // return true since we want to catch all keys and we rely on the _applyPropagationFlags
    // which is executed later and interprets the preventDefault property
    return true;
  }

  return false;
};

/**
 * @override KeyStroke.js
 */
scout.DesktopKeyStroke.prototype.handle = function(event) {
  // NOP
};
