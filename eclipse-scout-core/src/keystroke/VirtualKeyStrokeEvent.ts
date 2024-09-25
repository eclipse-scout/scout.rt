/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStrokeMode, ScoutKeyboardEvent} from '../index';

export class VirtualKeyStrokeEvent implements ScoutKeyboardEvent {
  which: number;
  ctrlKey: boolean;
  metaKey: boolean;
  altKey: boolean;
  shiftKey: boolean;
  target: HTMLElement;
  type: KeyStrokeMode;
  originalEvent?: KeyboardEvent & { smartFieldEvent?: boolean } | undefined;
  protected _propagationStopped: boolean;
  protected _immediatePropagationStopped: boolean;
  protected _defaultPrevented: boolean;

  constructor(which: number, ctrl: boolean, alt: boolean, shift: boolean, keyStrokeMode: KeyStrokeMode, eventOrTarget: JQuery.KeyboardEventBase | HTMLElement) {
    this.which = which;
    this.ctrlKey = ctrl;
    this.metaKey = false;
    this.altKey = alt;
    this.shiftKey = shift;
    if (eventOrTarget instanceof HTMLElement) {
      this.target = eventOrTarget;
    } else {
      this.target = eventOrTarget.target;
      this.originalEvent = eventOrTarget.originalEvent;
    }
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
