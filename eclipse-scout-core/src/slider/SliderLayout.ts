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
import {AbstractLayout, graphics, Slider} from '../index';

export class SliderLayout extends AbstractLayout {
  slider: Slider;

  constructor(slider: Slider) {
    super();
    this.slider = slider;
  }

  override layout($container: JQuery) {
    let size = graphics.size($container);
    this.slider.$sliderInput.css('height', size.height);
    this.slider.$sliderValue.css('height', size.height);
  }
}
