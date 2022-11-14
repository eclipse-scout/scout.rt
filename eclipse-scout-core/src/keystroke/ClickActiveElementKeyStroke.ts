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
import {KeyStroke, ScoutKeyboardEvent, Widget} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & { _$activeElement?: JQuery }) {
    event._$activeElement.trigger($.Event('click', {
      which: 1
    }));
  }
}
