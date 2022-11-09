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
import {CopyableWidget, keys, KeyStroke, ScoutKeyboardEvent} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class CopyKeyStroke extends KeyStroke {
  declare field: CopyableWidget;

  constructor(field: CopyableWidget) {
    super();
    this.field = field;
    this.which = [keys.C];
    this.ctrl = true;
    this.preventDefault = false;
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent) => field.$container;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.copy();
  }
}
