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
import {KeyStrokeMode} from './KeyStroke';
import {ScoutKeyboardEvent} from '../index';

export default class VirtualKeyStrokeEvent implements ScoutKeyboardEvent {
  which: number;
  ctrlKey: boolean;
  metaKey: boolean;
  altKey: boolean;
  shiftKey: boolean;
  target: HTMLElement;
  type: KeyStrokeMode;
  protected _propagationStopped: boolean;
  protected _immediatePropagationStopped: boolean;
  protected _defaultPrevented: boolean;

  constructor(which: number, ctrl: boolean, alt: boolean, shift: boolean, keyStrokeMode: KeyStrokeMode, target: HTMLElement) {
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

  isPropagationStopped(): boolean {
    return this._propagationStopped;
  }

  isImmediatePropagationStopped(): boolean {
    return this._immediatePropagationStopped;
  }

  isDefaultPrevented(): boolean {
    return this._defaultPrevented;
  }

  isAnyPropagationStopped(): boolean {
    return this._propagationStopped || this._immediatePropagationStopped;
  }
}
