/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CopyableWidget, keys, KeyStroke, ScoutKeyboardEvent} from '../index';

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

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.copy();
  }
}
