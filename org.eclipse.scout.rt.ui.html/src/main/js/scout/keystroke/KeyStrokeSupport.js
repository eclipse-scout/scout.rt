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
 * Provides methods to "sync" properties 'keyStrokes' and 'menus' on a model-adapter.
 * This class is basically required because Table, Tree and FormField have no common base-class
 * but all require support for keyStrokes and menus.
 */
scout.KeyStrokeSupport = function(adapter) {
  this._adapter = adapter;
};

scout.KeyStrokeSupport.prototype.syncKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this.updateKeyStrokes(newKeyStrokes, oldKeyStrokes);
  this._adapter.keyStrokes = newKeyStrokes;
};

scout.KeyStrokeSupport.prototype.syncMenus = function(newMenus, oldMenus) {
  this.updateKeyStrokes(newMenus, oldMenus);
  this._adapter.menus = newMenus;
};

scout.KeyStrokeSupport.prototype.updateKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this.unregisterKeyStrokes(oldKeyStrokes);
  this.registerKeyStrokes(newKeyStrokes);
};

scout.KeyStrokeSupport.prototype.registerKeyStrokes = function(keyStrokes) {
  keyStrokes = scout.arrays.ensure(keyStrokes);
  keyStrokes.forEach(function(keyStroke) {
    this.keyStrokeContext.registerKeyStroke(keyStroke);
  }, this._adapter);
};

scout.KeyStrokeSupport.prototype.unregisterKeyStrokes = function(keyStrokes) {
  keyStrokes = scout.arrays.ensure(keyStrokes);
  keyStrokes.forEach(function(keyStroke) {
    this.keyStrokeContext.unregisterKeyStroke(keyStroke);
  }, this._adapter);
};
