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
scout.VirtualKeyStrokeEvent = function(which, ctrl, alt, shift, keyStrokeMode, target) {
  this.which = which;
  this.ctrlKey = ctrl;
  this.metaKey = false;
  this.altKey = alt;
  this.shiftKey = shift;
  this.target = target;
  this.type = keyStrokeMode;

  this._propagationStopped = false;
  this._immediatePropagationStopped = false;
  this._defaultPrevented = false;
};

scout.VirtualKeyStrokeEvent.prototype.stopPropagation = function() {
  this._propagationStopped = true;
};

scout.VirtualKeyStrokeEvent.prototype.stopImmediatePropagation = function() {
  this._immediatePropagationStopped = true;
};

scout.VirtualKeyStrokeEvent.prototype.preventDefault = function() {
  this._defaultPrevented = true;
};

scout.VirtualKeyStrokeEvent.prototype.isPropagationStopped = function() {
  return this._propagationStopped;
};

scout.VirtualKeyStrokeEvent.prototype.isImmediatePropagationStopped = function() {
  return this._immediatePropagationStopped;
};

scout.VirtualKeyStrokeEvent.prototype.isDefaultPrevented = function() {
  return this._defaultPrevented;
};

scout.VirtualKeyStrokeEvent.prototype.isAnyPropagationStopped = function() {
  return this._propagationStopped || this._immediatePropagationStopped;
};
