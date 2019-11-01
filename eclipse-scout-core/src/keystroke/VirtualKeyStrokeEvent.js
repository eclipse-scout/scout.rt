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
export default class VirtualKeyStrokeEvent {

  constructor(which, ctrl, alt, shift, keyStrokeMode, target) {
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
  }

  stopPropagation() {
    this._propagationStopped = true;
  }

  stopImmediatePropagation() {
    this._immediatePropagationStopped = true;
  }

  preventDefault() {
    this._defaultPrevented = true;
  }

  isPropagationStopped() {
    return this._propagationStopped;
  }

  isImmediatePropagationStopped() {
    return this._immediatePropagationStopped;
  }

  isDefaultPrevented() {
    return this._defaultPrevented;
  }

  isAnyPropagationStopped() {
    return this._propagationStopped || this._immediatePropagationStopped;
  }
}
