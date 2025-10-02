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

  get slider(): Slider {
    return this.field;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    switch (event.which) {
      case keys.LEFT:
        this.slider.move(-Math.abs(this._calculateMoveSizeLeftWithRemainder()));
        break;

      case keys.RIGHT:
        this.slider.move(Math.abs(this.slider.step));
        break;

      case keys.PAGE_DOWN:
        this.slider.move(-this._calculatePageMoveUnits());
        break;

      case keys.PAGE_UP:
        this.slider.move(this._calculatePageMoveUnits());
        break;

      case keys.HOME:
        this.slider.move(this.slider.minValue - this.slider.maxValue);
        break;

      case keys.END:
        this.slider.move(this.slider.maxValue - this.slider.minValue);
        break;
    }
  }

  protected _calculateMoveSizeLeftWithRemainder(): number {
    const lastStepRemainder = (this.slider.maxValue - this.slider.minValue) % this.slider.step;

    if (this.slider.value === this.slider.maxValue && lastStepRemainder > 0) {
      return lastStepRemainder;
    }
    return this.slider.step;
  }

  protected _calculatePageMoveUnits(): number {
    const PAGE_STEPS = 10;
    // The page move unit has to be at least as big as the step size
    return Math.max((this.slider.maxValue - this.slider.minValue) / PAGE_STEPS, this.slider.step);
  }
}
