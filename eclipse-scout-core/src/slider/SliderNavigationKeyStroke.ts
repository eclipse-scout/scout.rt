/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Slider} from '../index';

export class SliderNavigationKeyStroke extends KeyStroke {
  declare field: Slider;

  constructor(slider: Slider) {
    super();
    this.field = slider;
    this.which = [keys.LEFT, keys.RIGHT, keys.PAGE_DOWN, keys.PAGE_UP, keys.HOME, keys.END];
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
    this.renderingHints.render = false;
    this.repeatable = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    switch (event.which) {
      case keys.LEFT:
        this.field.move(-Math.abs(this.field.step));
        break;

      case keys.RIGHT:
        this.field.move(Math.abs(this.field.step));
        break;

      case keys.PAGE_DOWN:
        this.field.move(-this._calculatePageMoveUnits(this.field));
        break;

      case keys.PAGE_UP:
        this.field.move(this._calculatePageMoveUnits(this.field));
        break;

      case keys.HOME:
        this.field.move(this.field.minValue - this.field.maxValue);
        break;

      case keys.END:
        this.field.move(this.field.maxValue - this.field.minValue);
        break;
    }
  }

  protected _calculatePageMoveUnits(slider: Slider) {
    const PAGE_STEPS = 10;
    return (slider.maxValue - slider.minValue) / PAGE_STEPS;
  }
}
