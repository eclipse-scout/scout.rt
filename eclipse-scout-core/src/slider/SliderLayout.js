/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics} from '../index';

export default class SliderLayout extends AbstractLayout {

  constructor(slider) {
    super();
    this.slider = slider;
  }

  layout($container) {
    let size = graphics.size($container);
    this.slider.$sliderInput.css('height', size.height);
    this.slider.$sliderValue.css('height', size.height);
  }
}
