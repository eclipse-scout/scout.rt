/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStroke, ScoutKeyboardEvent, Widget} from '../index';

export class ClickActiveElementKeyStroke extends KeyStroke {

  constructor(field: Widget, which: number[]) {
    super();
    this.field = field;
    this.which = which;
    this.stopPropagation = true;
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & { _$activeElement?: JQuery }) => event._$activeElement;
  }

  protected override _accept(event: ScoutKeyboardEvent & { _$activeElement?: JQuery }): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    event._$activeElement = this.field.$container.activeElement();
    return true;
  }

  override handle(event: JQuery.KeyboardEventBase & { _$activeElement?: JQuery }) {
    event._$activeElement.trigger($.Event('click', {
      which: 1
    }));
  }
}
