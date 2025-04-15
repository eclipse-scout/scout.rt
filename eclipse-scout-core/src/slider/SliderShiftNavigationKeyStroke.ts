/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, Slider} from '../index';
import {SliderNavigationKeyStroke} from './SliderNavigationKeyStroke';

export class SliderShiftNavigationKeyStroke extends SliderNavigationKeyStroke {

  constructor(slider: Slider) {
    super(slider);
    this.which = [keys.LEFT, keys.RIGHT];
    this.shift = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    switch (event.which) {
      case keys.LEFT:
        this.field.move(-this._calculatePageMoveUnits(this.field));
        break;

      case keys.RIGHT:
        this.field.move(this._calculatePageMoveUnits(this.field));
        break;

    }
  }
}
